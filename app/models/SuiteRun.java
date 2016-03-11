package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import minderengine.Visibility;
import play.Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by yerlibilgin on 30/12/14.
 * <p>
 * Represents a collection of test runs created by a run on a test suite
 */
@Entity
@Table(name = "SuiteRun")
public class SuiteRun extends Model {
  @Id
  public Long id;

  @ManyToOne
  public TestSuite testSuite;

  public Date date;

  @ManyToOne
  public User runner;

  @Column
  public int number;

  public Visibility visibility;

  @OneToMany(cascade = CascadeType.ALL)
  public List<TestRun> testRuns;

  public SuiteRun() {

  }

  public SuiteRun(TestSuite testSuite, User runner) {
    this.testSuite = testSuite;
    this.runner = runner;
    this.date = new Date();
  }

  private static final Finder<Long, SuiteRun> find = new Finder<>(SuiteRun.class);

  public static SuiteRun findById(Long id) {
    SuiteRun byId = find.byId(id);
    if (null == byId) return null;

    byId.runner = User.findById(byId.runner.id);
    return byId;
  }

  public static List<SuiteRun> findBySuite(TestSuite testSuite) {
    return find.where().eq("testSuite", testSuite).orderBy("date desc").findList();
  }

  public static List<SuiteRun> getRecentRuns(int num) {
    return find.where().orderBy("date desc").findPagedList(0, num).getList();
  }

  public static int getMaxNumber() {
    com.avaje.ebean.SqlQuery qu = Ebean.createSqlQuery("Select max(number) from SuiteRun");
    Object max = qu.findUnique().get("max");
    Logger.debug(max + " is max number");
    if (max == null)
      return 1;

    return Integer.parseInt(max.toString());
  }
}
