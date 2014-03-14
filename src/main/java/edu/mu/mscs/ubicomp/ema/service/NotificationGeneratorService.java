package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.dao.NotificationRepository;
import edu.mu.mscs.ubicomp.ema.dao.ScheduleRepository;
import edu.mu.mscs.ubicomp.ema.entity.ContactingTime;
import edu.mu.mscs.ubicomp.ema.entity.Notification;
import edu.mu.mscs.ubicomp.ema.entity.Schedule;
import edu.mu.mscs.ubicomp.ema.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@EnableScheduling
public class NotificationGeneratorService {
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
  private Logger logger = LoggerFactory.getLogger(getClass());

  public static final int TOTAL_TIME_SLOT = 20;
  public static final int SLOT_DURATION = 30;

  @Autowired
  private ScheduleRepository scheduleRepository;
  @Autowired
  private NotificationRepository notificationRepository;

  @Scheduled(cron = "*/10 * * * * *")
  public void generate() {
    Date today = new Date();
    logger.debug("Started notification generation at: " + dateFormat.format(today));

    final List<Schedule> schedules = scheduleRepository.findSchedule(today);
    logger.debug("Total schedules found: " + schedules.size());

    Map<User, List<Schedule>> userSchedulesMap = generateUserSchedulesMap(schedules);
    for (Map.Entry<User, List<Schedule>> userSchedulesEntry : userSchedulesMap.entrySet()) {
      generateNotificationForUser(userSchedulesEntry.getKey(), userSchedulesEntry.getValue(), today);
    }
  }

  private void generateNotificationForUser(final User user, final List<Schedule> userSchedules, final Date today) {
    final ContactingTime contactingTime = user.getContactingTime();
    if(contactingTime != null) {
      logger.debug("Generating notifications for user: " + user + " with contact time: " + contactingTime);
      generateNotification(today, contactingTime, userSchedules);
    }
    else {
      logger.warn("No contacting time for user: " + user);
    }
  }

  private void generateNotification(final Date today, final ContactingTime contactingTime, final List<Schedule> schedules) {
    final Random random = new Random();
    final int usableSlot = TOTAL_TIME_SLOT - schedules.size() * 3;
    int usedSlot = 0;
    for (Schedule schedule : schedules) {
      final int randomSlot = random.nextInt(usableSlot - usedSlot);
      final Time baseTime = addMinute(contactingTime.getStartTime(), (usedSlot + randomSlot) * SLOT_DURATION);
      final List<Notification> notifications = createNotifications(today, baseTime, schedule);
      logger.debug("Total notification generated for schedule: " + schedule + " total: " + notifications.size());
      notificationRepository.persistAll(notifications);

      usedSlot += randomSlot;
    }
  }

  private List<Notification> createNotifications(final Date today, final Time baseTime, final Schedule schedule) {
    final List<Notification> notifications = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      final Time startTime = addMinute(baseTime, SLOT_DURATION * i);
      final Timestamp scheduledTime = new Timestamp(startTime.getTime() + today.getTime());

      final Notification notification = new Notification();
      notification.setSchedule(schedule);
      notification.setSent(false);
      notification.setScheduledTime(scheduledTime);
      notifications.add(notification);
    }

    return notifications;
  }

  private Time addMinute(final Time time, final int minute) {
    final long time1 = time.getTime();
    final long time2 = TimeUnit.MINUTES.toMillis(minute);
    return new Time(time1 + time2);
  }

  private Map<User, List<Schedule>> generateUserSchedulesMap(final List<Schedule> schedules) {
    Map<User, List<Schedule>> userSchedulesMap = new HashMap<>();
    for (Schedule schedule : schedules) {
      List<Schedule> userSchedules = userSchedulesMap.get(schedule.getUser());
      if (userSchedules == null) {
        userSchedules = new ArrayList<>();
        userSchedulesMap.put(schedule.getUser(), userSchedules);
      }
      userSchedules.add(schedule);
    }
    return userSchedulesMap;
  }

}
