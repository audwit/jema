package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Date;
import java.util.Collection;

@Entity
public class Schedule {
  private Integer id;
  private Integer userId;
  private Date surveyDate;
  private Boolean denied;
  private Collection<Answer> answers;
  private Collection<Notification> notifications;
  private User user;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "userId")
  public Integer getUserId() {
    return userId;
  }

  public void setUserId(final Integer userId) {
    this.userId = userId;
  }

  @Basic
  @Column(name = "surveyDate")
  public Date getSurveyDate() {
    return surveyDate;
  }

  public void setSurveyDate(final Date surveyDate) {
    this.surveyDate = surveyDate;
  }

  @Basic
  @Column(name = "denied")
  public Boolean getDenied() {
    return denied;
  }

  public void setDenied(final Boolean denied) {
    this.denied = denied;
  }

  @OneToMany(mappedBy = "schedule")
  public Collection<Answer> getAnswers() {
    return answers;
  }

  public void setAnswers(final Collection<Answer> answersById) {
    this.answers = answersById;
  }

  @OneToMany(mappedBy = "schedule")
  public Collection<Notification> getNotifications() {
    return notifications;
  }

  public void setNotifications(final Collection<Notification> notificationsById) {
    this.notifications = notificationsById;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", referencedColumnName = "Study_id", nullable = false)
  public User getUser() {
    return user;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Schedule that = (Schedule) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(that))
        .append(id, that.id)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 31)
        .append(id)
        .hashCode();
  }
}
