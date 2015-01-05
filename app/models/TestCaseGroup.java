package models;

import play.db.ebean.Model;

import javax.persistence.*;

import java.util.List;

/**
 * Represents an entity that might contain multiple test cases Created by
 * yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCaseGroup")
public class TestCaseGroup extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Column(unique = true, nullable = false)
	public String name;
	
	@ManyToOne
	@JoinColumn(name = "ID")
	public User user;

	@OneToMany(cascade = CascadeType.ALL)
	public List<TestAssertion> testAssertions;

	@Column(nullable = false, length = 50)
	public String shortDescription;

	public String description;

	public static final Finder<Long, TestCaseGroup> find = new Finder<Long, TestCaseGroup>(
			Long.class, TestCaseGroup.class);
}
