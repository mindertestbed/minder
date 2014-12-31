package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class Wrapper extends Model {
  @Id
  public Long id;

  @Column(unique = true)
  public String name;

  @ManyToOne
  @Column(name = "owner")
  public User owner;


  @OneToMany
  public List<TSignal> signals;

  @OneToMany
  public List<TSlot> slots;

  public static final Finder<Long, Wrapper> find = new Finder<>(
      Long.class, Wrapper.class);
}
