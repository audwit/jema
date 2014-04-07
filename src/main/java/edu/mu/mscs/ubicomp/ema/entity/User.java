package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
public class User {
  private Integer id;
  private String username;
  private String email;
  private String password;
  private String role;
  private ContactingTime contactingTime;
  private Collection<Answer> answers;
  private Collection<Schedule> schedules;
  private Timestamp lastLogin;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer studyId) {
    this.id = studyId;
  }

  @Basic
  @Column(name = "name")
  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  @Basic
  @Column(name = "email")
  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  @Basic
  @Column(name = "password")
  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Basic
  @Column(name = "role")
  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  @OneToOne(mappedBy = "user")
  public ContactingTime getContactingTime() {
    return contactingTime;
  }

  public void setContactingTime(final ContactingTime contactingTime) {
    this.contactingTime = contactingTime;
  }

  @OneToMany(mappedBy = "user")
  public Collection<Answer> getAnswers() {
    return answers;
  }

  public void setAnswers(final Collection<Answer> answersByStudyId) {
    this.answers = answersByStudyId;
  }

  @OneToMany(mappedBy = "user")
  public Collection<Schedule> getSchedules() {
    return schedules;
  }

  public void setSchedules(final Collection<Schedule> schedulesByStudyId) {
    this.schedules = schedulesByStudyId;
  }

  @Basic
  @Column(name = "last_login")
  public Timestamp getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(final Timestamp lastLogin) {
    this.lastLogin = lastLogin;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final User that = (User) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(that))
        .append(id, that.id)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 31)
        .append(id)
        .hashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("email", email)
        .toString();
  }
}
