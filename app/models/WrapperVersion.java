package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 07/08/15.
 */
@Entity
@Table(name = "WrapperVersion")
public class WrapperVersion extends Model {
  @Id
  public Long id;


  @Column(nullable = false)
  @ManyToOne
  public Wrapper wrapper;

  @Column(nullable = false)
  public String version;

  public Date creationDate;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  public List<TSignal> signals;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  public List<TSlot> slots;


  public static final Finder<Long, WrapperVersion> find = new Finder<>(Long.class,
      WrapperVersion.class);

  public static WrapperVersion findById(Long id) {
    return find.byId(id);
  }

  public static WrapperVersion latestByWrapper(Wrapper wrapper) {
    final List<WrapperVersion> list = find.where().eq("wrapper", wrapper).orderBy().desc("creationDate").findList();

    if (list.size() == 0)
      return null;

    return list.get(0);
  }

  public static List<WrapperVersion> getAllByWrapper(Wrapper wrapper) {
    return find.where().eq("wrapper", wrapper).orderBy().desc("version").findList();
  }

  public static WrapperVersion findWrapperAndVersion(Wrapper wrapper, String version) {
    return find.where().eq("wrapper", wrapper).eq("version", version).findUnique();
  }
}
