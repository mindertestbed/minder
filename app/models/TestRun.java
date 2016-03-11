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
 */
@Entity
@Table(name = "TestRun")
public class TestRun extends Model {
  @Id
  public Long id;

  @ManyToOne
  @Column(name="job_id")
  public AbstractJob job;

  @ManyToOne
  public SuiteRun suiteRun;

  public Date date;

  @ManyToOne
  @Column(nullable = false)
  public User runner;

  @OneToOne
  public UserHistory history;

  @Column(length = ModelConstants.REPORT_LENGTH)
  public byte[] report;

  @Column(length = ModelConstants.LOG_LENGTH, columnDefinition = "TEXT")
  public String sutNames;

  @Column(nullable = false)
  public boolean success;

  @Column
  public int number;

  @Column(length = ModelConstants.ERROR_MESSAGE_LENGTH, columnDefinition = "TEXT")
  public byte[] errorMessage;

  public Visibility visibility;

  public TestRun() {

  }

  public TestRun(AbstractJob job, User runner) {
    this.job = job;
    this.runner = runner;
    this.date = new Date();
  }

  private static final Finder<Long, TestRun> find = new Finder<>(TestRun.class);

  public static TestRun findById(Long id) {
    TestRun byId = find.byId(id);
    if (null == byId) return null;

    byId.runner = User.findById(byId.runner.id);
    return byId;
  }

  public static List<TestRun> findByJob(AbstractJob rc) {
    return find.where().eq("job", rc).orderBy("date desc").findList();
  }


  public static List<TestRun> findBySuiteRun(SuiteRun suiteRun) {
    return find.where().eq("suiteRun", suiteRun).orderBy("date desc").findList();
  }


  public static List<TestRun> findByJob(Long id) {
    return find.where().eq("job_id", id).orderBy("date desc").findList();
  }


  public static List<TestRun> getRecentRuns(int num) {
    return find.where().orderBy("date desc").findPagedList(0, num).getList();
  }

  public static int getMaxNumber() {
    com.avaje.ebean.SqlQuery qu = Ebean.createSqlQuery("Select max(number) from TestRun");
    Object max = qu.findUnique().get("max");
    Logger.debug(max + " is max number");
    if (max == null)
      return 1;

    return Integer.parseInt(max.toString());
  }

  public static void updateUser(User user, User localUser) {

  }

  public static int countRunsForJob(Job job) {
    com.avaje.ebean.SqlQuery qu = Ebean.createSqlQuery("Select count(id) from TestRun where job_id=" + job.id);
    Object count = qu.findUnique().get("count");
    return Integer.parseInt(count.toString());
  }
}
