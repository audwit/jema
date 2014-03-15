package edu.mu.mscs.ubicomp.ema.client;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
public class ClickATellClient {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${clickATell.baseApiUri}")
  private String baseApiUri;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${clickATell.username}")
  private String username;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${clickATell.password}")
  private String password;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${clickATell.apiId}")
  private String apiId;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired(required = true)
  @Value("${clickATell.from}")
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
    if(StringUtils.isBlank(textMessage)) {
      throw new IllegalArgumentException("textMessage should not be null or empty");
    }
    if(CollectionUtils.isEmpty(phoneNumbers)) {
      logger.debug("Not sending any message, no phone numbers given.");
    }

    final String numbers = StringUtils.join(phoneNumbers, ",");
    final String requestBody = requestTemplate
        .replace("TEXT", textMessage)
        .replace("TO", numbers);

    logger.debug("Sending total {} notification using: {}", phoneNumbers.size(), textMessage);
    logger.debug("Request body: {}", requestBody);
  }

}
