package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.ebean.Model;

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


  @Column(length = ModelConstants.DESCRIPTION_LENGTH)
  public String description;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;

  public static final Finder<Long, TestGroup> find = new Finder<>(
      Long.class, TestGroup.class);

  public static List<TestGroup> findByUser(User user){
    return find.where().eq("owner", user).setOrderBy("id").findList();
  }

  public static TestGroup findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static TestGroup findById(Long id){
    return find.byId(id);
  }

}
