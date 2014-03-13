package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "User")
public class User {
  private String email;
  private String password;
  private String role;
  private Integer id;

  @Basic
  @Column(name = "Email")
  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  @Basic
  @Column(name = "Password")
  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Basic
  @Column(name = "Role")
  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  @Id
  @Column(name = "Study_id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
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
    return new HashCodeBuilder(17, 31)
        .append(id)
        .hashCode();
  }
}
