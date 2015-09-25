package models;

import com.avaje.ebean.ExpressionList;

import javax.persistence.*;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 15/09/15.
 */
@Entity
@DiscriminatorValue("1")
public class SuiteJob extends AbstractJob {
  @ManyToOne
  public TestSuite testSuite;

  private static final Finder<Long, SuiteJob> find = new Finder<>(
      SuiteJob.class);

  public static SuiteJob findById(Long id) {
    return find.byId(id);
  }

  public static List<SuiteJob> getAllByTestSuite(TestSuite testSuite) {
    return find.where().eq("testSuite", testSuite).orderBy("id").findList();
  }

  public static List<SuiteJob> findByTdl(Tdl tdl) {
    ExpressionList<SuiteJob> f = find.where().eq("tdl", tdl);
    return f.orderBy().desc("id").findList();
  }
}
