package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCase")
public class TestCase extends Model{
  @Id
  public Long id;

  @ManyToOne
  public TestAssertion testAssertion;

  @ManyToOne
  public TestCaseGroup testCaseGroup;

  @Column(unique = true, nullable = false)
  public String testCaseName;

  @Column(nullable = false, length = 50)
  public String shortDescription;

  public String description;

  @Column(nullable = false, length = 4096)
  public String tdl;

  public String parameters;

}
