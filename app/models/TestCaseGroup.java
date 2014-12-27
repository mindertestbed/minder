package models;

import javax.persistence.*;
import java.util.List;

/**
 * Represents an entity that might contain multiple test cases
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
public class TestCaseGroup extends MinderEntity{
  @Column(unique=true, nullable = false)
  public String name;

  @ManyToOne
  @Column(nullable = false)
  public TestCaseCategory category;

  @OneToMany(cascade = CascadeType.ALL)
  @Basic(fetch = FetchType.LAZY)
  public List<TestCase> testCaseList;

  @Column(nullable=false, length = 50)
  public String shortDescription;

  public String description;
}
