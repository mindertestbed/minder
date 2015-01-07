package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@Table(name = "RunConfiguration")
public class RunConfiguration extends Model {
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
	public boolean isObsolete;

	/**
	 * When this configuration is obsoleted, the tdl is backed up here.
	 */
	public String tdl;

	public RunConfiguration() {

	}

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	public List<MappedWrapper> wrappers;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	public List<TestRun> testRuns;


	public static final Finder<Long, RunConfiguration> find = new Finder<>(Long.class,
			RunConfiguration.class);
}
