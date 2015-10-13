package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Represents an entity that might contain multiple test cases
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestGroup")
public class TestGroup extends Model {
  @Id
  public Long id;

  @Column(unique = true, nullable = false)
  public String name;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
  public List<TestAssertion> testAssertions;

  @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
  public List<UtilClass> utilClasses;

  @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
  public List<TestAsset> testAssets;

  @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
  public List<TestSuite> testSuites;


  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  @Column
  public String dependencyString;

  private static final Finder<Long, TestGroup> find = new Finder<>(
      Long.class, TestGroup.class);

  public static List<TestGroup> findByUser(User user){
    return find.where().eq("owner", user).setOrderBy("id").findList();
  }

  public static List<TestGroup> findAll(){
    return find.where().setOrderBy("id").findList();
  }

  public static TestGroup findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static TestGroup findById(Long id){
    TestGroup byId = find.byId(id);
    byId.owner = User.findById(byId.owner.id);
    return byId;
  }

  public static void updateUser(User user, User localUser) {
    //Ebean.createUpdate(TestGroup.class, )
  }
}
