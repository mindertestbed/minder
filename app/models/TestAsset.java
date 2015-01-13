package models;

import play.db.ebean.Model;

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
  @Column(name = "owner", nullable = false)
  public User owner;

  /**
   * The name of the asset,
   */
  @Column(nullable = false)
  public String name;

  public static final Finder<Long, TestAsset> find = new Finder<>(Long.class,
      TestAsset.class);

  @Column(length = ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH)
  public String description;

  public static List<TestAsset> findByOwner(User owner){
    return find.where().eq("owner", owner).orderBy("id").findList();
  }

  public static TestAsset findByUserAndName(User owner, String name) {
    return find.where().eq("owner", owner).eq("name", name).findUnique();
  }
}
