package models;

import com.avaje.ebean.Model;
import editormodels.PreemptionPolicy;

import javax.persistence.*;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 14/09/15.
 */
@Entity
@Table(name = "TestSuite")
public class TestSuite extends Model {
  @Id
  public long id;

  @Column(unique = true, nullable = false)
  public String name;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  /**
   * If there is a parameter matching here, it will be used for the tests
   * if the child TDL job defines its own parameter, it will override this
   */

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String mtdlParameters;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @ManyToOne
  @Column(nullable = false)
  public TestGroup testGroup;

  public Visibility visibility;

  public PreemptionPolicy preemptionPolicy;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "testSuite")
  public List<SuiteJob> jobs;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "testSuite")
  public List<SuiteRun> run;

  private static final Finder<Long, TestSuite> find = new Finder<>(TestSuite.class);

  public static TestSuite findById(Long id){
    return find.byId(id);
  }

  public static List<TestSuite> findByGroup(TestGroup group) {
    return find.where().eq("testGroup", group).setOrderBy("id").findList();
  }

  public static TestSuite findByGroupAndName(TestGroup tg, String name) {
    return find.where().eq("testGroup", tg).eq("name", name).findUnique();
  }
}
