package models;

import java.util.List;

import javax.persistence.*;

import com.avaje.ebean.Model;

@Entity
@Table(name = "Adapter")
public class Adapter extends Model {

  @Id
  public Long id;

  @Column(name = "NAME", unique = true)
  public String name;


  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;


  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  @ManyToOne
  public User user;

  @OneToMany(cascade = CascadeType.ALL)
  public List<AdapterVersion> adapterVersions;

  public static final Finder<Long, Adapter> find = new Finder<>(Adapter.class);

  public static List<Adapter> getAll() {
    return find.all();
  }

  public static Adapter findByName(String value) {
    return find.where().eq("name", value).findUnique();
  }

  public static Adapter findById(Long id) {
    return find.byId(id);
  }
}
