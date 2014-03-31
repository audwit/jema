package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class Answer {
  private Integer id;
  private Integer answer;
  private Timestamp submissionTime;
  private Question question;
  private User user;
  private Schedule schedule;
  private Notification notification;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "answer")
  public Integer getAnswer() {
    return answer;
  }

  public void setAnswer(final Integer answer) {
    this.answer = answer;
  }

  @Basic
  @Column(name = "submissionTime")
  public Timestamp getSubmissionTime() {
    return submissionTime;
  }

  public void setSubmissionTime(final Timestamp submissionTime) {
    this.submissionTime = submissionTime;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "questionId", referencedColumnName = "id", nullable = false)
  public Question getQuestion() {
    return question;
  }

  public void setQuestion(final Question questionByQuestionId) {
    this.question = questionByQuestionId;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", referencedColumnName = "Study_id", nullable = false)
  public User getUser() {
    return user;
  }

  public void setUser(final User userByUserId) {
    this.user = userByUserId;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scheduleId", referencedColumnName = "id")
  public Schedule getSchedule() {
    return schedule;
  }

  public void setSchedule(final Schedule schedule) {
    this.schedule = schedule;
  }

  @OneToOne
  @PrimaryKeyJoinColumn
  public Notification getNotification() {
    return notification;
  }

  public void setNotification(final Notification notification) {
    this.notification = notification;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Answer that = (Answer) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(that))
        .append(id, that.id)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
        .append(id)
        .hashCode();
  }
}
