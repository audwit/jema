package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.Notification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class NotificationRepository {
  public static final String QL_STRING = "FROM Notification n JOIN n.schedule s WHERE s.surveyDate = :date AND n.scheduledTime = :time";

  @PersistenceContext
  private EntityManager entityManager;

  public void persistAll(final List<Notification> notifications) {
    for (Notification notification : notifications) {
      entityManager.persist(notification);
    }
  }

  public List<Notification> findNotifications(final Date date, final Date time) {
    final TypedQuery<Notification> query = entityManager.createQuery(QL_STRING, Notification.class)
        .setParameter("date", date)
        .setParameter("time", time);

    return query.getResultList();
  }
}
