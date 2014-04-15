package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.client.MailClient;
import edu.mu.mscs.ubicomp.ema.dao.MessageRepository;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.Message;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class ReminderService {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private Random random = new Random();

  private String dummyNumber;
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

  private ClickATellClient textMessageClient;
  private MailClient mailClient;
  private MessageRepository messageRepository;
  private UserRepository userRepository;
  private String baseUrl;

  public void setDummyNumber(final String dummyNumber) {
    this.dummyNumber = dummyNumber;
  }

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

  public void sendNotifications() {
    final LocalDate today = LocalDate.now();
    sendFirstReminder(today);
    sendSecondReminder(today);
    sendThirdReminder(today);
    sendFourthReminder(today);
  }

  private void sendFirstReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusDays(firstNotificationDifference));
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message, message1);
  }

  private void sendSecondReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusDays(secondNotificationDifference));
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message, message1);
  }

  private void sendTextMessage(final List<User> inactiveUsers, final Message message, final String textMessage) {
    final String messageFormat = String.format("%s\n%s", message.getContent(), textMessage);
    for (User inactiveUser : inactiveUsers) {
      final String token = UUID.randomUUID().toString();
      updateUserToken(inactiveUser, token);
      String url = createResetUrl(inactiveUser);
      final String textMessageBody = String.format(messageFormat, url);
      textMessageClient.sendTextMessage(textMessageBody, Arrays.asList(dummyNumber));
    }
  }

  private void sendThirdReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusDays(thirdNotificationDifference));
    inactiveUsers.forEach((user)-> sendEmailSafely(subject1, String.format(email1, user.getUsername()), user));
  }

  private void sendFourthReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusDays(fourthNotificationDifference));
    inactiveUsers.forEach((user) -> sendEmailSafely(subject2, String.format(email2, user.getUsername()), user));
  }

  private void sendEmailSafely(final String subject, final String email, final User user) {
    final String token = UUID.randomUUID().toString();
    updateUserToken(user, token);
    String url = createResetUrl(user);
    try {
      mailClient.send(user.getEmail(),  subject, String.format(email, user.getUsername(), url));
    } catch (MessagingException e) {
      logger.warn("Failed sending email notification.", e);
    }
  }

  private String createResetUrl(final User user) {
    return String.format("%s/profile/resetlogin/%d/%s", baseUrl, user.getId(), user.getResetToken());
  }

  private void updateUserToken(final User user, final String token) {
    user.setResetToken(token);
    userRepository.update(user);
  }

  private List<User> getLastLoggedInOn(final LocalDate lastLoginDate) {
    final Date lastLoginStart = DateTimeUtils.toDate(LocalDateTime.of(lastLoginDate, LocalTime.MIN));
    final Date lastLoginEnd = DateTimeUtils.toDate(LocalDateTime.of(lastLoginDate, LocalTime.MAX));

    final List<User> inactiveUsers = userRepository.getInactiveUsers(lastLoginStart, lastLoginEnd);
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
