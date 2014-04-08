package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.client.ClickATellClient;
import edu.mu.mscs.ubicomp.ema.dao.MessageRepository;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.Message;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ReminderService {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private Random random = new Random();

  @Autowired
  @Value("${notification.dummyNumber}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String dummyNumber;
  @Autowired
  private ClickATellClient client;
  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private UserRepository userRepository;

  public void sendNotifications() {
    final LocalDate today = LocalDate.now();
    sendFirstReminder(today);
    sendSecondReminder(today);
  }

  private void sendFirstReminder(final LocalDate today) {
    final List<User> inactiveUsers = getInactiveUsersSince(today, 2, 1);
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message);
  }

  private void sendSecondReminder(final LocalDate today) {
    final List<User> inactiveUsers = getInactiveUsersSince(today, 3, 2);
    final Message message = retrieveRandomMessage();
    sendTextMessage(inactiveUsers, message);
  }

  private void sendTextMessage(final List<User> inactiveUsers, final Message message) {
    final String textMessage = message.getContent();
    final List<String> phoneNumbers = inactiveUsers.stream()
        .map(user -> dummyNumber)
        .collect(Collectors.toList());
    client.sendTextMessage(textMessage, phoneNumbers);
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
