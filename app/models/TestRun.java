package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Model;
import minderengine.Visibility;
import play.Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
  public byte[] reportMetadata;

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
    if (null == byId) {
      return null;
    }

    byId.runner = User.findById(byId.runner.id);
    return byId;
  }

  public static List<TestRun> findByJob(AbstractJob rc) {
    return find.where().eq("job", rc).isNotNull("date").orderBy("date desc").findList();
  }


  public static List<TestRun> findBySuiteRun(SuiteRun suiteRun) {
    return find.where().eq("suiteRun", suiteRun).isNotNull("date").orderBy("date desc").findList();
  }

  public static List<TestRun> findBySuiteRunId(long suiteRunID) {
    return find.where().eq("suite_run_id", suiteRunID).isNotNull("date").orderBy("date desc").findList();
  }

  public static List<TestRun> findBySuiteRun(SuiteRun suiteRun, int pageIndex, int pageSize) {
    return find.where().eq("suiteRun", suiteRun).isNotNull("date").orderBy("date desc").findPagedList(pageIndex, pageSize).getList();
  }

  public static int countBySuiteRun(SuiteRun suiteRun) {
    return find.where().eq("suiteRun", suiteRun).isNotNull("date").findRowCount();
  }

  public static List<TestRun> findByJob(Long id) {
    return find.where().eq("job_id", id).isNotNull("date").orderBy("date desc").findList();
  }

  public static List<TestRun> findByJob(AbstractJob job, int pageIndex, int pageSize) {
    return find.where().eq("job", job).isNotNull("date").orderBy("date desc").findPagedList(pageIndex, pageSize).getList();
  }

  public static int countByJob(AbstractJob job) {
    return find.where().eq("job", job).isNotNull("date").orderBy("date desc").findRowCount();
  }

  public static List<TestRun> getRecentPagedRuns(int page, int pageSize) {
    List<TestRun> list = find.where().isNotNull("date").orderBy("date desc")
        .findPagedList(page, pageSize).getList();
    return list;
  }

  public static int getRecentPagedRunsCount(int pageSize) {
    final double count = find.where().isNotNull("date").orderBy("date desc").findRowCount() * 1.0 / pageSize;
    return (int) Math.ceil(count);
  }

  public static int getMaxNumber() {
    final List<TestRun> list = find.setMaxRows(1).orderBy("number desc").findList();
    if (list.isEmpty()) {
      return 1;
    }
    return list.get(0).number;
  }

  public static void updateUser(User user, User localUser) {

  }

  public static int countRunsForJob(Job job) {
    com.avaje.ebean.SqlQuery qu = Ebean.createSqlQuery("Select count(id) from TestRun where job_id=" + job.id);
    Object count = qu.findUnique().get("count");
    return Integer.parseInt(count.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestRun testRun = (TestRun) o;
    return number == testRun.number &&
        Objects.equals(id, testRun.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, number);
  }
}
