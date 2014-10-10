package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class NotificationRepository {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public static final String QL_STRING = "SELECT n FROM Notification n JOIN n.schedule s " +
      "WHERE n.scheduledTime = :scheduledTime AND s.denied = false " +
      "and s not in (select distinct a.schedule from Answer a where a.schedule = n.schedule)";

  @PersistenceContext
  private EntityManager entityManager;

  public void persistAll(final List<Notification> notifications) {
    notifications.forEach(entityManager::persist);
  }

  public List<Notification> findNotifications(final Date scheduledTime) {
    final TypedQuery<Notification> query = entityManager.createQuery(QL_STRING, Notification.class)
        .setParameter("scheduledTime", scheduledTime);

    logger.debug("Find notifications with scheduledTime: {}", scheduledTime);
    return query.getResultList();
  }

  public void markAsSent(final List<Notification> notifications) {
    notifications.forEach((notification) -> {
      notification.setSent(true);
      entityManager.merge(notification);
    });

    entityManager.flush();
  }
}
