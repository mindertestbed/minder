package models;

import com.avaje.ebean.Ebean;
import mtdl.TdlCompiler;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * /**
 * Created by yerlibilgin on 18/05/15.
 */
@Entity
@Table(name = "UtilClass")
public class UtilClass extends Model {
  @Id
  public long id;

  @ManyToOne
  @Column(nullable = false)
  public TestGroup testGroup;

  @Column(unique = true, nullable = false)
  public String name;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;

  @Column(nullable = false, length = ModelConstants.MAX_TDL_LENGTH)
  public String source;

  @ManyToOne
  @Column(nullable = false, name = "owner_id")
  public User owner;


  private static final Finder<Long, UtilClass> find = new Finder<>(Long.class,
      UtilClass.class);


  public static List<UtilClass> findAll() {
    return find.where().setOrderBy("id").findList();
  }

  public static UtilClass findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static UtilClass findById(Long id) {
    UtilClass byId = find.byId(id);
    byId.owner = User.findById(byId.owner.id);
    return byId;
  }


  @Override
  public void save() {
    try {
      Ebean.beginTransaction();
      super.save();


      String root = "_" + this.testGroup.id;
      TdlCompiler.compileUtil(root, root, testGroup.dependencyString, name, source);
      Ebean.commitTransaction();
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    } finally {
      Ebean.endTransaction();
    }
  }


  @Override
  public void update() {
    try {
      Ebean.beginTransaction();
      super.update();


      String root = "_" + this.testGroup.id;
      TdlCompiler.compileUtil(root, root, testGroup.dependencyString, name, source);
      Ebean.commitTransaction();
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    } finally {
      Ebean.endTransaction();
    }
  }
}

