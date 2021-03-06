package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "user")
public class User {
  private Integer id;
  private String username;
  private String email;
  private String password;
  private String role;
  private String previousRole;
  private String resetToken;
  private ContactingTime contactingTime;
  private Collection<Answer> answers;
  private Collection<Schedule> schedules;
  private Timestamp lastLogin;
  private Timestamp startDate;
  private Boolean active;

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

  @Basic
  @Column(name = "previous_role")
  public String getPreviousRole() {
    return previousRole;
  }

  public void setPreviousRole(final String previousRole) {
    this.previousRole = previousRole;
  }

  @Basic
  @Column(name = "reset_token")
  public String getResetToken() {
    return resetToken;
  }

  public void setResetToken(final String token) {
    this.resetToken = token;
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

  @Basic
  @Column(name = "start_date")
  public Timestamp getStartDate() {
    return startDate;
  }

  public void setStartDate(final Timestamp startDate) {
    this.startDate = startDate;
  }

  @Basic
  @Column(name = "active", nullable = false, columnDefinition = "TINYINT(1)")
  public Boolean getActive() {
    return active;
  }

  public void setActive(final Boolean active) {
    this.active = active;
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
        .append("username", username)
        .append("email", email)
        .toString();
  }
}
