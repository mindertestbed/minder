package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@DiscriminatorValue("0")
public class Job extends AbstractJob {

  private static final Finder<Long, Job> find = new Finder<>(
      Job.class);


  public static List<Job> findByTdl(Tdl tdl) {
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

