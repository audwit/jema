package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.client.MailClient;
import edu.mu.mscs.ubicomp.ema.dao.MessageRepository;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.Message;
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

public class ReminderService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private Random random = new Random();

  private String message1;
  private String message2;
  private String email1;
  private String email2;
  private String subject1;
  private String subject2;
  private int firstNotificationDifference;
  private int secondNotificationDifference;
  private int thirdNotificationDifference;
  private int fourthNotificationDifference;
  private long inactiveWarningDate;
  private String warningEmailAddress;
  private String warningEmailSubject;
  private String inactiveEmailTemplate;
  private String chooseGroupSubject;
  private String chooseGroupEmail;
  private int chooseGroupDifference;
  private String studyEndSubject;
  private String studyEndEmail;
  private int studyEndDifference;

  private ClickATellClient textMessageClient;
  private MailClient mailClient;
  private MessageRepository messageRepository;
  private UserRepository userRepository;
  private String baseUrl;
  private int totalThread;
  private ExecutorService executorService;

  public void setMessage1(final String message1) {
    this.message1 = message1;
  }

  public void setMessage2(final String message2) {
    this.message2 = message2;
  }

  public void setEmail1(final String email1) {
    this.email1 = email1;
  }

  public void setEmail2(final String email2) {
    this.email2 = email2;
  }

  public void setSubject1(final String subject1) {
    this.subject1 = subject1;
  }

  public void setSubject2(final String subject2) {
    this.subject2 = subject2;
  }

  public void setFirstNotificationDifference(final int firstNotificationDifference) {
    this.firstNotificationDifference = firstNotificationDifference;
  }

  public void setSecondNotificationDifference(final int secondNotificationDifference) {
    this.secondNotificationDifference = secondNotificationDifference;
  }

  public void setThirdNotificationDifference(final int thirdNotificationDifference) {
    this.thirdNotificationDifference = thirdNotificationDifference;
  }

  public void setFourthNotificationDifference(final int fourthNotificationDifference) {
    this.fourthNotificationDifference = fourthNotificationDifference;
  }

  public void setInactiveWarningDate(final long inactiveWarningDate) {
    this.inactiveWarningDate = inactiveWarningDate;
  }

  public void setWarningEmailAddress(final String warningEmailAddress) {
    this.warningEmailAddress = warningEmailAddress;
  }

  public void setWarningEmailSubject(final String warningEmailSubject) {
    this.warningEmailSubject = warningEmailSubject;
  }

  public void setInactiveEmailTemplate(final String inactiveEmailTemplate) {
    this.inactiveEmailTemplate = inactiveEmailTemplate;
  }

  public void setChooseGroupSubject(final String chooseGroupSubject) {
    this.chooseGroupSubject = chooseGroupSubject;
  }

  public void setChooseGroupEmail(final String chooseGroupEmail) {
    this.chooseGroupEmail = chooseGroupEmail;
  }

  public void setChooseGroupDifference(final int chooseGroupDifference) {
    this.chooseGroupDifference = chooseGroupDifference;
  }

  public void setStudyEndSubject(final String studyEndSubject) {
    this.studyEndSubject = studyEndSubject;
  }

  public void setStudyEndEmail(final String studyEndEmail) {
    this.studyEndEmail = studyEndEmail;
  }

  public void setStudyEndDifference(final int studyEndDifference) {
    this.studyEndDifference = studyEndDifference;
  }

  public void setTextMessageClient(final ClickATellClient textMessageClient) {
    this.textMessageClient = textMessageClient;
  }

  public void setMailClient(final MailClient mailClient) {
    this.mailClient = mailClient;
  }

  public void setMessageRepository(final MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  public void setUserRepository(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setTotalThread(final int totalThread) {
    this.totalThread = totalThread;
  }

  @PostConstruct
  public void init() {
    final BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
        .namingPattern(getClass().getName() + "-%d")
        .build();
    executorService = Executors.newFixedThreadPool(totalThread, threadFactory);
  }

  public void sendNotifications() {
    final LocalDate today = LocalDate.now();
    sendFirstReminder(today);
    sendSecondReminder(today);
    sendThirdReminder(today);
    sendFourthReminder(today);
    sendInactiveUsersList(today);
    sendEightMonthReminder(today);
    sendEndOfStudyReminder(today);
  }

  private void sendFirstReminder(final LocalDate today) {
    final LocalDate lastLoginDate = today.minusDays(firstNotificationDifference);
    final List<User> inactiveUsers = getLastLoggedInOn(lastLoginDate);

    if(CollectionUtils.isNotEmpty(inactiveUsers)) {
      final Message message = retrieveRandomMessage();
      logger.debug("Sending first inactivity reminder to {} participants", inactiveUsers.size());
      sendTextMessage(inactiveUsers, message, message2);
    }
    else {
      logger.debug("No user to send first inactivity reminders");
    }
  }

  private void sendSecondReminder(final LocalDate today) {
    final LocalDate lastLoginDate = today.minusDays(secondNotificationDifference);
    final List<User> inactiveUsers = getLastLoggedInOn(lastLoginDate);

    if(CollectionUtils.isNotEmpty(inactiveUsers)) {
      final Message message = retrieveRandomMessage();
      logger.debug("Sending second inactivity reminder to {} participants", inactiveUsers.size());
      sendTextMessage(inactiveUsers, message, message2);
    }
    else {
      logger.debug("No user to send second inactivity reminders");
    }
  }

  private void sendTextMessage(final List<User> inactiveUsers, final Message message, final String textMessage) {
    final String messageFormat = String.format("%s\n%s", message.getContent(), textMessage);
    for (User inactiveUser : inactiveUsers) {
      final String token = UUID.randomUUID().toString();
      String url = updateUserToken(inactiveUser, token);
      final String textMessageBody = String.format(messageFormat, url);
      final String phoneNumber = userRepository.getPhoneNumber(inactiveUser);
      executorService.execute(() -> {
        logger.debug("Sending inactivity reminder to participant {} with phone: {}", inactiveUser.getUsername(), phoneNumber);
        textMessageClient.sendTextMessage(textMessageBody, Arrays.asList(phoneNumber));
      });
    }
  }

  private void sendThirdReminder(final LocalDate today) {
    final LocalDate lastLoginDate = today.minusDays(thirdNotificationDifference);
    final List<User> inactiveUsers = getLastLoggedInOn(lastLoginDate);
    inactiveUsers.forEach((user) -> sendEmailSafely(subject1, email1, user, "third"));
  }

  private void sendFourthReminder(final LocalDate today) {
    final LocalDate lastLoginDate = today.minusDays(fourthNotificationDifference);
    final List<User> inactiveUsers = getLastLoggedInOn(lastLoginDate);
    inactiveUsers.forEach((user) -> sendEmailSafely(subject2, email2, user, "fourth"));
  }

  private void sendInactiveUsersList(final LocalDate today) {
    final LocalDate lastLoginDate = today.minusDays(inactiveWarningDate);
    final List<User> inactiveUsers = getLastLoggedInOn(lastLoginDate);

    if(CollectionUtils.isNotEmpty(inactiveUsers)) {
      logger.debug("Sending inactive user list to admin: {} ", warningEmailAddress);
      final String studyIds = prepareStudyIds(inactiveUsers);

      executorService.submit(() -> {
        try {
          final String body = String.format(inactiveEmailTemplate, LocalDate.now().toString(), studyIds);
          mailClient.send(warningEmailAddress, warningEmailSubject, body);
        } catch (MessagingException e) {
          logger.warn("Failed sending warning email notification to: " + warningEmailAddress, e);
        }
      });
    }
    else {
      logger.debug("No inactive user found to notify admin");
    }
  }

  private void sendEightMonthReminder(final LocalDate today) {
    final LocalDate startDate = today.minusDays(chooseGroupDifference);
    final List<User> participants = userRepository.findUsersBy(
        DateTimeUtils.toDate(startDate),
        GiftCardNotifier.WAIT_LIST_GROUP
    );

    if(CollectionUtils.isNotEmpty(participants)) {
      logger.debug("Sending eight month reminder to admin with start date: {}", startDate);
      final String studyIds = prepareStudyIds(participants);
      final String body = String.format(chooseGroupEmail, studyIds);

      executorService.submit(() -> {
        try {
          mailClient.send(warningEmailAddress, chooseGroupSubject, body);
        } catch (MessagingException e) {
          logger.warn("Failed sending end of study email notification to: " + warningEmailAddress, e);
        }
      });
    }
    else {
      logger.debug("No user found to remind admin for eight month study with start date : {}", startDate);
    }
  }

  private void sendEndOfStudyReminder(final LocalDate today) {
    List<String> roles = new ArrayList<>(GiftCardNotifier.REGULAR_GROUP);
    roles.addAll(GiftCardNotifier.WAIT_LIST_GROUP);
    final LocalDate startDate = today.minusDays(studyEndDifference);
    final List<User> participants = userRepository.findUsersBy(DateTimeUtils.toDate(startDate), roles);

    if(CollectionUtils.isNotEmpty(participants)) {
      logger.debug("Sending end of study reminder to admin with start date: {}", startDate);

      final String studyIds = prepareStudyIds(participants);
      final String body = String.format(studyEndEmail, studyIds);

      executorService.submit(() -> {
        try {
          mailClient.send(warningEmailAddress, studyEndSubject, body);
        } catch (MessagingException e) {
          logger.warn("Failed sending end of study email notification to: " + warningEmailAddress, e);
        }
      });
    }
    else {
      logger.debug("No user found to remind admin for end of study with start date : {}", startDate);
    }
  }

  private String prepareStudyIds(final List<User> users) {
    StringBuilder emailMessageBuilder = new StringBuilder();
    users.forEach((user) -> emailMessageBuilder.append(user.getId()).append("\n"));
    return emailMessageBuilder.toString();
  }

  private void sendEmailSafely(final String subject, final String email, final User user, final String warningName) {
    final String token = UUID.randomUUID().toString();
    String url = updateUserToken(user, token);
    executorService.submit(() -> {
      try {
        logger.debug("Sending {} reminder to participant with study_id: {} email: {}", warningName, user.getUsername(), user.getEmail());
        mailClient.send(user.getEmail(), subject, String.format(email, userRepository.getName(user), url));
      } catch (MessagingException e) {
        logger.warn("Failed sending email notification to " + user.getEmail() + " for user: " + user.getId(), e);
      }
    });
  }

  private String updateUserToken(final User user, final String token) {
    user.setResetToken(token);
    userRepository.update(user);
    return String.format("%s/profile/resetlogin/%d/%s", baseUrl, user.getId(), user.getResetToken());
  }

  private List<User> getLastLoggedInOn(final LocalDate lastLoginDate) {
    final Date lastLogin = DateTimeUtils.toDate(lastLoginDate);

    final List<User> inactiveUsers = userRepository.getInactiveUsers(lastLogin);
    logger.debug("Found total inactive users: {}", inactiveUsers.size());
    return inactiveUsers;
  }

  private Message retrieveRandomMessage() {
    final int totalMessage = messageRepository.getTotalMessage();
    final int id = random.nextInt(totalMessage) + 1;
    final Message message = messageRepository.find(id);
    logger.debug("Sending notification using message: {}", message);
    return message;
  }

}
