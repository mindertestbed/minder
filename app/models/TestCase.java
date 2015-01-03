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
  @Column(nullable = false)
  public TestAssertion testAssertion;

  @Column(unique = true, nullable = false)
  public String name;

  @Column(nullable = false, length = 50)
  public String shortDescription;

  public String description;

  @Column(nullable = false, length = 10000)
  public String tdl;

  public String parameters;

  public static final Finder<Long, TestCase> find = new Finder<>(
      Long.class, TestCase.class);

}
