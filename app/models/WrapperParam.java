package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name="WrapperParam")
public class WrapperParam extends Model {
  @Id
  public Long id;

  @Column(nullable = false)
  public String name;

  @ManyToOne
  public TestCase testCase;

  public static final Finder<Long, WrapperParam> find = new Finder<>(Long.class,
      WrapperParam.class);

  public static WrapperParam findByTestCaseAndName(TestCase testCase, String str) {
    return find.where().eq("testCase", testCase).eq("name", str).findUnique();
  }
}
