package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCase",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"test_assertion_id", "name"})})
public class TestCase extends Model {
  @Id
  public long id;

  @ManyToOne
  @Column(nullable = false)
  public TestAssertion testAssertion;

  @Column(nullable = false)
  public String name;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  private static final Finder<Long, TestCase> find = new Finder<>(TestCase.class);

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  public List<Tdl> tdls;

  public static TestCase findById(Long id) {
    TestCase byId = find.byId(id);
    System.out.println(byId);
    if (null == byId) return null;

    byId.owner = User.findById(byId.owner.id);
    return byId;
  }

  public static TestCase findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static TestCase findByTestAssertionId(Long assertionId) {
    return find.where().eq("testAssertion.id", assertionId).findUnique();
  }

  public static List<TestCase> listByTestAssertionId(Long assertionId) {
    return find.where().eq("testAssertion.id", assertionId).findList();
  }

  public static List<TestCase> listByTestAssertion(TestAssertion assertion) {
    return find.where().eq("testAssertion", assertion).findList();
  }

  public static List<TestCase> findByGroup(TestGroup testGroup, int pageIndex, int pageSize) {
   return find.where().in("testAssertion", TestAssertion.findByGroup(testGroup)).findPagedList(pageIndex, pageSize).getList();
  }

  public static int countByGroup(TestGroup testGroup) {
   return find.where().in("testAssertion", TestAssertion.findByGroup(testGroup)).findRowCount();
  }
}
