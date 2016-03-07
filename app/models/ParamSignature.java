package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ParamSignature")
public class ParamSignature extends Model {
  @Id
  public Long id;

  @Column(name = "name")
  public String signature;

  @ManyToOne
  public WrapperParam wrapperParam;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ParamSignature that = (ParamSignature) o;

    if (signature != null ? !signature.equals(that.signature) : that.signature != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return signature != null ? signature.hashCode() : 0;
  }

  public static final Finder<Long, ParamSignature> find = new Finder<>(Long.class,
      ParamSignature.class);

  public static List<ParamSignature> getAll() {
    return find.all();
  }

  public static List<ParamSignature> getByWrapperParam(WrapperParam wp) {
    return find.where().eq("wrapperParam", wp).findList();

  }
}

