package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Collection;

@Entity
public class Question {
  private Integer id;
  private String title;
  private Integer activityId;
  private Collection<Answer> answers;
  private Activity activity;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "title")
  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  @Basic
  @Column(name = "activityId")
  public Integer getActivityId() {
    return activityId;
  }

  public void setActivityId(final Integer activityId) {
    this.activityId = activityId;
  }

  @OneToMany(mappedBy = "question")
  public Collection<Answer> getAnswers() {
    return answers;
  }

  public void setAnswers(final Collection<Answer> answersById) {
    this.answers = answersById;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activityId", referencedColumnName = "id", nullable = false)
  public Activity getActivity() {
    return activity;
  }

  public void setActivity(final Activity activityByActivityId) {
    this.activity = activityByActivityId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Question that = (Question) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(that))
        .append(id, that.id)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 11)
        .append(id)
        .hashCode();
  }
}
