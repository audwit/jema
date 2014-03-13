package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name = "Schedule")
public class Schedule {
  private Integer id;
  private Byte active;
  private Date startDate;
  private Time startTime;
  private Date endDate;
  private Time endTime;
  private Byte firstNotificationSent;
  private Byte secondNotificationSent;
  private Byte thirdNotificationSent;
  private Byte noResponse;
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
  @Column(name = "active")
  public Byte getActive() {
    return active;
  }

  public void setActive(final Byte active) {
    this.active = active;
  }

  @Basic
  @Column(name = "startDate")
  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(final Date startDate) {
    this.startDate = startDate;
  }

  @Basic
  @Column(name = "startTime")
  public Time getStartTime() {
    return startTime;
  }

  public void setStartTime(final Time startTime) {
    this.startTime = startTime;
  }

  @Basic
  @Column(name = "endDate")
  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(final Date endDate) {
    this.endDate = endDate;
  }

  @Basic
  @Column(name = "endTime")
  public Time getEndTime() {
    return endTime;
  }

  public void setEndTime(final Time endTime) {
    this.endTime = endTime;
  }

  @Basic
  @Column(name = "firstNotificationSent")
  public Byte getFirstNotificationSent() {
    return firstNotificationSent;
  }

  public void setFirstNotificationSent(final Byte firstNotificationSent) {
    this.firstNotificationSent = firstNotificationSent;
  }

  @Basic
  @Column(name = "secondNotificationSent")
  public Byte getSecondNotificationSent() {
    return secondNotificationSent;
  }

  public void setSecondNotificationSent(final Byte secondNotificationSent) {
    this.secondNotificationSent = secondNotificationSent;
  }

  @Basic
  @Column(name = "thirdNotificationSent")
  public Byte getThirdNotificationSent() {
    return thirdNotificationSent;
  }

  public void setThirdNotificationSent(final Byte thirdNotificationSent) {
    this.thirdNotificationSent = thirdNotificationSent;
  }

  @Basic
  @Column(name = "noResponse")
  public Byte getNoResponse() {
    return noResponse;
  }

  public void setNoResponse(final Byte noResponse) {
    this.noResponse = noResponse;
  }

  @JoinColumn(name = "userId")
  @ManyToOne(fetch = FetchType.LAZY)
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
    return new HashCodeBuilder(17, 31)
        .append(id)
        .hashCode();
  }

}
