package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
public class TestCase extends MinderEntity{
  @ManyToOne
  private TestAssertion source;

  @Column(unique = true, nullable = false)
  private String testCaseName;

  @Column(nullable = false, length = 50)
  private String shortDescription;

  private String description;

  @Column(nullable = false)
  private String tdl;

  private String parameters;
}
