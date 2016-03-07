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
@Table(name = "TestAsset", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_group_id", "name"})
})
public class TestAsset extends Model{
  private static final Finder<Long, TestAsset> find = new Finder<>(TestAsset.class);

  @Id
  public Long id;

  @ManyToOne
  @Column(nullable = false)
  public TestGroup testGroup;

  /**
   * The name of the asset,
   */
  @Column(nullable = false)
  public String name;

  @Column(length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  public static List<TestAsset> findByGroup(TestGroup testGroup){
    return find.where().eq("testGroup", testGroup).orderBy("id").findList();
  }

  public static TestAsset findByGroup(TestGroup testGroup, String name) {
    return find.where().eq("testGroup", testGroup).eq("name", name).findUnique();
  }

  public static TestAsset findById(Long id) {
    return find.byId(id);
  }
}
