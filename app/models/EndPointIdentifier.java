package models;

import com.avaje.ebean.Model;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author yerlibilgin
 */
@Entity
public class EndPointIdentifier extends Model {

  public String method;
  public String identifier;

  @ManyToOne
  public Tdl tdl;

  private static final Finder<Long, EndPointIdentifier> find = new Finder<>(EndPointIdentifier.class);

  public static EndPointIdentifier findById(long id) {
    return find.byId(id);
  }

  public static List<EndPointIdentifier> listByTdl(Tdl tdl){
    return find.where().eq("tdl", tdl).orderBy("identifier").findList();
  }
}
