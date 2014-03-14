package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class Notification {
  private Integer id;
  private Integer scheduleId;
  private Timestamp scheduledTime;
  private Timestamp sentTime;
  private Boolean sent;
  private String status;
  private Schedule schedule;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "scheduleId")
  public Integer getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(final Integer scheduleId) {
    this.scheduleId = scheduleId;
  }

  @Basic
  @Column(name = "scheduledTime")
  public Timestamp getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(final Timestamp scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  @Basic
  @Column(name = "sentTime")
  public Timestamp getSentTime() {
    return sentTime;
  }

  public void setSentTime(final Timestamp sentTime) {
    this.sentTime = sentTime;
  }

  @Basic
  @Column(name = "sent")
  public Boolean getSent() {
    return sent;
  }

  public void setSent(final Boolean sent) {
    this.sent = sent;
  }

  @Basic
  @Column(name = "status")
  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scheduleId", referencedColumnName = "id", nullable = false)
  public Schedule getSchedule() {
    return schedule;
  }

  public void setSchedule(final Schedule scheduleByScheduleId) {
    this.schedule = scheduleByScheduleId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Notification that = (Notification) o;

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
