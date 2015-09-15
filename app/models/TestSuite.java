package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 14/09/15.
 */
@Entity
@Table(name = "TestSuite")
public class TestSuite extends Model {
  @Id
  public long id;

  @Column(unique = true, nullable = false)
  public String name;

  @Column(nullable = false, length = ModelConstants.SHORT_DESC_LENGTH, columnDefinition = "TEXT")
  public String shortDescription;

  @Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
  public String description;

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @ManyToOne
  @Column(name = "group", nullable = false)
  public TestGroup testGroup;


  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  public List<MappedWrapper> mappedWrappers;

  @ManyToMany
  @Column
  public List<Tdl> tdls;

  private static final Finder<Long, TestSuite> find = new Finder<>(TestSuite.class);


  public static List<TestSuite> findByGroup(TestGroup group) {
    return find.where().eq("testGroup", group).setOrderBy("id").findList();
  }
}
