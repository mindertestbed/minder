package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name="WrapperParam")
public class WrapperParam extends Model {
  @Id
  public Long id;

  @Column(nullable = false)
  public String name;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public List<ParamSignature> signatures;

  @ManyToOne
  public Tdl tdl;


  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    WrapperParam that = (WrapperParam) o;

    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  public static final Finder<Long, WrapperParam> find = new Finder<>(Long.class,
      WrapperParam.class);

  public static WrapperParam findByTdlAndName(Tdl tdl, String str) {
    return find.where().eq("tdl", tdl).eq("name", str).findUnique();
  }

  public static List<WrapperParam> findByTestCase(Tdl tdl) {
    return find.where().eq("tdl", tdl).findList();
  }

  public static WrapperParam findById(Long id) {
    return find.byId(id);
  }
}
