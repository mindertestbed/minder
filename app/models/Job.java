package models;

import com.avaje.ebean.ExpressionList;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@Table(name = "Job")
public class Job extends Model {
	@Id
	public Long id;

	@Column(unique = true, nullable = false)
	public String name;

	@ManyToOne
	@Column(nullable = false)
	public TestCase testCase;

	/**
	 * When the tdl changes and the wrappers in the new tdl are not compatible with
	 * this run configurtion, then this configuration is obsoleted. Hence, not visible in the main UI.
	 */
	public boolean obsolete;

	/**
	 * When this configuration is obsoleted, the tdl is backed up here.
	 */
	@Column(length = 20000)
	public String tdl;

	public Job() {

	}

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<MappedWrapper> mappedWrappers;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<TestRun> testRuns;

	public static final Finder<Long, Job> find = new Finder<>(Long.class,
			Job.class);


	public static List<Job> findByTestCase(TestCase testCase){
		ExpressionList<Job> f = find.where().eq("testCase", testCase);
		return f.setOrderBy("id").findList();
	}

	public static Job findById(Long id) {
		return find.byId(id);
	}
	public static Job findByTestCaseAndName(TestCase testCase, String name) {
		return find.where().eq("testCase", testCase).eq("name", name).findUnique();
	}
}

