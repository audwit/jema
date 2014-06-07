package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.dao.NotificationRepository;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.Notification;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Transactional
public class NotificationSenderService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private ClickATellClient client;
  private List<String> messages;
  private NotificationRepository notificationRepository;
  private UserRepository userRepository;
  private int totalThread;
  private ExecutorService executorService;

  public void setTotalThread(final int totalThread) {
    this.totalThread = totalThread;
  }

  public void setNotificationRepository(final NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  public void setUserRepository(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void setClient(final ClickATellClient client) {
    this.client = client;
  }

  public void setMessages(final List<String> messages) {
    this.messages = messages;
  }

  @PostConstruct
  public void initialize() {
    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
  }

  public void send() throws ParseException {
    final LocalTime now = LocalTime.now();
    final LocalDateTime scheduledTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(now.getHour(), calculateMinute(now)));

    final String sequenceId = scheduledTime.toString();
    logger.debug("Sending notification at scheduledTime: {}", sequenceId);

    final List<Notification> notifications = notificationRepository.findNotifications(
        DateTimeUtils.toDate(scheduledTime)
    );
    logger.debug("Found total notification: {}", notifications.size());
    if (CollectionUtils.isNotEmpty(notifications)) {
      sendNotifications(notifications, sequenceId);
    }
  }

  private int calculateMinute(final LocalTime now) {
    final int minute = now.getMinute();
    if(minute < 15) {
      return 0;
    }
    else if(minute < 30) {
      return 15;
    }
    else if(minute < 45) {
      return 30;
    }
    return 45;
  }

  private void sendNotifications(final List<Notification> notifications, final String sequenceId) {
    notifications.stream()
        .collect(Collectors.groupingBy(Notification::getSerial))
        .forEach((serial, serialNotifications) -> sendNotifications(serial, serialNotifications, sequenceId));
  }

  private void sendNotifications(final int serial, final List<Notification> notifications, final String sequenceId) {
    final String sequenceNo = sequenceId + "_" + serial;
    final List<String> phoneNumbers = notifications.stream()
        .map(notification -> userRepository.getPhoneNumber(notification.getSchedule().getUser()))
        .collect(Collectors.toList());

    executorService.submit(
        () -> client.sendTextMessage(messages.get(serial), phoneNumbers, sequenceNo)
    );
  }

}
