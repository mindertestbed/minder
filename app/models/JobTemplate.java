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

@Table(name = "JobTemplate")
public class JobTemplate extends Model {

  @Id
  public Long id;

  @Column(nullable = false)
  public String name;

  @ManyToOne
  @Column(nullable = false)
  public TestGroup testGroup;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  public Visibility visibility;

  public String mtdlParameters;


  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  public List<MappedWrapper> mappedWrappers;

  private static final Finder<Long, JobTemplate> find = new Finder<>(
      JobTemplate.class);

  public static List<JobTemplate> findByGroup(TestGroup testGroup) {
    return find.where().eq("testGroup", testGroup).orderBy("id").findList();
  }

  public static JobTemplate findByGroup(TestGroup testGroup, String name) {
    return find.where().eq("testGroup", testGroup).eq("name", name).findUnique();
  }
}
