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
  @Column(name = "job_id")
  public AbstractJob job;

  @ManyToOne
  public SuiteRun suiteRun;

  public Date date;

  @Column(name = "finishdate")
  public Date finishDate;

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
  public TestRunStatus status;

  @Column
  public int number;

  @Column(length = ModelConstants.ERROR_MESSAGE_LENGTH, columnDefinition = "TEXT")
  public byte[] errorMessage;

  public Visibility visibility;

  public TestRun() {

  }

  @Override
  public void save() {
    this.history.save();
    super.save();
  }

  @Override
  public void update() {
    this.history.update();
    super.update();
  }

  public boolean isFinished() {
    return status == TestRunStatus.SUCCESS || this.status == TestRunStatus.FAILED || this.status == TestRunStatus.CANCELLED;
  }

  private static final Finder<Long, TestRun> find = new Finder<>(TestRun.class);

  public static TestRun findById(Long id) {
    TestRun byId = find.byId(id);
    if (null == byId) return null;

    byId.runner = User.findById(byId.runner.id);
    return byId;
  }

  public static List<TestRun> findByJob(AbstractJob rc) {
    return find.where().eq("job", rc).isNotNull("date").orderBy("date desc").findList();
  }


  public static List<TestRun> findBySuiteRun(SuiteRun suiteRun) {
    return find.where().eq("suiteRun", suiteRun).isNotNull("date").orderBy("date desc").findList();
  }

  public static List<TestRun> findByJob(Long id) {
    return find.where().eq("job_id", id).isNotNull("date").orderBy("date desc").findList();
  }


  public static List<TestRun> getRecentRuns(int num) {
    List<TestRun> list = find.where().isNotNull("date").orderBy("date desc").findPagedList(0, num).getList();
    return list;
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
