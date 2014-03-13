package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Time;

@Entity
@Table(name = "ContactingTime")
public class ContactingTime {
  private Integer id;
  private Time startTime;
  private Time endTime;
  private Integer userId;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
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
  @Column(name = "endTime")
  public Time getEndTime() {
    return endTime;
  }

  public void setEndTime(final Time endTime) {
    this.endTime = endTime;
  }

  @Basic
  @Column(name = "userId")
  public Integer getUserId() {
    return userId;
  }

  public void setUserId(final Integer userId) {
    this.userId = userId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ContactingTime that = (ContactingTime) o;

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
