package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for minderResponse complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 18/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testCase", propOrder = {
        "id",
        "testAssertionId",
        "name",
        "owner",
        "tdls"
})
@XmlRootElement(name = "testCase")
public class RestTestCase {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String testAssertionId;

    @XmlElement(required = false)
    protected String name;

    @XmlElement(required = false)
    protected String owner;

    @XmlElementWrapper(name="tdls")
    @XmlElement(required = false)
    public List<RestTdl> tdls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestAssertionId() {
        return testAssertionId;
    }

    public void setTestAssertionId(String testAssertionId) {
        this.testAssertionId = testAssertionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<RestTdl> getTdls() {
        return tdls;
    }

    public void setTdls(List<RestTdl> tdls) {
        this.tdls = tdls;
    }
}
