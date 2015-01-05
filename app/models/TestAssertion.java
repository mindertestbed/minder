package models;

import play.db.ebean.Model;

import javax.persistence.*;

import java.util.List;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestAssertion")
public class TestAssertion extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Column(nullable = false, unique = true)
	public String taId;

	@Column(nullable = false)
	public String normativeSource;

	@Column(nullable = false)
	public String target;

	public String prerequisites;

	@Column(nullable = false)
	public String predicate;

	public String variables;

	@ManyToOne
	@JoinColumn(name = "ID")
	public TestCaseGroup testCaseGroup;

	@OneToMany(mappedBy = "testAssertion", fetch=FetchType.EAGER,cascade = CascadeType.ALL)
	public List<TestCase> testCases;
}
