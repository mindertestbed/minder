package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCase")
public class TestCase extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@ManyToOne
	@Column(nullable = false)
	public TestAssertion testAssertion;

	@Column(unique = true, nullable = false)
	public String name;

	@Column(nullable = false, length = 50)
	public String shortDescription;

	public String description;

	@Column(nullable = false, length = 10000)
	public String tdl;
	public String parameters;

	public static final Finder<Long, TestCase> find = new Finder<>(Long.class,
			TestCase.class);

	public static TestCase findById(Long id) {
		return find.byId(id);
	}
	public static TestCase findByName(String name) {
		return find.where().eq("name", name).findUnique();
	}
	
	public static TestCase findByTestAssertionId(Long assertionId) {
		return find.where().eq("testAssertion.id", assertionId).findUnique();
	}

}
