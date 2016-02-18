package models;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

import com.avaje.ebean.Model;

import javax.persistence.*;


/**
 * Created by edonafasllija on 11/02/16.
 */
@Entity
@DiscriminatorValue("3")
public class JobTemplate extends AbstractJob {
  @ManyToOne
  public TestGroup testGroup;

  private static final Finder<Long, JobTemplate> find = new Finder<>(
      JobTemplate.class);

  public static List<JobTemplate> findByGroup(TestGroup testGroup) {
    return find.where().eq("testGroup", testGroup).orderBy("id").findList();
  }

  public static JobTemplate findByGroup(TestGroup testGroup, String name) {
    return find.where().eq("testGroup", testGroup).eq("name", name).findUnique();
  }
}
