package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by yerlibilgin on 13/01/15.
 *
 * Represents the ASSETS uploaded and used by users.
 */
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_group_id", "name"})
})
public class JobSchedule extends Model {
  private static final Finder<Long, JobSchedule> find = new Finder<>(JobSchedule.class);

  @Id
  public Long id;

  @ManyToOne
  @Column(nullable = false)
  public TestGroup testGroup;

  @Column(nullable = false)
  public String name;

  @Column(nullable = false)
  public String cronExpression;

  @Column(length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;


  @ManyToMany(targetEntity = Job.class, cascade = CascadeType.REMOVE)
  public Set<Job> jobs;

  @ManyToMany(targetEntity = TestSuite.class, cascade = CascadeType.REMOVE)
  public Set<TestSuite> testSuites;


  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @ManyToOne
  public JobSchedule nextJob;

  public static List<JobSchedule> findByGroup(TestGroup testGroup) {
    return find.where().eq("testGroup", testGroup).orderBy("id").findList();
  }

  public static JobSchedule findByGroup(TestGroup testGroup, String name) {
    return find.where().eq("testGroup", testGroup).eq("name", name).findUnique();
  }

  public static JobSchedule findById(Long id) {
    return find.byId(id);
  }


  public static List<JobSchedule> findByTestGroup(TestGroup testGroup, int pageIndex, int pageSize) {
    return find.where().eq("testGroup", testGroup).orderBy("id asc").findPagedList(pageIndex, pageSize).getList();
  }

  public static int countByTestGroup(TestGroup testGroup) {
    return find.where().eq("testGroup", testGroup).findRowCount();
  }

}
