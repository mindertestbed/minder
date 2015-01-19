package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@Table(name = "TestRun")
public class TestRun extends Model {
	@Id
	public Long id;

	@ManyToOne
	@Column(nullable = false)
	public RunConfiguration runConfiguration;

	public Date date;

	@ManyToOne
	@Column(nullable = false)
	public User runner;

	@OneToOne
	public UserHistory history;

	@Column(length = 10000)
	public byte[] report;

	@Column(length = 1000)
	public String wrappers;

	@Column(nullable = false)
	public boolean success;

	public TestRun() {

	}

	public TestRun(RunConfiguration runConfiguration, User runner) {
		this.runConfiguration = runConfiguration;
		this.runner = runner;
		this.date = new Date();
	}

	public static final Finder<Long, TestRun> find = new Finder<>(Long.class,
			TestRun.class);

	public static TestRun findById(Long id) {
		return find.byId(id);
	}
}
