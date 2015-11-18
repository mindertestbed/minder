package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for minderResponse complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 18/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testAssertion", propOrder = {
        "id",
        "groupName",
        "testAssertionId",
        "normativeSource",
        "target",
        "prerequisites",
        "predicate",
        "variables",
        "tag",
        "description",
        "shortDescription",
        "prescriptionLevel",
        "owner"
})
@XmlRootElement(name = "testAssertion")
public class RestTestAssertion {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String groupName;

    @XmlElement(required = false)
    protected String testAssertionId;

    @XmlElement(required = false)
    protected String normativeSource;

    @XmlElement(required = false)
    protected String target;

    @XmlElement(required = false)
    protected String prerequisites;

    @XmlElement(required = false)
    protected String predicate;

    @XmlElement(required = false)
    protected String variables;

    @XmlElement(required = false)
    protected String tag;

    @XmlElement(required = false)
    protected String description;

    @XmlElement(required = false)
    protected String shortDescription;

    @XmlElement(required = false)
    protected String prescriptionLevel;

    @XmlElement(required = false)
    protected String owner;

    @XmlElementWrapper(name="testcases")
    @XmlElement(required = false)
    public List<RestTestCase> testcases;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTestAssertionId() {
        return testAssertionId;
    }

    public void setTestAssertionId(String testAssertionId) {
        this.testAssertionId = testAssertionId;
    }

    public String getNormativeSource() {
        return normativeSource;
    }

    public void setNormativeSource(String normativeSource) {
        this.normativeSource = normativeSource;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getPrescriptionLevel() {
        return prescriptionLevel;
    }

    public void setPrescriptionLevel(String prescriptionLevel) {
        this.prescriptionLevel = prescriptionLevel;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<RestTestCase> getTestcases() {
        return testcases;
    }

    public void setTestcases(List<RestTestCase> testcases) {
        this.testcases = testcases;
    }
}
