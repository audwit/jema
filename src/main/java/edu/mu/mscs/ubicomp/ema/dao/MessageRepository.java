package edu.mu.mscs.ubicomp.ema.dao;

import edu.mu.mscs.ubicomp.ema.entity.Message;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;

@Repository
public class MessageRepository {
  @PersistenceContext
  private EntityManager entityManager;

  public Message find(final int id) {
    return entityManager.find(Message.class, id);
  }

  public int getTotalMessage() {
    Query countQuery = entityManager.createNativeQuery("select count(*) from Message");
    final BigInteger totalMessage = (BigInteger) countQuery.getSingleResult();
    return totalMessage.intValue();
  }

}
