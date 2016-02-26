package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by yerlibilgin on 31/12/14.
 */
@Entity
@Table(name = "TSignal")
public class TSignal extends Model {
  @Id
  public Long id;

  @ManyToOne
  public WrapperVersion wrapperVersion;

  public String signature;

  public static final Finder<Long, TSignal> find = new Finder<>(TSignal.class);

  public static void deleteByVersion(WrapperVersion wrapperVersion) {
    SqlUpdate tangoDown = Ebean.createSqlUpdate("DELETE FROM TSignal WHERE wrapper_version_id = " + wrapperVersion.id);
    tangoDown.execute();
  }

  public static List<TSignal> findBySignature(String signature) {
    return find.where().eq("signature", signature).findList();
  }

  public static TSignal createNew(WrapperVersion wrapperVersion, String methodKey) {
    TSignal ts = new TSignal();
    ts.wrapperVersion = wrapperVersion;
    ts.signature = methodKey;
    ts.save();
    return ts;
  }
}
