package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
public class UserRepository {
  private static final String QL_STRING = "FROM User u WHERE u.lastLogin >= :lastLoginStart AND u.lastLogin <= :lastLoginEnd";

  @PersistenceContext
  private EntityManager entityManager;

  public List<User> getInactiveUsers(final Date lastLoginStart, final Date lastLoginEnd) {
    final TypedQuery<User> query = entityManager.createQuery(QL_STRING, User.class)
        .setParameter("lastLoginStart", lastLoginStart)
        .setParameter("lastLoginEnd", lastLoginEnd);
    return query.getResultList();
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void update(final User user) {
    entityManager.merge(user);
    entityManager.flush();
  }

  public String getPhoneNumber(User user) {
    final Query query = entityManager.createNativeQuery("SELECT b.studyphonenumber FROM baseline b left join user u ON u.name = b.study_id " +
        "where u.id = ?")
        .setParameter(1, user.getId());

    final Object[] singleResult = (Object[]) query.getSingleResult();
    return singleResult == null ? null : singleResult[0].toString();
  }

  public String getName(final User user) {
    final Query query = entityManager.createNativeQuery("SELECT ec.firstname as firstName, ec.lastname as lastName " +
        "FROM enrollment_contact ec " +
        "left join enrolled_user eu ON eu.screening_id = ec.screening_id " +
        "left join user u ON u.name = eu.study_id " +
        "where u.id = ?")
        .setParameter(1, user.getId());

    final String name;
    final Object[] singleResult = (Object[]) query.getSingleResult();
    if(singleResult != null) {
      name = singleResult[0] + " " + singleResult[1];
    }
    else {
      name = user.getUsername();
    }
    return name;
  }
}
