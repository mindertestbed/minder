package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestAssertion")
public class TestAssertion extends Model {
  @Id
  public Long id;

  @ManyToOne
  @Column(name = "group", nullable = false)
  public TestGroup testGroup;

  @Column(nullable = false, unique = true)
  public String taId;

  @Column(nullable = false, length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String normativeSource;

  @Column(nullable = false)
  public String target;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String prerequisites;

  @Column(nullable = false, length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String predicate;

  @Column(length = ModelConstants.K)
  public String variables;

  @Column(length = ModelConstants.K)
  public String tag;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  public PrescriptionLevel prescriptionLevel;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  public List<TestCase> testCases;

  private static final Finder<Long, TestAssertion> find = new Finder<>(
      Long.class, TestAssertion.class);

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  public static List<TestAssertion> findByGroup(TestGroup group) {
    return find.where().eq("testGroup", group).setOrderBy("taId").findList();
  }

  public static TestAssertion findByTaId(String taId) {
    return find.where().eq("taId", taId).findUnique();
  }

  public static TestAssertion findById(Long id) {
    TestAssertion byId = find.byId(id);
    byId.owner = User.findById(byId.owner.id);
    return byId;
  }

  public static void updateUser(User user, User localUser) {

  }
}
