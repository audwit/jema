package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.Schedule;
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
public class ScheduleRepository {
  public static final String SELECT_SCHEDULE = "SELECT s FROM Schedule s JOIN s.user u " +
      "WHERE s.surveyDate = :today " +
      "AND s NOT IN (SELECT n.schedule FROM Notification n JOIN n.schedule ss WHERE ss.surveyDate = :today )";

  @PersistenceContext
  private EntityManager entityManager;

  public List<Schedule> findSchedule(final Date date) {
    final TypedQuery<Schedule> query = entityManager.createQuery(
        SELECT_SCHEDULE,
        Schedule.class
    ).setParameter("today", date);

    return query.getResultList();
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int findTotalDenied(final User user, final Date startDate, final Date endDate) {
    final String totalDenied = "SELECT count(s) as total FROM Schedule s " +
        "where s.denied = true and s.user = :user and s.surveyDate between :startDate and :endDate";
    final Query query = entityManager.createQuery(totalDenied)
        .setParameter("user", user)
        .setParameter("startDate", startDate)
        .setParameter("endDate", endDate);

    final Long singleResult = (Long) query.getSingleResult();
    return singleResult.intValue();
  }
}
