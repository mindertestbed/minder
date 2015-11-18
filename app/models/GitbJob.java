package models;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@DiscriminatorValue("2")
public class GitbJob extends AbstractJob {

	private static final Finder<Long, GitbJob> find = new Finder<>(
			GitbJob.class);

	public static GitbJob findByTdl(Tdl tdl) {
		return find.where().eq("tdl", tdl).findUnique();
	}

	public static GitbJob findById(Long id) {
		return find.byId(id);
	}
}
