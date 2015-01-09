package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCase")
public class TestCase extends Model {
  @Id
  public Long id;

  @ManyToOne
  @Column(nullable = false)
  public TestAssertion testAssertion;

  @Column(unique = true, nullable = false)
  public String name;

  @Column(nullable = false, length = 50)
  public String shortDescription;

  public String description;

  @Column(nullable = false, length = 20000)
  public String tdl;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public List<WrapperParam> parameters;

  public static final Finder<Long, TestCase> find = new Finder<>(Long.class,
      TestCase.class);

  public static TestCase findById(Long id) {
    return find.byId(id);
  }

  public void setTdl(String tdl) {
    this.tdl = tdl;
  }

  public static TestCase findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static TestCase findByTestAssertionId(Long assertionId) {
    return find.where().eq("testAssertion.id", assertionId).findUnique();
  }

  private void detectParameters() {
    deleteCurrentWrapperParamsOnDatabase();

    Pattern pattern = Pattern.compile("\"[a-zA-Z0-9_\\.\\[\\]\\(\\),\\s]+\"\\s+of\\s+\"\\$[a-zA-Z0-9\\-_]+\"");
    Matcher matcher = pattern.matcher(tdl);

    LinkedHashMap<String, WrapperParam> parms = new LinkedHashMap<>();
    while (matcher.find()) {
      String match = tdl.substring(matcher.start(), matcher.end());
      String[] split = match.split("\"");

      String signature = split[1];
      String wrapperVariable = split[3];

      System.out.println(signature + ":" + wrapperVariable);

      WrapperParam wrapperParam;
      if (parms.containsKey(wrapperVariable))
        wrapperParam = parms.get(wrapperVariable);
      else {
        wrapperParam = new WrapperParam();
        wrapperParam.name = wrapperVariable;
        wrapperParam.signatures = new ArrayList<>();
        wrapperParam.testCase = this;
        parms.put(wrapperVariable, wrapperParam);
      }


      ParamSignature ps = new ParamSignature();
      ps.signature = signature;
      ps.wrapperParam = wrapperParam;

      if (!wrapperParam.signatures.contains(ps)) {
        wrapperParam.signatures.add(ps);
      }

      this.parameters.add(wrapperParam);
    }

    try {
      Ebean.beginTransaction();

      Object[] set = parms.keySet().toArray();

      System.out.println("SET LENGTH: " + set.length);
      for(int i = 0; i < set.length; ++i){
        System.out.println("TO SAVE FOR " + i);
        WrapperParam str = parms.get(set[i].toString());
        str.save();
        System.out.println("TO SAVE FOR " + str.name);
        for (ParamSignature signature : str.signatures) {
          System.out.println("Param Signature " + signature.signature);
          signature.save();
        }
      }
      System.out.println("============");

      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();
    }


  }

  private void deleteCurrentWrapperParamsOnDatabase() {
    try {
      Ebean.beginTransaction();
      List<WrapperParam> all = WrapperParam.findByTestCase(this);

      for (WrapperParam wrapperParam : all) {
        wrapperParam.delete();
      }
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();
    }
  }


  @Override
  public void save() {
    super.save();
    detectParameters();
  }
}
