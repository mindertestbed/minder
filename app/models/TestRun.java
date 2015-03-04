package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
	public Job job;

	public Date date;

	@ManyToOne
	@Column(nullable = false)
	public User runner;

	@OneToOne
	public UserHistory history;

	@Column(length = ModelConstants.REPORT_LENGTH)
	public byte[] report;

	@Column(length = ModelConstants.LOG_LENGTH)
	public String wrappers;

	@Column(nullable = false)
	public boolean success;

	@Column(length = ModelConstants.ERROR_MESSAGE_LENGTH)
	public String errorMessage;

	public TestRun() {

	}

	public TestRun(Job job, User runner) {
		this.job = job;
		this.runner = runner;
		this.date = new Date();
	}

	public static final Finder<Long, TestRun> find = new Finder<>(Long.class,
			TestRun.class);

	public static TestRun findById(Long id) {
		return find.byId(id);
	}

	public static List<TestRun> findByJob(Job rc) {
		return find.where().eq("job", rc).orderBy("date desc").findList();
	}


	public static List<TestRun> findByJob(Long id) {
		return findByJob(Job.findById(id));
	}
}
