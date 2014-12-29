package models;

import com.avaje.ebean.ExpressionList;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 *
 * Represents a test case catry that might point to
 * Multiple Test Case Groups
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCaseCategory")
public class TestCaseCategory extends Model {

  @Id
  public Long id;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @Column(unique = true, nullable = false)
  public String name;

  @OneToMany(cascade = CascadeType.ALL)
  public List<TestCaseGroup> testCaseGroups;

  @Column(nullable = false, length = 50)
  public String shortDescription;

  public String description;

  public static final Finder<Long, TestCaseCategory> find = new Finder<Long, TestCaseCategory>(
      Long.class, TestCaseCategory.class);

  public static final List<TestCaseCategory> findByUser(User user) {
    ExpressionList<TestCaseCategory> lst = find.where().eq("owner", user);
    return lst.findList();
  }

  public int dummy = 0;
}
