package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.dao.NotificationRepository;
import edu.mu.mscs.ubicomp.ema.dao.ScheduleRepository;
import edu.mu.mscs.ubicomp.ema.entity.ContactingTime;
import edu.mu.mscs.ubicomp.ema.entity.Notification;
import edu.mu.mscs.ubicomp.ema.entity.Schedule;
import edu.mu.mscs.ubicomp.ema.entity.User;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@EnableScheduling
public class NotificationGeneratorService {
  private static final SimpleDateFormat ZERO_TIME_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
  private Logger logger = LoggerFactory.getLogger(getClass());

  public static final int TOTAL_TIME_SLOT = 20;
  public static final int SLOT_DURATION = 30;

  @Autowired
  private ScheduleRepository scheduleRepository;
  @Autowired
  private NotificationRepository notificationRepository;
  private final Random random = new Random();

  @Scheduled(cron = "*/10 * * * * *")
  public void generate() throws ParseException {
    final Date now = new Date();
    final Date today = ZERO_TIME_FORMATTER.parse(ZERO_TIME_FORMATTER.format(now));
    logger.debug("Started notification generation at: {}", today);

    final List<Schedule> schedules = scheduleRepository.findSchedule(today);
    logger.debug("Total schedules found: " + schedules.size());
    if (CollectionUtils.isNotEmpty(schedules)) {
      schedules
          .stream()
          .collect(Collectors.groupingBy(Schedule::getUser))
          .forEach(this::generateNotification);
    } else {
      logger.debug("No schedule found to generate notification.");
    }
  }

  private void generateNotification(final User user, final List<Schedule> schedules) {
    final ContactingTime contactingTime = user.getContactingTime();
    if(contactingTime == null) {
      logger.warn("No contacting time for user: " + user);
      return;
    }
    logger.debug("Generating notifications ContactTime: {}", contactingTime);
    final int usableSlot = TOTAL_TIME_SLOT - schedules.size() * 3;
    int usedSlot = 0;
    int i = 0;
    for (Schedule schedule : schedules) {
      final int randomSlot = random.nextInt(usableSlot - usedSlot);
      final int baseSlot = usedSlot + randomSlot + i * 3;
      final Time baseTime = addMinute(contactingTime.getStartTime(), baseSlot * SLOT_DURATION);
      logger.debug("Randomized notification start time: {}", baseTime);

      final List<Notification> notifications = createNotifications(baseTime, schedule);
      notificationRepository.persistAll(notifications);

      usedSlot += randomSlot;
      i++;
    }
  }

  private List<Notification> createNotifications(final Time baseTime, final Schedule schedule) {
    final List<Notification> notifications = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      final Time startTime = addMinute(baseTime, SLOT_DURATION * i);

      final Notification notification = new Notification();
      notification.setSchedule(schedule);
      notification.setSent(false);
      notification.setScheduledTime(startTime);
      notification.setSerial(i);
      notifications.add(notification);
    }

    return notifications;
  }

  private Time addMinute(final Time time, final int minute) {
    final long time1 = time.getTime();
    final long time2 = TimeUnit.MINUTES.toMillis(minute);
    return new Time(time1 + time2);
  }

}
