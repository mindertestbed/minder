package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
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
  private static final Finder<Long, AbstractJob> find = new Finder<>(
      AbstractJob.class);

  public static AbstractJob findById(Long id) {
    AbstractJob byId = find.byId(id);
    if (byId == null)
      return null;
    byId.owner = User.findById(byId.owner.id);
    return byId;
  }

  @Id
  public Long id;

  @Column(nullable = false)
  public String name;

  @ManyToOne
  public Tdl tdl;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  public Visibility visibility;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String mtdlParameters;


  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "job")
  public List<MappedWrapper> mappedWrappers;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "job")
  public List<TestRun> testRuns;

  public static void deleteById(Long id) {
    AbstractJob aj = findById(id);
    if (aj != null)
      aj.delete();
  }
}
