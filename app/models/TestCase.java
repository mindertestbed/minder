package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "TestCase")
public class TestCase extends Model {
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

	@Column(nullable = false, length = 20000)
	public String tdl;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public List<WrapperParam> parameters;

	public static final Finder<Long, TestCase> find = new Finder<>(Long.class,
			TestCase.class);

	public static TestCase findById(Long id) {
		return find.byId(id);
	}

	public void setTdl(String tdl) {
		this.tdl = tdl;
		detectParameters();
	}

	public static TestCase findByName(String name) {
		return find.where().eq("name", name).findUnique();
	}
	
	public static TestCase findByTestAssertionId(Long assertionId) {
		return find.where().eq("testAssertion.id", assertionId).findUnique();
	}

	private void detectParameters() {
		Pattern pattern = Pattern.compile("of\\s+\"\\$[a-zA-Z0-9\\-_]+\"");
		Matcher matcher = pattern.matcher(tdl);

		LinkedHashSet<String> parms = new LinkedHashSet<>();
		while (matcher.find()) {
			String varName = tdl.substring(matcher.start(), matcher.end());
			varName = varName.substring(varName.indexOf('\"') + 1);
			varName = varName.substring(0, varName.indexOf('\"'));
			if (!parms.contains(varName))
				parms.add(varName);
		}
		for(String str : parms) {
			//check the database
			WrapperParam found = WrapperParam.findByTestCaseAndName(this, str);

			if (found == null){
				found = new WrapperParam();
				found.name = str;
				found.testCase = this;
				found.save();
			}
		}

	}
}
