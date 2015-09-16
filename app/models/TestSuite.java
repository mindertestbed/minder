package models;

import com.avaje.ebean.Model;

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

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  /**
   * If there is a parameter matching here, it will be used for the tests
   * if the child TDL job defines its own parameter, it will override this
   */
  public String mtdlParameters;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @ManyToOne
  @Column(name = "group", nullable = false)
  public TestGroup testGroup;


  @OneToMany
  public List<SuiteJob> jobs;

  private static final Finder<Long, TestSuite> find = new Finder<>(TestSuite.class);

  public static TestSuite findById(Long id){
    return find.byId(id);
  }

  public static List<TestSuite> findByGroup(TestGroup group) {
    return find.where().eq("testGroup", group).setOrderBy("id").findList();
  }
}
