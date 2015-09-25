package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 07/08/15.
 */

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "Tdl")
public class Tdl extends Model {
  @Id
  public long id;

  @ManyToOne
  @Column(nullable = false)
  public TestCase testCase;

  @Column(nullable = false, length = ModelConstants.MAX_TDL_LENGTH, columnDefinition = "TEXT")
  public String tdl;

  @Column(nullable = false)
  public String version;

  @OneToMany
  public List<AbstractJob> jobs;

  public Date creationDate;


  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public List<WrapperParam> parameters;

  private static final Finder<Long, Tdl> find = new Finder<>(Tdl.class);

  public static Tdl getLatestTdl(TestCase testCase) {
    final List<Tdl> list = findByTestCase(testCase);

    if (list.size() == 0)
      return null;

    return list.get(0);
  }

  public static List<Tdl> findByTestCase(TestCase testCase) {
    return find.where().eq("testCase", testCase).orderBy().desc("creationDate").findList();
  }

  public static Tdl findById(Long tdlId) {
    return find.byId(tdlId);
  }
}
