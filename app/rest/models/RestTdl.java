package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for RestTdl complex type.

 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 19/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tdl", propOrder = {
        "id",
        "testCaseId",
        "tdl",
        "version",
        "creationDate",
        "parameters"
})
@XmlRootElement(name = "tdl")
public class RestTdl {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String testCaseId;

    @XmlElement(required = false)
    protected byte[] tdl;

    @XmlElement(required = false)
    protected String version;

    @XmlElement(required = false)
    protected String creationDate;

    @XmlElementWrapper(name="adapterParameters")
    @XmlElement(required = false)
    public List<RestTdlAdapterParam> parameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public byte[] getTdl() {
        return tdl;
    }

    public void setTdl(byte[] tdl) {
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

    public List<RestTdlAdapterParam> getParameters() {
        return parameters;
    }

    public void setParameters(List<RestTdlAdapterParam> parameters) {
        this.parameters = parameters;
    }
}
