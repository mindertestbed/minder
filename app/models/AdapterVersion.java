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
@Table(name = "AdapterVersion")
public class AdapterVersion extends Model {
  @Id
  public Long id;


  @Column(nullable = false)
  @ManyToOne
  public Adapter adapter;

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
  public List<MappedAdapter> mappedAdapters;


  public static final Finder<Long, AdapterVersion> find = new Finder<>(AdapterVersion.class);

  public static AdapterVersion findById(Long id) {
    return find.byId(id);
  }

  public static AdapterVersion latestByAdapter(Adapter adapter) {
    final List<AdapterVersion> list = find.where().eq("adapter", adapter).orderBy().desc("creationDate").findList();

    if (list.size() == 0)
      return null;

    return list.get(0);
  }

  public static AdapterVersion latestByAdapterName(String adapterName) {
    final List<AdapterVersion> list = find.where().eq("adapter.name", adapterName).orderBy().desc("creationDate").findList();

    if (list.size() == 0)
      return null;

    return list.get(0);
  }

  public static List<AdapterVersion> getAllByAdapter(Adapter adapter) {
    return find.where().eq("adapter", adapter).orderBy().desc("version").findList();
  }

  public static AdapterVersion findAdapterAndVersion(Adapter adapter, String version) {
    return find.where().eq("adapter", adapter).eq("version", version).findUnique();
  }

  public static AdapterVersion findAdapterNameAndVersion(String adapterName, String version) {
    return find.where().eq("adapter.name", adapterName).eq("version", version).findUnique();
  }
}
