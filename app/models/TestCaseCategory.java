package models;

import javax.persistence.*;
import java.util.List;

/**
 * Represents a test case category that might point to
 * Multiple Test Case Groups
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
public class TestCaseCategory extends MinderEntity{

  @ManyToOne
  @Column(nullable = false)
  public User owner;

  @Column(unique=true, nullable = false)
  public String name;

  @OneToMany(cascade = CascadeType.ALL)
  public List<TestCaseGroup> testCaseGroup;

  @Column(nullable=false, length = 50)
  public String shortDescription;

  public String description;

  public static final Finder<Long, TestCaseCategory> find = new Finder<Long, TestCaseCategory>(
      Long.class, TestCaseCategory.class);
}
