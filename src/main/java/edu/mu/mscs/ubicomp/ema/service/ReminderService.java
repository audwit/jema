package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.dao.MessageRepository;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.Message;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ReminderService {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private Random random = new Random();

  private String dummyNumber;
  private String message1;
  private String message2;
  private ClickATellClient textMessageClient;
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

  public void setTextMessageClient(final ClickATellClient textMessageClient) {
    this.textMessageClient = textMessageClient;
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
  }

  private void sendFirstReminder(final LocalDate today) {
    final List<User> inactiveUsers = getInactiveUsersSince(today, 2, 1);
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message.getContent() + "\n" + message1);
  }

  private void sendSecondReminder(final LocalDate today) {
    final List<User> inactiveUsers = getInactiveUsersSince(today, 3, 2);
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message.getContent() + "\n" + message2);
  }

  private void sendTextMessage(final List<User> inactiveUsers, final String textMessage) {
    final List<String> phoneNumbers = inactiveUsers.stream()
        .map(user -> dummyNumber)
        .collect(Collectors.toList());
    textMessageClient.sendTextMessage(textMessage, phoneNumbers);
  }

  private List<User> getInactiveUsersSince(final LocalDate today, final int lastLoginStartWeek, final int lastLoginEndWeek) {
    final LocalDate lastLoginStart = today.minusWeeks(lastLoginStartWeek);
    final LocalDate lastLoginEnd = today.minusWeeks(lastLoginEndWeek);
    final List<User> inactiveUsers = userRepository.getInactiveUsers(
        DateTimeUtils.toDate(lastLoginStart),
        DateTimeUtils.toDate(lastLoginEnd)
    );
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
