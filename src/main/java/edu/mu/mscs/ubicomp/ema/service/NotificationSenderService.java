package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.dao.NotificationRepository;
import edu.mu.mscs.ubicomp.ema.entity.Notification;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@Transactional
public class NotificationSenderService {
  public static final SimpleDateFormat SEQUENCE_ID_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  @Value("${notification.dummyNumber}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String dummyNumber;
  @Autowired
  @Value("${notification.message0}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String message0;
  @Autowired
  @Value("${notification.message1}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String message1;
  @Autowired
  @Value("${notification.message2}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String message2;
  @Autowired
  @Value("${notificationSender.totalThread}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private int totalThread;

  @Autowired
  private NotificationRepository notificationRepository;
  @Autowired
  private ClickATellClient client;

  private ExecutorService executorService;
  private ArrayList<String> messages;

  @PostConstruct
  public void initialize() {
    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
    messages = new ArrayList<>(3);
    messages.add(0, message0);
    messages.add(1, message1);
    messages.add(2, message2);
  }

  @Scheduled(cron = "*/30 * * * * *")
  public void send() throws ParseException {
    final Date now = new Date();
    final String sequenceId = SEQUENCE_ID_FORMAT.format(now);
    logger.debug("Sending notification at time: {}", sequenceId);

    final Date date = DATE_FORMATTER.parse(DATE_FORMATTER.format(now));
    final Date time = TIME_FORMATTER.parse(TIME_FORMATTER.format(now));

    final List<Notification> notifications = notificationRepository.findNotifications(date, time);
    if (CollectionUtils.isNotEmpty(notifications)) {
      logger.debug("Found total notification: {}", notifications.size());
      sendNotifications(notifications, sequenceId);
    } else {
      logger.debug("No notification found to be sent");
    }
  }

  private void sendNotifications(final List<Notification> notifications, final String sequenceId) {
    notifications.stream()
        .collect(Collectors.groupingBy(Notification::getSerial))
        .entrySet()
        .forEach(entry -> {
          final Integer serial = entry.getKey();
          sendNotifications(messages.get(serial), entry.getValue(), sequenceId + "_" + serial);
        });
  }

  private void sendNotifications(final String message, final List<Notification> notifications, final String sequenceNo) {
    final List<String> phoneNumbers = notifications.stream()
        .map(notification -> dummyNumber)
        .collect(Collectors.toList());

    executorService.submit(
        () -> client.sendTextMessage(message, phoneNumbers, sequenceNo)
    );
  }

}
