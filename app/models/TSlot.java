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
  public Wrapper wrapper;

  public String signature;

  public static final Finder<Long, TSlot> find = new Finder<>(
      Long.class, TSlot.class);

  public static void deleteByWrapper(Wrapper wrapper) {
    SqlUpdate tangoDown = Ebean.createSqlUpdate("DELETE FROM TSlot WHERE wrapper_id = " + wrapper.id);
    tangoDown.execute();
  }

  public static TSlot createNew(Wrapper wrapper, String methodKey) {
    TSlot ts = new TSlot();
    ts.wrapper = wrapper;
    ts.signature = methodKey;
    ts.save();
    return ts;
  }
}
