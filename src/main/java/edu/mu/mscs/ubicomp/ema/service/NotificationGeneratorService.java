package edu.mu.mscs.ubicomp.ema.service;

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
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Transactional
public class NotificationGeneratorService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  public static final int NOTIFICATION_COUNT = 3;
  public static final int HOUR_DURATION = 10;
  public static final int SLOT_DURATION = 15;
  public static final int TOTAL_TIME_SLOT = (int) TimeUnit.HOURS.toMinutes(HOUR_DURATION) / SLOT_DURATION;

  private ScheduleRepository scheduleRepository;
  private NotificationRepository notificationRepository;
  private final Random random = new Random();

  public void setScheduleRepository(final ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }

  public void setNotificationRepository(final NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  public void generate() throws ParseException {
    final LocalDateTime now = LocalDateTime.now();
    final LocalDate today = now.toLocalDate();
    logger.debug("Started notification generation at: {}", now);

    final List<Schedule> schedules = scheduleRepository.findSchedule(DateTimeUtils.toDate(today));
    logger.debug("Total schedules found: " + schedules.size());
    schedules.stream()
        .collect(Collectors.groupingBy(Schedule::getUser))
        .forEach(this::generateNotification);
  }

  private void generateNotification(final User user, final List<Schedule> schedules) {
    final ContactingTime contactingTime = user.getContactingTime();
    if (contactingTime == null) {
      logger.warn("Notification will not be generated. Contacting time not found for user: {}.", user);
      return;
    }
    logger.debug("Generating notifications for User: {} using ContactTime: {}", user, contactingTime);
    final Integer[] randomSlots = generateRandomSlots(schedules);
    final LocalDate today = LocalDate.now();
    final LocalDateTime startTime = LocalDateTime.of(today, contactingTime.getStartTime().toLocalTime());

    for (int i = 0; i < schedules.size(); i++) {
      final LocalDateTime baseTime = startTime.plusMinutes((randomSlots[i] + i * NOTIFICATION_COUNT) * SLOT_DURATION);
      final List<Notification> notifications = createNotifications(baseTime, schedules.get(i));
      notificationRepository.persistAll(notifications);
    }
  }

  private Integer[] generateRandomSlots(final List<Schedule> schedules) {
    final Set<Integer> randomSlots = new LinkedHashSet<>();
    final int usedSlot = schedules.size() * NOTIFICATION_COUNT;
    final int remainingSlot = TOTAL_TIME_SLOT - usedSlot;
    while (randomSlots.size() != schedules.size()) {
      randomSlots.add(random.nextInt(remainingSlot));
    }

    final Integer[] container = new Integer[randomSlots.size()];
    return randomSlots.toArray(container);
  }

  private List<Notification> createNotifications(final LocalDateTime baseTime, final Schedule schedule) {
    final List<Notification> notifications = new ArrayList<>();
    for (int i = 0; i < NOTIFICATION_COUNT; i++) {
      final LocalDateTime startTime = baseTime.plusMinutes(SLOT_DURATION * i);

      final Notification notification = new Notification();
      notification.setSchedule(schedule);
      notification.setSent(false);
      notification.setScheduledTime(DateTimeUtils.toTimestamp(startTime));
      notification.setSerial(i);
      notifications.add(notification);
    }

    return notifications;
  }

}
