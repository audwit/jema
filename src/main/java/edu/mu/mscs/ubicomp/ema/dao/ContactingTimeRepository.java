package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.ContactingTime;
import edu.mu.mscs.ubicomp.ema.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Time;
import java.time.LocalTime;

@Repository
public class ContactingTimeRepository {
  @PersistenceContext
  private EntityManager entityManager;

  public ContactingTime addContactingTime(final User user) {
    if (user.getContactingTime() != null) {
      throw new RuntimeException("ContactTime already set for user with id: " + user.getId());
    }

    final ContactingTime contactingTime = new ContactingTime();
    contactingTime.setStartTime(Time.valueOf(LocalTime.of(10, 0, 0)));
    contactingTime.setUser(user);
    entityManager.persist(contactingTime);
    return contactingTime;
  }
}
