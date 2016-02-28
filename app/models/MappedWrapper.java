package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name = "MappedWrapper")
public class MappedWrapper extends Model {
  @Id
  public Long id;

  @Column(nullable = false)
  @ManyToOne
  public WrapperParam parameter;

  @ManyToOne
  public WrapperVersion wrapperVersion;

  @ManyToOne()
  public AbstractJob job;

  public static final Finder<Long, MappedWrapper> find = new Finder<>(MappedWrapper.class);

  public static MappedWrapper findById(Long id) {
    return find.byId(id);
  }

  public static List<MappedWrapper> findByJob(AbstractJob rc) {
    return find.where().eq("job", rc).orderBy("id").findList();
  }

  public static void deleteByJob(AbstractJob rc) {
    find.where().eq("job", rc).findList().forEach(models.MappedWrapper::delete);
  }

  public static List<MappedWrapper> findByWrapperParam(WrapperParam wrapperParam) {
    return find.where().eq("parameter", wrapperParam).orderBy("id").findList();
  }
}
