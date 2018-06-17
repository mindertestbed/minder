package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;

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

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  public List<AbstractJob> jobs;

  public Date creationDate;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  public List<AdapterParam> parameters;

  /**
   * If the first rivet is an end point, this tdl is an HTTP endpoint,
   * and it must be able to be called indirectly (asynchronously).
   * The flag for such a tdl is set here
   */
  public boolean isHttpEndpoint;

  /**
   * If ths tdl contains http endpoint identifiers, then the identifiers
   */
  @OneToMany
  public List<EndPointIdentifier> httpEndpointIdentifiers;

  private static final Finder<Long, Tdl> find = new Finder<>(Tdl.class);

  public static Tdl getLatestTdl(TestCase testCase) {
    final List<Tdl> list = findByTestCase(testCase);

    if (list.size() == 0) {
      return null;
    }

    return list.get(0);
  }

  public static List<Tdl> findByTestCase(TestCase testCase) {
    return find.where().eq("testCase", testCase).orderBy().desc("creationDate").findList();
  }

  public static Tdl findById(Long tdlId) {
    return find.byId(tdlId);
  }

  public static List<Tdl> getAllUnparametricTdls() {
    RawSql rawSql = RawSqlBuilder.unparsed("SELECT t.* FROM tdl t LEFT JOIN adapterparam w ON t.id = w.tdl_id WHERE w.tdl_id IS NULL")
        .columnMapping("id", "id")
        .columnMapping("test_case_id", "testCase.id")
        .columnMapping("tdl", "tdl")
        .columnMapping("version", "version")
        .columnMapping("creation_date", "creationDate")
        .create();

    Query<Tdl> query = Ebean.find(Tdl.class);
    query.setRawSql(rawSql);
    List<Tdl> tdlList = query.findList();

    return tdlList;
  }

  public static Tdl findByTestCaseAndVersion(TestCase testCase, String version) {
    return find.where().eq("testCase", testCase).eq("version", version).findUnique();
  }

  public static List<Tdl> listByTestCase(TestCase testCase) {
    return find.where().eq("testCase", testCase).orderBy().desc("creationDate").findList();
  }
}
