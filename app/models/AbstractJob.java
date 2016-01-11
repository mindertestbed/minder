package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 16/09/15.
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "_type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class AbstractJob extends Model {
  @Id
  public Long id;

  @Column(unique = true, nullable = false)
  public String name;

  @ManyToOne
  @Column(nullable = false)
  public Tdl tdl;


  @ManyToOne
  @Column(nullable = false)
  public User owner;

  public Visibility visibility;

  public String mtdlParameters;


  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  public List<MappedWrapper> mappedWrappers;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public List<TestRun> testRuns;
}
