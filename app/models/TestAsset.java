package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 13/01/15.
 *
 * Represents the ASSETS uploaded and used by users.
 */
@Entity
@Table(name = "TestAsset")
public class TestAsset extends Model{
  @Id
  public Long id;

  @ManyToOne
  @Column(name = "group", nullable = false)
  public TestGroup group;

  /**
   * The name of the asset,
   */
  @Column(nullable = false)
  public String name;

  private static final Finder<Long, TestAsset> find = new Finder<>(Long.class,
      TestAsset.class);

  @Column(length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  public static List<TestAsset> findByGroup(TestGroup group){
    return find.where().eq("group", group).orderBy("id").findList();
  }

  public static TestAsset findByGroup(TestGroup group, String name) {
    return find.where().eq("group", group).eq("name", name).findUnique();
  }

  public static TestAsset findById(Long id) {
    return find.byId(id);
  }
}
