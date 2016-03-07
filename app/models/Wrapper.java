package models;

import java.util.List;

import javax.persistence.*;

import com.avaje.ebean.Model;

@Entity
@Table(name = "Wrapper")
public class Wrapper extends Model {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

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
  public List<WrapperVersion> wrapperVersions;

  public static final Finder<Long, Wrapper> find = new Finder<>(Long.class,
      Wrapper.class);

  public static List<Wrapper> getAll() {
    return find.all();
  }

  public static Wrapper findByName(String value) {
    return find.where().eq("name", value).findUnique();
  }

  public static Wrapper findById(Long id) {
    return find.byId(id);
  }
}
