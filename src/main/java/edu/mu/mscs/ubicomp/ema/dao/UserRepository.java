package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class UserRepository {
  private static final String QL_STRING = "FROM User u WHERE u.lastLogin > :lastLoginStart AND u.lastLogin < :lastLoginEnd";

  @PersistenceContext
  private EntityManager entityManager;

  public List<User> getInactiveUsers(final Date lastLoginStart, final Date lastLoginEnd) {
    final TypedQuery<User> query = entityManager.createQuery(QL_STRING, User.class)
        .setParameter("lastLoginStart", lastLoginStart)
        .setParameter("lastLoginEnd", lastLoginEnd);
    return query.getResultList();
  }

  public void update(final User user) {
    entityManager.merge(user);
    entityManager.flush();
  }
}
