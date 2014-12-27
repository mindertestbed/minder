package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
public class TestAssertion extends MinderEntity{
  @Column(nullable=false, unique = true)
  private String taId;

  @Column(nullable=false)
  private String normativeSource;

  @Column(nullable=false)
  private String target;

  private String prerequisites;

  @Column(nullable=false)
  private String predicate;

  private String variables;

  @OneToMany(cascade = CascadeType.ALL)
  @Basic(fetch = FetchType.LAZY)
  private List<TestAssertion> relatedTestAssertions;


  public List<TestAssertion> getRelatedTestAssertions() {
    return relatedTestAssertions;
  }

  public void setRelatedTestAssertions(List<TestAssertion> relatedTestAssertions) {
    this.relatedTestAssertions = relatedTestAssertions;
  }

  public String getTaId() {
    return taId;
  }

  public void setTaId(String taId) {
    this.taId = taId;
  }

  public String getNormativeSource() {
    return normativeSource;
  }

  public void setNormativeSource(String normativeSource) {
    this.normativeSource = normativeSource;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(String prerequisites) {
    this.prerequisites = prerequisites;
  }

  public String getPredicate() {
    return predicate;
  }

  public void setPredicate(String predicate) {
    this.predicate = predicate;
  }

  public String getVariables() {
    return variables;
  }

  public void setVariables(String variables) {
    this.variables = variables;
  }
}
