package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.Date;

@Repository
public class AnswerRepository {

  @PersistenceContext
  private EntityManager entityManager;


  public int findTotalAnswer(final User user, final Date start, final Date end) {
    final String totalAnswer = "SELECT distinct (a.submissionTime) FROM Answer a " +
        "where a.user = :user and a.submissionTime between :startDateTime and :endDateTime";
    final TypedQuery<Date> query = entityManager.createQuery(totalAnswer, Date.class)
        .setParameter("user", user)
        .setParameter("startDateTime", start, TemporalType.DATE)
        .setParameter("endDateTime", end, TemporalType.DATE);

    return query.getResultList().size();
  }
}
