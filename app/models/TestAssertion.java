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

import play.db.ebean.Model;

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

	@Column(nullable = false, length = 250)
	public String normativeSource;

	@Column(nullable = false)
	public String target;

	@Column(length = 250)
	public String prerequisites;

	@Column(nullable = false)
	public String predicate;

	@Column(length = 250)
	public String variables;

	@Column(length = 250)
	public String tag;

	@Column(length = 1024)
	public String description;

	public PrescriptionLevel prescriptionLevel;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public List<TestCase> testCases;

  public static final Finder<Long, TestAssertion> find = new Finder<>(
      Long.class, TestAssertion.class);

	public static List<TestAssertion> findByGroup(TestGroup group){
    return find.where().eq("testGroup", group).setOrderBy("id").findList();
     }

  public static TestAssertion findByTaId(String taId){
    return find.where().eq("taId", taId).findUnique();
  }

  public static TestAssertion findById(Long id) {
    return find.byId(id);
  }
}
