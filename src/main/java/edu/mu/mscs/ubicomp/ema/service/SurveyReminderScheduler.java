package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.MailClient;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SurveyReminderScheduler {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private UserRepository userRepository;

  private int firstSurveyDay;
  private int secondSurveyDay;
  private int thirdSurveyDay;
  private int fourthSurveyDay;

  private int surveyInactiveDay;

  private MailClient mailClient;
  private int totalThread;
  private ExecutorService executorService;
  private String mail3;
  private String mail6;
  private String mail9;
  private String mail12;
  private String reminderSubject;

  public void setFirstSurveyDay(final int firstSurveyDay) {
    this.firstSurveyDay = firstSurveyDay;
  }

  public void setSecondSurveyDay(final int secondSurveyDay) {
    this.secondSurveyDay = secondSurveyDay;
  }

  public void setThirdSurveyDay(final int thirdSurveyDay) {
    this.thirdSurveyDay = thirdSurveyDay;
  }

  public void setFourthSurveyDay(final int fourthSurveyDay) {
    this.fourthSurveyDay = fourthSurveyDay;
  }

  public void setSurveyInactiveDay(final int surveyInactiveDay) {
    this.surveyInactiveDay = surveyInactiveDay;
  }

  public void setMail3(final String mail3) {
    this.mail3 = mail3;
  }

  public void setMail6(final String mail6) {
    this.mail6 = mail6;
  }

  public void setMail9(final String mail9) {
    this.mail9 = mail9;
  }

  public void setMail12(final String mail12) {
    this.mail12 = mail12;
  }

  public void setReminderSubject(final String reminderSubject) {
    this.reminderSubject = reminderSubject;
  }

  public void setUserRepository(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void setMailClient(final MailClient mailClient) {
    this.mailClient = mailClient;
  }

  public void setTotalThread(final int totalThread) {
    this.totalThread = totalThread;
  }

  @PostConstruct
  public void initialize() {
    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
  }


  public void sendReminder() {
    logger.debug("Reminder service started");
    sendMeasurementReminder(firstSurveyDay, mail3);
    sendMeasurementReminder(secondSurveyDay, mail6);
    sendMeasurementReminder(thirdSurveyDay, mail9);
    sendMeasurementReminder(fourthSurveyDay, mail12);

    findUnopenedSurvey(firstSurveyDay, "threemonths");
    findUnopenedSurvey(secondSurveyDay, "sixmonths");
    findUnopenedSurvey(thirdSurveyDay, "ninemonths");
    findUnopenedSurvey(fourthSurveyDay, "twelvemonths");
  }

  private void sendMeasurementReminder(final int surveyDay, final String notificationMail) {
    List<String> roles = new ArrayList<>(GiftCardNotifier.REGULAR_GROUP);
    roles.addAll(GiftCardNotifier.WAIT_LIST_GROUP);
    final LocalDate today = LocalDate.now();
    final LocalDate startDate = today.minusDays(surveyDay);

    final List<User> participants = userRepository.findUsersBy(DateTimeUtils.toDate(startDate), roles);
    logger.debug("Found total: {} user to remind with start date: {}", participants.size(), startDate.toString());

    for (User user : participants) {
      executorService.submit(() -> {
        try {
          mailClient.send(user.getEmail(), reminderSubject, notificationMail);
        } catch (MessagingException e) {
          logger.warn("Failed sending notificationMail notification to " + user.getEmail() + " for user: " + user.getId(), e);
        }
      });
    }
  }

  private void findUnopenedSurvey(final int surveyDay, final String surveyType) {
    final LocalDate now = LocalDate.now();
    final int totalDay = surveyDay + surveyInactiveDay;
    final LocalDate startLocalDateTime = now.minusDays(totalDay);
    final Date startDate = DateTimeUtils.toDate(startLocalDateTime);
    final List<User> users = userRepository.getRequiresNotificationUsers(surveyType, startDate);
    logger.debug("Found total: {} user to remind for: {} with start date: {}", users.size(), surveyType, startLocalDateTime.toString());

    for (User user : users) {
      executorService.submit(() -> {
        try {
          mailClient.send(user.getEmail(), reminderSubject, "dummy email for now");
        } catch (MessagingException e) {
          logger.warn("Failed sending notificationMail notification to " + user.getEmail() + " for user: " + user.getId(), e);
        }
      });
    }
  }

}
