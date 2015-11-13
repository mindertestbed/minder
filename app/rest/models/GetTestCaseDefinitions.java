package rest.models;

import java.util.ArrayList;
import java.util.List;

import com.gitb.tpl.v1.TestCase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTestCaseDefinitions", propOrder = { "testcases" })
@XmlRootElement(name = "GetTestCaseDefinitions")
public class GetTestCaseDefinitions {

	@XmlElement(required = true)
	protected List<TestCase> testcases;

	public List<TestCase> getTestCase() {
        if (testcases == null) {
        	testcases = new ArrayList<TestCase>();
        }
        return this.testcases;
    }

	public void setTestcases(List<TestCase> testcases) {
		this.testcases = testcases;
	}
	
	
}
