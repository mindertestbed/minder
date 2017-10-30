package models;

import com.avaje.ebean.ExpressionList;
import scala.Int;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Set;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@DiscriminatorValue("0")
public class Job extends AbstractJob {

  private static final Finder<Long, Job> find = new Finder<>(
      Job.class);


  @ManyToMany(targetEntity = JobSchedule.class, mappedBy = "jobs")
  public Set<JobSchedule> schedules;

  @ManyToOne
  public TestSuite testSuite;

  public static List<Job> findByTdl(Tdl tdl) {
    ExpressionList<Job> f = find.where().eq("tdl", tdl);
    return f.orderBy().desc("id").findList();
  }


  public static List<Job> findByTestCase(TestCase testCase, int pageIndex, int pageSize) {
    return find.where().in("tdl", Tdl.findByTestCase(testCase)).orderBy("id desc").findPagedList(pageIndex, pageSize).getList();
  }

  public static int countByTestCase(TestCase testCase) {
    return find.where().in("tdl", Tdl.findByTestCase(testCase)).findRowCount();
  }


  public static List<Job> getAllByTestSuite(TestSuite testSuite) {
    return find.where().eq("testSuite", testSuite).orderBy("id").findList();
  }

  public static Job findById(Long id) {
    Job byId = find.byId(id);
    if (byId == null)
      return null;
    byId.owner = User.findById(byId.owner.id);
    return byId;
  }

  public static Job findByTdlAndName(Tdl tdl, String name) {
    return find.where().eq("tdl", tdl).eq("name", name).findUnique();
  }

  public static List<Job> findByTestSuite(TestSuite testSuite) {
    return find.where().eq("testSuite", testSuite).orderBy("name").findList();
  }

  public static Job findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static List<Job> findAll() {
    return find.where().setOrderBy("id").findList();
  }


  public static List<Job> findBySuite(TestSuite testSuite, int pageIndex, int pageSize) {
    return find.where().eq("testSuite", testSuite).orderBy("id desc").findPagedList(pageIndex, pageSize).getList();
  }

  public static int countBySuite(TestSuite testSuite) {
    return find.where().eq("testSuite", testSuite).findRowCount();
  }

}

