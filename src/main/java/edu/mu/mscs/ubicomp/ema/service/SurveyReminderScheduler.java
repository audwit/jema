package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.MailClient;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.apache.commons.collections.CollectionUtils;
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
  private MailClient mailClient;
  private ExecutorService executorService;
  private int totalThread;

  private int firstSurveyDay;
  private int secondSurveyDay;
  private int thirdSurveyDay;
  private int fourthSurveyDay;

  private int surveyInactiveDay;
  private int firstWarningDay;

  private String reminderSubjectTemplate;
  private String firstReminderTemplate;
  private String secondReminderTemplate;
  private String thirdReminderTemplate;

  private String warningEmailAddress;
  private String warmingEmailSubject;
  private String warningEmailTemplate;

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

  public void setFirstWarningDay(final int firstWarningDay) {
    this.firstWarningDay = firstWarningDay;
  }

  public void setReminderSubjectTemplate(final String reminderSubjectTemplate) {
    this.reminderSubjectTemplate = reminderSubjectTemplate;
  }

  public void setFirstReminderTemplate(final String firstReminderTemplate) {
    this.firstReminderTemplate = firstReminderTemplate;
  }

  public void setSecondReminderTemplate(final String secondReminderTemplate) {
    this.secondReminderTemplate = secondReminderTemplate;
  }

  public void setThirdReminderTemplate(final String thirdReminderTemplate) {
    this.thirdReminderTemplate = thirdReminderTemplate;
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

  public void setWarningEmailAddress(final String warningEmailAddress) {
    this.warningEmailAddress = warningEmailAddress;
  }

  public void setWarmingEmailSubject(final String warmingEmailSubject) {
    this.warmingEmailSubject = warmingEmailSubject;
  }

  public void setWarningEmailTemplate(final String warningEmailTemplate) {
    this.warningEmailTemplate = warningEmailTemplate;
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

    sendNotifications(firstSurveyDay, "threemonths");
    sendNotifications(secondSurveyDay, "sixmonths");
    sendNotifications(thirdSurveyDay, "ninemonths");
    sendNotifications(fourthSurveyDay, "twelvemonths");
  }

  private void sendNotifications(final int surveyDay, final String surveyType) {
    sendFirstMeasurementReminder(surveyDay);
    sendSecondMeasurementReminder(surveyDay, surveyType);
    sendThirdMeasurementReminder(surveyDay, surveyType);
    sendAdminNotification(surveyDay, surveyType);
  }

  private void sendFirstMeasurementReminder(final int surveyDay) {
    List<String> roles = new ArrayList<>(GiftCardNotifier.REGULAR_GROUP);
    roles.addAll(GiftCardNotifier.WAIT_LIST_GROUP);
    final LocalDate today = LocalDate.now();
    final LocalDate startDate = today.minusDays(surveyDay);
    final List<User> participants = userRepository.findUsersBy(DateTimeUtils.toDate(startDate), roles);

    final int month = surveyDay / 30;
    final int actualMonth = month >= 11 ? month + 1 : month;
    if(CollectionUtils.isNotEmpty(participants)) {
      final String subject = String.format(reminderSubjectTemplate, actualMonth);
      final String notificationMail = String.format(firstReminderTemplate, String.valueOf(actualMonth), String.valueOf(10));

      logger.debug("Sending measurement email to total: {} user to remind with start date: {} for {} month survey",
          participants.size(), startDate, actualMonth);
      for (User user : participants) {
        executorService.submit(() -> {
          try {
            mailClient.send(user.getEmail(), subject, notificationMail);
          } catch (MessagingException e) {
            logger.warn("Failed sending first notification to " + user.getEmail() + " for user: " + user.getId(), e);
          }
        });
      }
    }
    else {
      logger.debug("No participants to send first reminder as start date: {} for {} month survey",
          startDate, actualMonth);
    }
  }

  private void sendSecondMeasurementReminder(final int surveyDay, final String surveyType) {
    final LocalDate now = LocalDate.now();
    final int totalDay = surveyDay + firstWarningDay;
    final LocalDate startLocalDateTime = now.minusDays(totalDay);
    final Date startDate = DateTimeUtils.toDate(startLocalDateTime);
    final List<User> participants = userRepository.getRequiresNotificationUsers(surveyType, startDate);

    final int month = surveyDay / 30;
    final int actualMonth = month >= 11 ? month + 1 : month;
    if(CollectionUtils.isNotEmpty(participants)) {
      final String subject = String.format(reminderSubjectTemplate, actualMonth);
      final String notificationMail = String.format(secondReminderTemplate, String.valueOf(actualMonth), String.valueOf(10));

      logger.debug("Sending second reminder to participants as start date: {} for {} month survey",
          startLocalDateTime.toString(), actualMonth);
      for (User user : participants) {
        executorService.submit(() -> {
          try {
            mailClient.send(user.getEmail(), subject, notificationMail);
          } catch (MessagingException e) {
            logger.warn("Failed sending second notification to " + user.getEmail() + " for user: " + user.getId(), e);
          }
        });
      }
    }
    else {
      logger.debug("No participants to send second reminder as start date: {} for {} month survey",
          startLocalDateTime, actualMonth);
    }
  }

  private void sendThirdMeasurementReminder(final int surveyDay, final String surveyType) {
    final LocalDate now = LocalDate.now();
    final int totalDay = surveyDay + surveyInactiveDay - 1;
    final LocalDate startLocalDate = now.minusDays(totalDay);
    final Date startDate = DateTimeUtils.toDate(startLocalDate);
    final List<User> users = userRepository.getRequiresNotificationUsers(surveyType, startDate);

    final int month = surveyDay / 30;
    final int actualMonth = month >= 11 ? month + 1 : month;
    if(CollectionUtils.isNotEmpty(users)) {
      final String amount = String.valueOf(getAmount(actualMonth));
      final String subject = String.format(reminderSubjectTemplate, actualMonth);
      final String body = String.format(thirdReminderTemplate, actualMonth, amount);

      logger.debug("Sending third reminder to participants as start date: {} for {} month survey",
          startLocalDate, actualMonth);
      for (User user : users) {
        executorService.submit(() -> {
          try {
            mailClient.send(user.getEmail(), subject, body);
          } catch (MessagingException e) {
            logger.warn("Failed sending third notification to " + user.getEmail() + " for user: " + user.getId(), e);
          }
        });
      }
    }
  }

  private void sendAdminNotification(final int surveyDay, final String surveyType) {
    final LocalDate now = LocalDate.now();
    final int totalDay = surveyDay + surveyInactiveDay;
    final LocalDate startLocalDateTime = now.minusDays(totalDay);
    final Date startDate = DateTimeUtils.toDate(startLocalDateTime);
    final List<User> users = userRepository.getRequiresNotificationUsers(surveyType, startDate);

    final int month = surveyDay / 30;
    final int actualMonth = month >= 11 ? month + 1 : month;
    if(CollectionUtils.isNotEmpty(users)) {
      final String studyIds = prepareStudyIds(users);
      final String body = String.format(warningEmailTemplate, actualMonth, studyIds);

      logger.debug("Sending unopened measurement notification to admin as start date: {} for {} month survey",
          startLocalDateTime.toString(), actualMonth);

      executorService.submit(() -> {
        try {
          mailClient.send(warningEmailAddress, warmingEmailSubject, body);
        } catch (MessagingException e) {
          logger.warn("Failed sending admin notification to " + warningEmailAddress, e);
        }
      });
    }
    else {
      logger.debug("No participants to send enlist for warning as start date: {} for {} month survey",
          startLocalDateTime, actualMonth);
    }
  }

  private String prepareStudyIds(final List<User> users) {
    StringBuilder emailMessageBuilder = new StringBuilder();
    users.forEach((user) -> emailMessageBuilder.append(user.getUsername()).append("\n"));
    return emailMessageBuilder.toString();
  }

  private int getAmount(final int actualMonth) {
    if(actualMonth <= 3) {
      return 10;
    }
    else if(actualMonth <= 6) {
      return 15;
    }
    else if(actualMonth <= 9) {
      return 20;
    }
    else {
      return 30;
    }
  }

}
