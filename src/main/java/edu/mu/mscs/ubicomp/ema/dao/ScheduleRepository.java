package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.Schedule;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class ScheduleRepository {
  public static final String SELECT_SCHEDULE = "SELECT s FROM Schedule s WHERE s.surveyDate = :today " +
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

}
