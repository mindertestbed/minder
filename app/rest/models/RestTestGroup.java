package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for minderResponse complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="testGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="groupName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shortDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="descriptions" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 12/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testGroup", propOrder = {
        "id",
        "groupName",
        "shortDescription",
        "description",
        "owner",
        "dependencyString",
        "testassertions",
        "testassets"
})
@XmlRootElement(name = "testGroup")
public class RestTestGroup {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String groupName;

    @XmlElement(required = false)
    protected String shortDescription;

    @XmlElement(required = false)
    protected String description;

    @XmlElement(required = false)
    protected String owner;

    @XmlElement(required = false)
    protected String dependencyString;

    @XmlElementWrapper(name="testassertions")
    @XmlElement(required = false)
    public List<RestTestAssertion> testassertions;

    @XmlElementWrapper(name="testassets")
    @XmlElement(required = false)
    public List<RestTestAsset> testassets;

    @XmlElementWrapper(name="utilclasses")
    @XmlElement(required = false)
    public List<RestUtilClass> utilClasses;


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() { return owner;}

    public void setOwner(String owner) {this.owner = owner;}

    public String getDependencyString() {return dependencyString;}

    public void setDependencyString(String dependencyString) {this.dependencyString = dependencyString;}

    public List<RestTestAssertion> getTestassertions() {
        return testassertions;
    }

    public void setTestassertions(List<RestTestAssertion> testassertions) {
        this.testassertions = testassertions;
    }

    public List<RestTestAsset> getTestassets() {
        return testassets;
    }

    public void setTestassets(List<RestTestAsset> testassets) {
        this.testassets = testassets;
    }

    public List<RestUtilClass> getUtilClasses() {
        return utilClasses;
    }

    public void setUtilClasses(List<RestUtilClass> utilClasses) {
        this.utilClasses = utilClasses;
    }
}
