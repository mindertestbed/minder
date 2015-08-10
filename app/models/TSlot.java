package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by yerlibilgin on 31/12/14.
 */
@Entity
@Table(name = "TSlot")
public class TSlot extends Model {
  @Id
  public Long id;

  @ManyToOne
  public WrapperVersion wrapperVersion;

  public String signature;

  public static final Finder<Long, TSlot> find = new Finder<>(
      Long.class, TSlot.class);

  public static void deleteByVersion(WrapperVersion wrapperVersion) {
    SqlUpdate tangoDown = Ebean.createSqlUpdate("DELETE FROM TSlot WHERE wrapper_version_id = " + wrapperVersion.id);
    tangoDown.execute();
  }

  public static TSlot createNew(WrapperVersion wrapperVersion, String methodKey) {
    TSlot ts = new TSlot();
    ts.wrapperVersion = wrapperVersion;
    ts.signature = methodKey;
    ts.save();
    return ts;
  }
}
