package models;

import com.avaje.ebean.Model;
import com.yerlibilgin.ValueChecker;
import minderengine.Visibility;

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

  private static final Finder<Long, AbstractJob> find = new Finder<>(AbstractJob.class);

  public static AbstractJob findById(Long id) {
    AbstractJob byId = find.byId(id);
    if (byId == null) {
      return null;
    }
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

  @OneToOne
  public ReportTemplate reportTemplate;

  public Visibility visibility;

  @Column(unique = true)
  public String httpEndpoint;


  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String mtdlParameters;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "job")
  public List<MappedAdapter> mappedAdapters;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "job")
  public List<TestRun> testRuns;

  public static void deleteById(Long id) {
    AbstractJob aj = findById(id);
    if (aj != null) {
      aj.delete();
    }
  }

  public static AbstractJob findByEndpoint(String httpEndpoint) {
    ValueChecker.notNull(httpEndpoint, "httpEndpoint");
    return find.where().eq("httpEndpoint", httpEndpoint).findUnique();
  }
}
