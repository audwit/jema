package edu.mu.mscs.ubicomp.ema.service;

import edu.mu.mscs.ubicomp.ema.dao.AnswerRepository;
import edu.mu.mscs.ubicomp.ema.dao.ScheduleRepository;
import edu.mu.mscs.ubicomp.ema.dao.UserRepository;
import edu.mu.mscs.ubicomp.ema.entity.User;
import edu.mu.mscs.ubicomp.ema.util.DateTimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GiftCardNotifier {
  public static final String REQUEST_BODY_KEY = "users";
  public static final int DAYS_PER_ROUND = 28;
  public static final double THRESHOLD = 75;
  public static final List<String> REGULAR_GROUP = Arrays.asList("SS Study Group", "Boning Up Group");
  public static final List<String> WAIT_LIST_GROUP = Arrays.asList("Personal Choice Group");
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private ScheduleRepository scheduleRepository;
  private AnswerRepository answerRepository;
  private UserRepository userRepository;
  private String amazonURI;
  private int totalThread;
  private ExecutorService executorService;

  public void setScheduleRepository(final ScheduleRepository scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }

  public void setAnswerRepository(final AnswerRepository answerRepository) {
    this.answerRepository = answerRepository;
  }

  public void setUserRepository(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void setAmazonURI(final String amazonURI) {
    this.amazonURI = amazonURI;
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

  public void sendGiftCard() {
//    sendGiftCard(3, 3, 196, 25, REGULAR_GROUP);
//    sendGiftCard(13, 10, 84, 15, REGULAR_GROUP);
//    sendGiftCard(10, 10, 84, 15, WAIT_LIST_GROUP);
//    sendGiftCard(13, 3, 196, 25, WAIT_LIST_GROUP);
  }

  private void sendGiftCard(final int completedRound, final int giftCardRoundLength, final int totalSchedule, final int amount, final List<String> roles) {
    final LocalDate today = LocalDate.now();
    final LocalDate startDate = today.minusDays(DAYS_PER_ROUND * completedRound + 1);
    final LocalDate start = today.minusDays(DAYS_PER_ROUND * giftCardRoundLength);
    final LocalDate end = today.minusDays(1);
    sendGiftCard(startDate, start, end, roles, totalSchedule, amount);
  }

  private void sendGiftCard(
      final LocalDate studyStartDate,
      final LocalDate startDate,
      final LocalDate endDate,
      final List<String> roles,
      final int totalSchedule,
      final int amount) {
    final List<User> users = userRepository.findUsersBy(DateTimeUtils.toDate(studyStartDate), roles);

    List<User> eligibleUsers = new LinkedList<>();
    for (User user : users) {
      final int totalAnswered = getTotalAnswered(user, startDate, endDate);
      final int totalDenied = getTotalDenied(user, startDate, endDate);
      final int total = totalAnswered + totalDenied;

      final Double completion = (total * 100) / (double) totalSchedule;
      if (completion.compareTo(THRESHOLD) >= 0) {
        eligibleUsers.add(user);
      }
    }

    if(CollectionUtils.isNotEmpty(eligibleUsers)) {
      logger.debug("Sending amazon gift card of amount: {}, studyStartDate: {}, startDate: {}, endDate: {}",
          amount, studyStartDate, startDate, endDate);
      logger.debug("Sending gift card to: {}", users);
      sendRequest(eligibleUsers, amount);
    }
    else {
      logger.debug("Found no participants eligible for amazon gift card.");
    }
  }

  private int getTotalDenied(final User user, final LocalDate startDate, final LocalDate endDate) {
    final Date start = DateTimeUtils.toDate(startDate);
    final Date end = DateTimeUtils.toDate(endDate);
    return scheduleRepository.findTotalDenied(user, start, end);
  }

  private int getTotalAnswered(final User user, final LocalDate startDate, final LocalDate endDate) {
    final Date start = DateTimeUtils.toDate(LocalDateTime.of(startDate, LocalTime.MIN));
    final Date end = DateTimeUtils.toDate(LocalDateTime.of(endDate, LocalTime.MAX));
    return answerRepository.findTotalAnswer(user, start, end);
  }

  private void sendRequest(final List<User> users, final int amount) {
    final String requestBody = prepareRequestBody(users, amount);
    executorService.submit(() -> {
      try {
        sendInternal(requestBody);
      }
      catch (Exception e) {
        logger.warn("Failed to send gift card request. Request body: " + requestBody, e);
      }
    });
  }

  private String prepareRequestBody(final List<User> users, final int amount) {
    List<String> giftCards = new ArrayList<>();
    for (User user : users) {
      String giftCardJson = "{\"userId\": " + user.getId() + ", \"amount\": " + amount + "}";
      giftCards.add(giftCardJson);
    }

    return "[" + String.join(",", giftCards) + "]";
  }

  private void sendInternal(final String requestBody) throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      logger.debug("Sending gift card request with: {}", requestBody);
      HttpPost httpPost = createRequest(requestBody);
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        final int statusCode = response.getStatusLine().getStatusCode();
        final HttpEntity entity = response.getEntity();
        final String responseString = IOUtils.toString(entity.getContent());
        logger.debug("Gift card request feedback: \n" + responseString);
        EntityUtils.consume(entity);
        if (statusCode > 300) {
          logger.warn("Failed to send gift card request. Response status code: " + statusCode);
          throw new RuntimeException("Failed to send gift card request. Response status code: " + statusCode);
        }
      }
    }
  }

  private HttpPost createRequest(final String requestBody) throws UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(amazonURI);
    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(new BasicNameValuePair(REQUEST_BODY_KEY, requestBody));
    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    return httpPost;
  }

  @PreDestroy
  public void destroy() {
    logger.debug("Destroying executor service");
    final List<Runnable> jobs = executorService.shutdownNow();
    logger.debug("Destroyed executor service. Killed total jobs: " + jobs.size());
  }

}
