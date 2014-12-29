package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestAssertion")
public class TestAssertion extends Model{

  @Id
  public Long id;

  @Column(nullable=false, unique = true)
  public String taId;

  @Column(nullable=false)
  public String normativeSource;

  @Column(nullable=false)
  public String target;

  public String prerequisites;

  @Column(nullable=false)
  public String predicate;

  public String variables;

  @OneToMany(cascade = CascadeType.ALL)
  @Basic(fetch = FetchType.LAZY)
  public List<TestAssertion> testAssertions;

}
