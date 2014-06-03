package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.User;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
public class UserRepository {
  private static final String QL_STRING = "FROM User u WHERE u.lastLogin >= :lastLoginStart AND u.lastLogin <= :lastLoginEnd";
  private Logger logger = LoggerFactory.getLogger(getClass());

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

    try {
      final String phoneNumber = query.getSingleResult().toString();
      return phoneNumber.startsWith("+") ? phoneNumber : "+1" + phoneNumber;
    } catch (Exception ex) {
      logger.warn("Failed to retrieve phone number for user: " + user, ex);
      return null;
    }
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
    if (singleResult != null) {
      name = singleResult[0] + " " + singleResult[1];
    } else {
      name = user.getUsername();
    }
    return name;
  }

  public List<User> getRequiresNotificationUsers(String columnName, Date startDate) {
    final String queryFormat = "select u.* from `user` u where u.name not in (\n" +
        "  select c.study_id from `completion` c where c.survey_id in (\n" +
        "    select s.survey_id from `schedule` s where " + columnName + " = 1\n" +
        "  ))\n" +
        "and u.start_date = :startDate";
    final Query nativeQuery = entityManager.createNativeQuery(queryFormat, User.class)
        .setParameter("startDate", startDate, TemporalType.DATE);

    return nativeQuery.getResultList();
  }

  public List<User> findUsersBy(final Date startDate, final List<String> roles) {
    Validate.notEmpty(roles, "roles can not be empty");
    final String qlString = "SELECT u FROM User u WHERE u.startDate = :startDate and u.role in (:roles)";
    final TypedQuery<User> query = entityManager.createQuery(qlString, User.class)
        .setParameter("startDate", startDate, TemporalType.DATE)
        .setParameter("roles", roles);
    return query.getResultList();
  }
}
