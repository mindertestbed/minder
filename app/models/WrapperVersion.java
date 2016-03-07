package models;

import com.avaje.ebean.Model;

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

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  public List<GitbEndpoint> gitbEndpoints;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public List<MappedWrapper> mappedWrappers;


  public static final Finder<Long, WrapperVersion> find = new Finder<>(WrapperVersion.class);

  public static WrapperVersion findById(Long id) {
    return find.byId(id);
  }

  public static WrapperVersion latestByWrapper(Wrapper wrapper) {
    final List<WrapperVersion> list = find.where().eq("wrapper", wrapper).orderBy().desc("creationDate").findList();

    if (list.size() == 0)
      return null;

    return list.get(0);
  }

  public static WrapperVersion latestByWrapperName(String wrapperName) {
    final List<WrapperVersion> list = find.where().eq("wrapper.name", wrapperName).orderBy().desc("creationDate").findList();

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

  public static WrapperVersion findWrapperNameAndVersion(String wrapperName, String version) {
    return find.where().eq("wrapper.name", wrapperName).eq("version", version).findUnique();
  }
}
