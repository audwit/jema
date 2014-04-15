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
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

  private ClickATellClient textMessageClient;
  private MailClient mailClient;
  private MessageRepository messageRepository;
  private UserRepository userRepository;

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

  public void sendNotifications() {
    final LocalDate today = LocalDate.now();
    sendFirstReminder(today);
    sendSecondReminder(today);
    sendThirdReminder(today);
    sendFourthReminder(today);
  }

  private void sendFirstReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusWeeks(1));
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message.getContent() + "\n" + message1);
  }

  private void sendSecondReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusWeeks(1).minusDays(1));
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message.getContent() + "\n" + message2);
  }

  private void sendTextMessage(final List<User> inactiveUsers, final String textMessage) {
    final List<String> phoneNumbers = inactiveUsers.stream()
        .map(user -> dummyNumber)
        .collect(Collectors.toList());
    textMessageClient.sendTextMessage(textMessage, phoneNumbers);
  }

  private void sendThirdReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusWeeks(1).minusDays(2));
    inactiveUsers.forEach((user)-> sendEmailSafely(subject1, String.format(email1, user.getUsername()), user));
  }

  private void sendFourthReminder(final LocalDate today) {
    final List<User> inactiveUsers = getLastLoggedInOn(today.minusWeeks(1).minusDays(3));
    inactiveUsers.forEach((user)-> sendEmailSafely(subject2, String.format(email2, user.getUsername()), user));
  }

  private void sendEmailSafely(final String subject, final String email, final User user) {
    try {
      mailClient.send(user.getEmail(),  subject, String.format(email, user.getUsername()));
    } catch (MessagingException e) {
      logger.warn("Failed sending email notification.", e);
    }
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
