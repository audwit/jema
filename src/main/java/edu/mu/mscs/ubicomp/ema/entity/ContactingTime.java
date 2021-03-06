package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.sql.Time;

@Entity
public class ContactingTime {
  private Integer id;
  private Time startTime;
  private User user;

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
  @Column(name = "startTime")
  public Time getStartTime() {
    return startTime;
  }

  public void setStartTime(final Time startTime) {
    this.startTime = startTime;
  }

  @OneToOne
  @JoinColumn(name = "userId")
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

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("startTime", startTime)
        .toString();
  }
}
