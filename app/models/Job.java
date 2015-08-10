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
	public Tdl tdl;


	@ManyToOne
	@Column(nullable = false)
	public User owner;

	public String mtdlParameters;

	public Job() {

	}

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<MappedWrapper> mappedWrappers;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<TestRun> testRuns;

	private static final Finder<Long, Job> find = new Finder<>(Long.class,
			Job.class);


	public static List<Job> findByTdl(Tdl tdl){
		ExpressionList<Job> f = find.where().eq("tdl", tdl);
		return f.orderBy().desc("id").findList();
	}

	public static Job findById(Long id) {
		Job byId = find.byId(id);
		byId.owner = User.findById(byId.owner.id);
		return byId;
	}
	public static Job findByTdlAndName(Tdl tdl, String name) {
		return find.where().eq("tdl", tdl).eq("name", name).findUnique();
	}
}

