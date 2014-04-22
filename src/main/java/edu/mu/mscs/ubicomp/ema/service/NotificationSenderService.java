package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.dao.NotificationRepository;
import edu.mu.mscs.ubicomp.ema.entity.Notification;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationSenderService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  @Value("${notification.dummyNumber}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String dummyNumber;
  @Autowired
  @Value("${notificationSender.totalThread}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private int totalThread;

  @Autowired
  private NotificationRepository notificationRepository;
  @Autowired
  private ClickATellClient client;

  private ExecutorService executorService;
  private List<String> messages;

  @Autowired
  @Value("${notification.messages}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  public void setMessages(final String messages) {
    this.messages = new ArrayList<>(Arrays.asList(messages.split(",")));
  }

  @PostConstruct
  public void initialize() {
    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
  }

  public void send() throws ParseException {
    final LocalDateTime now = LocalDateTime.now();
    final LocalDate date = now.toLocalDate();
    final LocalTime time = LocalTime.of(now.getHour(), now.getMinute() < 30 ? 0 : 30);

    final String sequenceId = now.toString();
    logger.debug("Sending notification at time: {}", sequenceId);

    final List<Notification> notifications = notificationRepository.findNotifications(
        DateTimeUtils.toDate(date),
        DateTimeUtils.toDate(time)
    );
    logger.debug("Found total notification: {}", notifications.size());
    if (CollectionUtils.isNotEmpty(notifications)) {
      sendNotifications(notifications, sequenceId);
    }
  }

  private void sendNotifications(final List<Notification> notifications, final String sequenceId) {
    notifications.stream()
        .collect(Collectors.groupingBy(Notification::getSerial))
        .forEach((serial, serialNotifications) -> sendNotifications(serial, serialNotifications, sequenceId));
  }

  private void sendNotifications(final int serial, final List<Notification> notifications, final String sequenceId) {
    final String sequenceNo = sequenceId + "_" + serial;
    final List<String> phoneNumbers = notifications.stream()
        .map(notification -> dummyNumber)
        .collect(Collectors.toList());

    executorService.submit(
        () -> client.sendTextMessage(messages.get(serial), phoneNumbers, sequenceNo)
    );
  }

}
