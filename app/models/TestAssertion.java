package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.ebean.Model;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestAssertion")
public class TestAssertion extends Model{
  @Id
  public Long id;

  @ManyToOne
  @Column(name="group", nullable = false)
  public TestGroup testGroup;

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

  @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
  public List<TestCase> testCases;

  public static final Finder<Long, TestAssertion> find = new Finder<>(
      Long.class, TestAssertion.class);
}
