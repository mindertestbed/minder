package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name = "MappedWrapper")
public class MappedWrapper extends Model{
  @Id
  public Long id;

  @Column(nullable = false)
  @ManyToOne
  public WrapperParam parameter;

  @OneToOne
  public Wrapper wrapper;

  @ManyToOne
  @Column(nullable = false)
  public RunConfiguration runConfiguration;

  public static final Finder<Long, MappedWrapper> find = new Finder<>(Long.class,
      MappedWrapper.class);

  public static MappedWrapper findById(Long id) {
    return find.byId(id);
  }

  public static List<MappedWrapper> findByRunConfiguration(RunConfiguration rc){
    return find.where().eq("runConfiguration", rc).orderBy("id").findList();
  }

  public static void deleteByRunConfiguration(RunConfiguration rc) {
    for (MappedWrapper mw : find.where().eq("runConfiguration", rc).findList()) {
      mw.delete();
    }
  }
}
