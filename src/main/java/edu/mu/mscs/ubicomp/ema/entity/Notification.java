package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
public class Notification {
  private Integer id;
  private Timestamp scheduledTime;
  private Time sentTime;
  private Boolean sent;
  private Integer serial;
  private String status;
  private Schedule schedule;
  private Answer answer;

  @Id
  @GeneratedValue
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
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
  public Time getSentTime() {
    return sentTime;
  }

  public void setSentTime(final Time sentTime) {
    this.sentTime = sentTime;
  }

  @Basic
  @Column(name = "sent", nullable = false, columnDefinition = "TINYINT(1)")
  public Boolean getSent() {
    return sent;
  }

  public void setSent(final Boolean sent) {
    this.sent = sent;
  }

  @Basic
  @Column(name = "serial")
  public Integer getSerial() {
    return serial;
  }

  public void setSerial(final Integer serial) {
    this.serial = serial;
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

  @OneToOne(mappedBy = "notification")
  public Answer getAnswer() {
    return answer;
  }

  public void setAnswer(final Answer answer) {
    this.answer = answer;
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

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("scheduledTime", scheduledTime)
        .append("sentTime", sentTime)
        .append("sent", sent)
        .toString();
  }
}
