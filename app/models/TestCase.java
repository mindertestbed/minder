package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCase")
public class TestCase extends Model {
  @Id
  public long id;

  @ManyToOne
  @Column(nullable = false)
  public TestAssertion testAssertion;

  @Column(unique = true, nullable = false)
  public String name;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  private static final Finder<Long, TestCase> find = new Finder<>(Long.class,
      TestCase.class);

  @OneToMany
  public List<Tdl> tdls;

  public static TestCase findById(Long id) {
    TestCase byId = find.byId(id);
    byId.owner = User.findById(byId.owner.id);
    return byId;
  }

  public static TestCase findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  public static TestCase findByTestAssertionId(Long assertionId) {
    return find.where().eq("testAssertion.id", assertionId).findUnique();
  }

  public static List<TestCase> listByTestAssertionId(Long assertionId) {
    return find.where().eq("testAssertion.id", assertionId).findList();
  }

}
