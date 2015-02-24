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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SurveyReminderScheduler {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public static final List<String> REGULAR_GROUP = Arrays.asList("SS Study Group", "Boning Up Group");
  public static final List<String> WAIT_LIST_GROUP = Arrays.asList("Personal Choice Group");
  private final Map<Integer, Map<Integer, Integer>> regularExpectedSurveyCount = initializeRegularSurveyCount();
  private final Map<Integer, Map<Integer, Integer>> waitListExpectedSurveyCount = initializeWaitListSurveyCount();

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
  private int finalWarningDay;

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

  public void setFinalWarningDay(final int finalWarningDay) {
    this.finalWarningDay = finalWarningDay;
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
    logger.debug("Expected survey requirement for regular participant is: {}", regularExpectedSurveyCount);
    logger.debug("Expected survey requirement for wait list participant is: {}", waitListExpectedSurveyCount);

    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
  }


  public void sendReminder() {
    final LocalDate now = LocalDate.now();
    logger.debug("Reminder service started for: {}", now);

    Stream.of(firstSurveyDay, secondSurveyDay, thirdSurveyDay, fourthSurveyDay).forEach(surveyDay -> {
      sendEmail(now, surveyDay, 0, firstReminderTemplate);
      sendEmail(now, surveyDay, firstWarningDay, secondReminderTemplate);
      sendEmail(now, surveyDay, finalWarningDay, thirdReminderTemplate);
      sendAdminNotification(now, surveyDay);
    });
  }

  private void sendAdminNotification(final LocalDate now, final int surveyDay) {
    final int month = surveyDay / 30;
    final int actualMonth = month >= 11 ? month + 1 : month;
    final int totalDay = surveyDay + surveyInactiveDay;
    final LocalDate startLocalDate = now.minusDays(totalDay);
    final Date startDate = DateTimeUtils.toDate(startLocalDate);
    final List<User> users = getUsersRequiresNotification(actualMonth, startDate);

    if(CollectionUtils.isNotEmpty(users)) {
      final String studyIds = prepareStudyIds(users);
      final String body = String.format(warningEmailTemplate, actualMonth, studyIds);

      logger.debug("Sending unopened measurement notification to admin as start date: {}\nmonth{}\nparticipants: {}",
          startDate.toString(), actualMonth, studyIds);

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
          startDate, actualMonth);
    }
  }

  private String prepareStudyIds(final List<User> users) {
    StringBuilder emailMessageBuilder = new StringBuilder();
    users.forEach((user) -> emailMessageBuilder.append(user.getUsername()).append("\n"));
    return emailMessageBuilder.toString();
  }

  private void sendEmail(final LocalDate now, final int surveyDay, final int warningDay, final String reminderTemplate) {
    final int month = surveyDay / 30;
    final int actualMonth = month >= 11 ? month + 1 : month;
    final int totalDay = surveyDay + warningDay;
    final LocalDate startLocalDate = now.minusDays(totalDay);
    final Date startDate = DateTimeUtils.toDate(startLocalDate);
    final List<User> participants = getUsersRequiresNotification(actualMonth, startDate);

    if(CollectionUtils.isNotEmpty(participants)) {
      logger.debug("Sending email notifications to: {}", participants);
      sendEmail(actualMonth, startDate, participants, reminderTemplate);
    }
    else {
      logger.debug("No participants to send first reminder as start date: {} for {} month survey", startDate, actualMonth);
    }
  }

  private List<User> getUsersRequiresNotification(final int actualMonth, final Date startDate) {
    final Map<Integer, List<Integer[]>> usersSubmittedSurveys = userRepository.getSubmittedSurveyTotal(actualMonth, startDate)
        .stream()
        .collect(Collectors.groupingBy(objects -> objects[0]));

    final List<User> regularUsers = getUsersRequiresNotification(startDate, usersSubmittedSurveys, REGULAR_GROUP, regularExpectedSurveyCount.get(actualMonth));
    final List<User> waitListUsers = getUsersRequiresNotification(startDate, usersSubmittedSurveys, WAIT_LIST_GROUP, waitListExpectedSurveyCount.get(actualMonth));
    regularUsers.addAll(waitListUsers);

    return regularUsers;
  }

  private List<User> getUsersRequiresNotification(final Date startDate,
                                final Map<Integer, List<Integer[]>> usersSubmittedSurveys,
                                final List<String> roles,
                                final Map<Integer, Integer> expectedSurveyCount) {
    return userRepository.findUsersBy(startDate, roles).stream()
        .filter(user -> {
              final List<Integer[]> submittedSurveys = usersSubmittedSurveys.getOrDefault(
                  Integer.valueOf(user.getUsername()),
                  Collections.emptyList()
              );
              final boolean isRequired = isNotificationRequired(submittedSurveys, expectedSurveyCount);
              if(isRequired) {
                logger.debug("Survey reminder required for {} with for surveys: {}", user, submittedSurveys);
              }
              return isRequired;
            }
        )
        .collect(Collectors.toList());
  }

  private static boolean isNotificationRequired(final List<Integer[]> submittedSurveys, final Map<Integer, Integer> expectedSurveyCount) {
    final List<Integer[]> requiredSubmittedSurveys = submittedSurveys.stream()
        .filter(submittedSurvey -> expectedSurveyCount.containsKey(submittedSurvey[1]))
        .collect(Collectors.toList());

    if (!CollectionUtils.isEmpty(requiredSubmittedSurveys) && requiredSubmittedSurveys.size() == expectedSurveyCount.size()) {
      for (final Integer[] submittedSurvey : submittedSurveys) {
        if (submittedSurvey[2] < expectedSurveyCount.get(submittedSurvey[1])) {
          return true;
        }
      }
      return false;
    }

    return true;
  }

  private void sendEmail(final int actualMonth, final Date startDate, final List<User> participants, final String reminderTemplate) {
    final String amount = getAmount(actualMonth);
    final String subject = String.format(reminderSubjectTemplate, actualMonth);
    final String notificationMail = String.format(reminderTemplate, actualMonth, amount);

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

  private String getAmount(final int actualMonth) {
    if(actualMonth <= 3) {
      return "$10";
    }
    else if(actualMonth <= 6) {
      return "$15";
    }
    else if(actualMonth <= 9) {
      return "$20";
    }
    else {
      return "$30";
    }
  }

  private static Map<Integer, Map<Integer, Integer>> initializeRegularSurveyCount() {
    Map<Integer, Map<Integer, Integer>> expectedSurveyCount = new HashMap<>();
    expectedSurveyCount.put(3, getExpectedSurveyCountMonth3());
    expectedSurveyCount.put(6, getExpectedSurveyCountMonth6and9());
    expectedSurveyCount.put(9, getExpectedSurveyCountMonth6and9());
    expectedSurveyCount.put(12, getExpectedSurveyCountMonth12());

    return Collections.unmodifiableMap(expectedSurveyCount);
  }

  private static Map<Integer, Map<Integer, Integer>> initializeWaitListSurveyCount() {
    Map<Integer, Map<Integer, Integer>> expectedSurveyCount = new HashMap<>();
    final Map<Integer, Integer> expectedSurveyCountMonth3 = new HashMap<>(getExpectedSurveyCountMonth3());
    expectedSurveyCount.remove(16);

    final Map<Integer, Integer> expectedSurveyCountMonth12 = new HashMap<>(getExpectedSurveyCountMonth12());
    expectedSurveyCountMonth12.put(14, 1);
    expectedSurveyCountMonth12.put(16, 1);

    expectedSurveyCount.put(3, Collections.unmodifiableMap(expectedSurveyCountMonth3));
    expectedSurveyCount.put(6, getExpectedSurveyCountMonth6and9());
    expectedSurveyCount.put(9, getExpectedSurveyCountMonth6and9());
    expectedSurveyCount.put(12, Collections.unmodifiableMap(expectedSurveyCountMonth12));

    return Collections.unmodifiableMap(expectedSurveyCount);
  }

  private static Map<Integer, Integer> getExpectedSurveyCountMonth3() {
    final Map<Integer, Integer> expectedSurveyCounts = new HashMap<>();
    expectedSurveyCounts.put(4, 3);
    expectedSurveyCounts.put(14, 1);
    expectedSurveyCounts.put(15, 1);
    expectedSurveyCounts.put(16, 1);
    expectedSurveyCounts.put(19, 1);
    expectedSurveyCounts.put(20, 1);
    expectedSurveyCounts.put(21, 1);
    expectedSurveyCounts.put(22, 1);
    expectedSurveyCounts.put(23, 1);

    return Collections.unmodifiableMap(expectedSurveyCounts);
  }

  private static Map<Integer, Integer> getExpectedSurveyCountMonth6and9() {
    final Map<Integer, Integer> expectedSurveyCounts = new HashMap<>();
    expectedSurveyCounts.put(4, 3);
    expectedSurveyCounts.put(15, 1);
    expectedSurveyCounts.put(19, 1);
    expectedSurveyCounts.put(22, 1);
    expectedSurveyCounts.put(23, 1);

    return Collections.unmodifiableMap(expectedSurveyCounts);
  }

  private static Map<Integer, Integer> getExpectedSurveyCountMonth12() {
    Map<Integer, Integer> expectedSurveyCounts = new HashMap<>();
    expectedSurveyCounts.put(4, 3);
    expectedSurveyCounts.put(8, 1);
    expectedSurveyCounts.put(9, 1);
    expectedSurveyCounts.put(12, 1);
    expectedSurveyCounts.put(13, 1);
    expectedSurveyCounts.put(15, 1);
    expectedSurveyCounts.put(18, 1);
    expectedSurveyCounts.put(19, 1);
    expectedSurveyCounts.put(20, 1);
    expectedSurveyCounts.put(21, 1);
    expectedSurveyCounts.put(22, 1);
    expectedSurveyCounts.put(23, 1);
    expectedSurveyCounts.put(24, 1);
    expectedSurveyCounts.put(25, 1);

    return Collections.unmodifiableMap(expectedSurveyCounts);
  }

}
