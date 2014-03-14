package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.Notification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class NotificationRepository {

  @PersistenceContext
  private EntityManager entityManager;

  public void persistAll(final List<Notification> notifications) {
    for (Notification notification : notifications) {
      entityManager.persist(notification);
    }
  }

}
