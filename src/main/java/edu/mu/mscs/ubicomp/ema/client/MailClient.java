package edu.mu.mscs.ubicomp.ema.client;

import com.sun.mail.smtp.SMTPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.Date;
import java.util.Properties;

public class MailClient {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private String host;
  private String username;
  private String password;
  private String from;
  private Properties properties;

  public void setHost(final String host) {
    this.host = host;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setFrom(final String from) {
    this.from = from;
  }

  public void setProperties(final Properties properties) {
    this.properties = properties;
  }

  @PostConstruct
  private void init() {
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

    properties.setProperty("mail.smtps.auth", "true");
    properties.put("mail.smtps.quitwait", "false");
    properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    properties.setProperty("mail.smtp.socketFactory.fallback", "false");
  }

  /**
   * Send email using GMail SMTP server.
   *
   * @param recipientEmail TO recipient
   * @param title title of the message
   * @param message message to be sent
   * @throws javax.mail.internet.AddressException if the email address parse failed
   * @throws javax.mail.MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
   */
  public void send(String recipientEmail, String title, String message) throws MessagingException {
    logger.debug("Sending email to: {}", recipientEmail);
    Session session = Session.getInstance(properties, null);
    final MimeMessage msg = new MimeMessage(session);

    msg.setFrom(new InternetAddress(from));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

    msg.setSubject(title);
    msg.setText(message, "utf-8");
    msg.setSentDate(new Date());

    SMTPTransport smtpTransport = (SMTPTransport)session.getTransport("smtps");

    smtpTransport.connect(host, username, password);
    smtpTransport.sendMessage(msg, msg.getAllRecipients());
    smtpTransport.close();
  }
}
