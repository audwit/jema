package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Answer")
public class Answer {
  private Integer id;
  private Integer answer;
  private Timestamp submissionTime;
  private Integer userId;
  private Integer questionId;
  private Integer scheduleId;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "answer")
  public Integer getAnswer() {
    return answer;
  }

  public void setAnswer(final Integer answer) {
    this.answer = answer;
  }

  @Basic
  @Column(name = "submissionTime")
  public Timestamp getSubmissionTime() {
    return submissionTime;
  }

  public void setSubmissionTime(final Timestamp submissionTime) {
    this.submissionTime = submissionTime;
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
  @Column(name = "questionId")
  public Integer getQuestionId() {
    return questionId;
  }

  public void setQuestionId(final Integer questionId) {
    this.questionId = questionId;
  }

  @Basic
  @Column(name = "scheduleId")
  public Integer getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(final Integer scheduleId) {
    this.scheduleId = scheduleId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Answer that = (Answer) o;

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
