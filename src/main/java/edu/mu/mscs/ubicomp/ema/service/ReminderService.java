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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class ReminderService {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private Random random = new Random();

  @Autowired
  private ClickATellClient client;
  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private UserRepository userRepository;

  public void sendNotifications() {
    final LocalDate today = LocalDate.now();
    final List<User> inactiveUsers = getInactiveUsers(today);
    logger.debug("Found total inactive users: {}", inactiveUsers.size());

    final Message message = retrieveRandomMessage();
    logger.debug("Sending notification using message: {}", message);
  }

  private List<User> getInactiveUsers(final LocalDate today) {
    final LocalDate lastLoginStart = today.minusWeeks(2);
    final LocalDate lastLoginEnd = today.minusWeeks(1);
    return userRepository.getInactiveUsers(
        DateTimeUtils.toDate(lastLoginStart),
        DateTimeUtils.toDate(lastLoginEnd)
    );
  }

  private Message retrieveRandomMessage() {
    final int totalMessage = messageRepository.getTotalMessage();
    final int id = random.nextInt(totalMessage);
    return messageRepository.find(id);
  }

}
