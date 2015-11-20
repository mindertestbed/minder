package rest.models;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for RestTdl complex type.

 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 19/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tdl", propOrder = {
        "id",
        "testCaseName",
        "tdl",
        "version",
        "creationDate"
})
@XmlRootElement(name = "tdl")
public class RestTdl {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String testCaseName;

    @XmlElement(required = false)
    protected String tdl;

    @XmlElement(required = false)
    protected String version;

    @XmlElement(required = false)
    protected String creationDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getTdl() {
        return tdl;
    }

    public void setTdl(String tdl) {
        this.tdl = tdl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
