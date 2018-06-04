package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name = "AdapterParam")
public class AdapterParam extends Model {
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

        AdapterParam that = (AdapterParam) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public static final Finder<Long, AdapterParam> find = new Finder<>(Long.class,
            AdapterParam.class);

    public static AdapterParam findByTdlAndName(Tdl tdl, String str) {
        return find.where().eq("tdl", tdl).eq("name", str).findUnique();
    }

    public static List<AdapterParam> findByTestCase(Tdl tdl) {
        return find.where().eq("tdl", tdl).findList();
    }

    public static AdapterParam findById(Long id) {
        return find.byId(id);
    }
}
