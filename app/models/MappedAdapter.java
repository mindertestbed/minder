package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name = "MappedAdapter")
public class MappedAdapter extends Model {
  @Id
  public Long id;

  @ManyToOne
  public AdapterParam parameter;

  @ManyToOne
  public AdapterVersion adapterVersion;

  @ManyToOne
  @Column(name="job_id")
  public AbstractJob job;

  public static final Finder<Long, MappedAdapter> find = new Finder<>(MappedAdapter.class);

  public static MappedAdapter findById(Long id) {
    return find.byId(id);
  }

  public static List<MappedAdapter> findByJob(AbstractJob rc) {
    return find.where().eq("job", rc).orderBy("id").findList();
  }

  public static void deleteByJob(AbstractJob rc) {
    find.where().eq("job", rc).findList().forEach(MappedAdapter::delete);
  }

  public static List<MappedAdapter> findByAdapterParam(AdapterParam adapterParam) {
    return find.where().eq("parameter", adapterParam).orderBy("id").findList();
  }
}
