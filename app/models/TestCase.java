package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import minderengine.TestEngine;
import mtdl.SignalSlot;
import play.Logger;
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

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH)
  public String description;

  @Column(nullable = false, length = ModelConstants.MAX_TDL_LENGTH)
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

    System.out.printf("");

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

  @Override
  public void update() {
    super.update();
    detectParameters();
  }
}
