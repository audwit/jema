package edu.mu.mscs.ubicomp.ema.client;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClickATellClient {
  public static final String REQUEST_BODY_KEY = "data";
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  @Value("${clickATell.baseApiUri}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String baseApiUri;
  @Autowired
  @Value("${clickATell.username}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String username;
  @Autowired
  @Value("${clickATell.password}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String password;
  @Autowired
  @Value("${clickATell.apiId}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String apiId;
  @Autowired
  @Value("${clickATell.from}")
  @SuppressWarnings("SpringJavaAutowiringInspection")
  private String from;

  private String requestTemplate;

  @PostConstruct
  public void initialize() throws IOException {
    requestTemplate = IOUtils.toString(getClass().getResourceAsStream("/sendMsg.template.xml"))
        .replace("USERNAME", username)
        .replace("PASSWORD", password)
        .replace("API_ID", apiId)
        .replace("FROM", from);
  }

  public void sendTextMessage(final String textMessage, final List<String> phoneNumbers) {
    sendTextMessage(textMessage, phoneNumbers, null);
  }

  public void sendTextMessage(final String textMessage, final List<String> phoneNumbers, String sequenceNo) {
    if (StringUtils.isBlank(textMessage)) {
      throw new IllegalArgumentException("textMessage should not be null or empty");
    }
    final List<String> filteredNumbers = phoneNumbers.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(filteredNumbers)) {
      logger.debug("Not sending any message, no phone numbers given.");
      return;
    }
    if (StringUtils.isEmpty(sequenceNo)) {
      sequenceNo = LocalDateTime.now().toString();
    }

    final String numbers = String.join(",", filteredNumbers);
    final String requestBody = requestTemplate
        .replace("TEXT", textMessage)
        .replace("TO", numbers)
        .replace("SEQUENCE_NO", sequenceNo);

    logger.debug("Sending total {} notification using: {}", filteredNumbers.size(), textMessage);
    logger.debug("Request body: \n{}", requestBody);
    sendInternal(requestBody);
  }

  private void sendInternal(final String requestBody) {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = createRequest(requestBody);
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        final int statusCode = response.getStatusLine().getStatusCode();
        final HttpEntity entity = response.getEntity();
        final String responseString = IOUtils.toString(entity.getContent());
        logger.debug("Response: \n" + responseString);
        EntityUtils.consume(entity);
        if (statusCode > 300) {
          throw new RuntimeException("Failed to send text message. Response status code: " + statusCode);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to send text message" + e.getMessage(), e);
    }
  }

  private HttpPost createRequest(final String requestBody) throws UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(baseApiUri);
    List<NameValuePair> nameValuePairs = new ArrayList<>();
    nameValuePairs.add(new BasicNameValuePair(REQUEST_BODY_KEY, requestBody));
    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    return httpPost;
  }

}
