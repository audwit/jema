package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.User;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserRepository {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @PersistenceContext
  private EntityManager entityManager;

  public List<User> getInactiveUsers(final Date lastLoginDate, final List<String> roles) {
    final String findUser = "FROM User u " +
        "WHERE DATE(u.lastLogin) = :lastLoginDate " +
        "AND u.active = true " +
        "AND u.role in (:roles) " +
        "AND u.startDate > '2000-00-00 00:00:00'";
    final TypedQuery<User> query = entityManager.createQuery(findUser, User.class)
        .setParameter("lastLoginDate", lastLoginDate, TemporalType.DATE)
        .setParameter("roles", roles);
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

  public List<Integer[]> getSubmittedSurveyTotal(final int month, final Date startDate) {
    final String countSurveyCompletionQuery = "select study_id, survey_id, count(survey_id) from completion " +
        "where time_stamp = :month " +
        "and survey_id in (SELECT survey_id FROM schedule WHERE threemonths = 1 ORDER BY survey_id) " +
//        "and study_id in (select u.name from user u) " +
        "and study_id in (select u.name from user u where date(u.start_date) = :startDate) " +
        "group by study_id, survey_id " +
        "order by survey_id";


    final Query query = entityManager.createNativeQuery(countSurveyCompletionQuery)
        .setParameter("startDate", startDate, TemporalType.DATE)
        .setParameter("month", month);
    final List<Object[]> resultList = query.getResultList();
    return resultList.stream().map(row -> new Integer[]{
        Integer.valueOf(row[0].toString()),
        Integer.valueOf(row[1].toString()),
        Integer.valueOf(row[2].toString())
    })
    .collect(Collectors.toList());
  }

  public List<User> getRequiresNotificationUsers(Date startDate, String month) {
    final List<User> regularUsers = getRegularUsers(startDate, month, "'Boning Up Group', 'SS Study Group'", 17);
    final List<User> choiceGroupUsers = getRegularUsers(startDate, month, "'Personal Choice Group'", 16);
    final LinkedList<User> users = new LinkedList<>(regularUsers);
    users.addAll(choiceGroupUsers);
    return users;
  }

  private List<User> getRegularUsers(final Date startDate, final String month, final String roles, final int totalSurvey) {
    final String queryFormat = "select u.* " +
        "from user u " +
        "where u.role in (" + roles + ") " +
        "AND u.active = true " +
        "and date(u.start_date) = :startDate " +
        "and (select count(c.survey_id) from completion c where c.time_stamp= :month and c.study_id = u.name) < '" + totalSurvey + "' " +
        "order by u.name asc";

    final Query nativeQuery = entityManager.createNativeQuery(queryFormat, User.class)
        .setParameter("startDate", startDate, TemporalType.DATE)
        .setParameter("month", month);

    return nativeQuery.getResultList();
  }

  public List<User> findUsersBy(final Date startDate, final List<String> roles) {
    Validate.notEmpty(roles, "roles can not be empty");
    final String qlString = "SELECT u FROM User u " +
        "WHERE DATE(u.startDate) = :startDate " +
        "AND u.active = true " +
        "AND u.role in (:roles)";
    final TypedQuery<User> query = entityManager.createQuery(qlString, User.class)
        .setParameter("startDate", startDate, TemporalType.DATE)
        .setParameter("roles", roles);
    return query.getResultList();
  }
}
