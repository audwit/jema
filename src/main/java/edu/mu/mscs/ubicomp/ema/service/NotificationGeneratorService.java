package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.dao.ContactingTimeRepository;
import edu.mu.mscs.ubicomp.ema.dao.NotificationRepository;
import edu.mu.mscs.ubicomp.ema.dao.ScheduleRepository;
import edu.mu.mscs.ubicomp.ema.entity.ContactingTime;
import edu.mu.mscs.ubicomp.ema.entity.Notification;
import edu.mu.mscs.ubicomp.ema.entity.Schedule;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.sql.Time;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Transactional
public class NotificationGeneratorService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  public static final int TOTAL_TIME_SLOT = 20;
  public static final int SLOT_DURATION = 30;

  private ContactingTimeRepository contactingTimeRepository;
  private ScheduleRepository scheduleRepository;
  private NotificationRepository notificationRepository;
  private final Random random = new Random();
  private LocalDate dummyDate;

  public void setContactingTimeRepository(final ContactingTimeRepository contactingTimeRepository) {
    this.contactingTimeRepository = contactingTimeRepository;
  }

  public void setScheduleRepository(final ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }

  public void setNotificationRepository(final NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  public void setDummyDate(final String dummyDate) {
    if (dummyDate != null) {
      this.dummyDate = LocalDate.parse(dummyDate);
    }
  }

  public void generate() throws ParseException {
    final LocalDateTime now = getCurrentTime();
    final LocalDate today = now.toLocalDate();
    logger.debug("Started notification generation at: {}", today);

    final List<Schedule> schedules = scheduleRepository.findSchedule(DateTimeUtils.toDate(today));
    logger.debug("Total schedules found: " + schedules.size());
    schedules.stream()
        .collect(Collectors.groupingBy(Schedule::getUser))
        .forEach(this::generateNotification);
  }

  private void generateNotification(final User user, final List<Schedule> schedules) {
    final ContactingTime contactingTime = user.getContactingTime();
    if(contactingTime == null) {
      logger.warn("Notification will not be generated. Contacting time not found for user: {}.", user);
      return;
    }

    logger.debug("Generating notifications ContactTime: {}", contactingTime);
    final LocalTime startTime = contactingTime.getStartTime().toLocalTime();
    final int usableSlot = TOTAL_TIME_SLOT - schedules.size() * 3;
    int usedSlot = 0;
    int i = 0;
    for (Schedule schedule : schedules) {
      final int randomSlot = random.nextInt(usableSlot - usedSlot);
      final int baseSlot = usedSlot + randomSlot + i * 3;
      final LocalTime baseTime = startTime.plusMinutes(baseSlot * SLOT_DURATION);
      logger.debug("Randomized notification start time: {}", baseTime);

      final List<Notification> notifications = createNotifications(baseTime, schedule);
      notificationRepository.persistAll(notifications);

      usedSlot += randomSlot;
      i++;
    }
  }

  private List<Notification> createNotifications(final LocalTime baseTime, final Schedule schedule) {
    final List<Notification> notifications = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      final LocalTime startTime = baseTime.plusMinutes(SLOT_DURATION * i);

      final Notification notification = new Notification();
      notification.setSchedule(schedule);
      notification.setSent(false);
      notification.setScheduledTime(Time.valueOf(startTime));
      notification.setSerial(i);
      notifications.add(notification);
    }

    return notifications;
  }

  private LocalDateTime getCurrentTime() {
    return dummyDate == null ? LocalDateTime.now() : LocalDateTime.of(dummyDate, LocalTime.now());
  }

}
