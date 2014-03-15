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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@EnableScheduling
@Transactional
public class NotificationSenderService {
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");
  private Logger logger = LoggerFactory.getLogger(getClass());

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  @Value("${notification.dummyNumber}")
  private String dummyNumber;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${notification.message0}")
  private String message0;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${notification.message1}")
  private String message1;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${notification.message2}")
  private String message2;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${notificationSender.totalThread}")
  private int totalThread;

  @Autowired(required = true)
  private NotificationRepository notificationRepository;
  @Autowired(required = true)
  private ClickATellClient client;

  private ExecutorService executorService;

  @PostConstruct
  public void initialize() {
    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
  }

  @Scheduled(cron = "*/10 * * * * *")
  public void send() throws ParseException {
    final Date now = new Date();
    logger.debug("Sending notification at time: {}", now);

    final Date date = DATE_FORMATTER.parse(DATE_FORMATTER.format(now));
    final Date time = TIME_FORMATTER.parse(TIME_FORMATTER.format(now));

    final List<Notification> notifications = notificationRepository.findNotifications(date, time);
    if (CollectionUtils.isNotEmpty(notifications)) {
      logger.debug("Found total notification: {}", notifications.size());
      sendNotifications(notifications);
    }
    else {
      logger.debug("No notification found to be sent");
    }
  }

  private void sendNotifications(final List<Notification> notifications) {
    final Map<Integer, List<Notification>> notificationBuckets = new HashMap<>();
    for (Notification notification : notifications) {
      final Integer serial = notification.getSerial();
      List<Notification> notificationBucket = notificationBuckets.get(serial);

      if(notificationBucket == null) {
        notificationBucket = new LinkedList<>();
        notificationBuckets.put(serial, notificationBucket);
      }
      notificationBucket.add(notification);
    }

    sendNotifications(message0, notificationBuckets.get(0));
    sendNotifications(message1, notificationBuckets.get(1));
    sendNotifications(message2, notificationBuckets.get(2));
  }

  private void sendNotifications(final String message, final List<Notification> notifications) {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        final List<String> phoneNumbers = new LinkedList<>();
        for (Notification notification : notifications) {
          phoneNumbers.add(dummyNumber);
        }
        client.sendTextMessage(message, phoneNumbers);
      }
    });
  }

}
