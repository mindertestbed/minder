package models;

import java.util.List;

import javax.persistence.*;

import play.db.ebean.Model;

public class TestCaseRun extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@OneToOne(mappedBy="testCaseRun",fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	public User user;
	
	@OneToOne(mappedBy="testCaseRun",fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	public TestCase testCase;
	
	@OneToMany(mappedBy = "testCaseRun", fetch=FetchType.EAGER,cascade = CascadeType.ALL)
	public List<Wrapper> wrappers;
}
