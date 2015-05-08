package models;

import controllers.common.enumeration.TestStatus;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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

	@Column
	public int number;

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


	public static List<TestRun> getRecentRuns(int num){
		return find.where().orderBy("date desc").findPagingList(num).getPage(0).getList();
	}

	public static int getMaxNumber(){
		List<TestRun> list = find.where().orderBy("number desc").findPagingList(1).getPage(0).getList();

		if (list.size() == 0)
			return 0;

		return list.get(0).number;
	}
}
