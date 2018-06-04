package models;


import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;


/**
 * Created by yerlibilgin
 */

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_group_id", "name"})
})
public class ReportTemplate extends Model {
  @Id
  public Long id;

  @Column(nullable = false)
  public String name;

  @ManyToOne
  @Column(nullable = false)
  public User owner;


  @Column(nullable = false, length = ModelConstants._100K)
  public byte[] html;


  public int number;

  /**
   * true: this report is for a batch (i.e. multiple tests), else single test
   */
  public boolean isBatchReport;


  @ManyToOne
  public TestGroup testGroup;

  private static final Finder<Long, ReportTemplate> find = new Finder<>(
      ReportTemplate.class);

  public static List<ReportTemplate> findByGroup(TestGroup testGroup) {
    return find.where().eq("testGroup", testGroup).orderBy("id").findList();
  }

  public static ReportTemplate findByGroup(TestGroup testGroup, String name) {
    return find.where().eq("testGroup", testGroup).eq("name", name).findUnique();
  }

  public static List<ReportTemplate> findByGroupAndType(TestGroup testGroup, boolean isBatchReport) {
    return find.where().eq("testGroup", testGroup).eq("isBatchReport", isBatchReport).orderBy("name").findList();
  }

  public static ReportTemplate byId(long id) {
    return find.byId(id);
  }

  public static int getNewNumber(long testGroupId) {
    List<ReportTemplate> list = find.where().eq("test_group_id", testGroupId).orderBy("number desc").findList();


    if (list.isEmpty())
      return 1;

    return list.get(0).number + 1;

  }
}
