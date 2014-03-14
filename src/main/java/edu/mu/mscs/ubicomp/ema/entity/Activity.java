package edu.mu.mscs.ubicomp.ema.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Collection;

@Entity
public class Activity {
  private Integer id;
  private String uniqueName;
  private String title;
  private Collection<Question> questions;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "uniqueName")
  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName(final String uniqueName) {
    this.uniqueName = uniqueName;
  }

  @Basic
  @Column(name = "title")
  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  @OneToMany(mappedBy = "activity")
  public Collection<Question> getQuestions() {
    return questions;
  }

  public void setQuestions(final Collection<Question> questionsById) {
    this.questions = questionsById;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Activity that = (Activity) o;

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
