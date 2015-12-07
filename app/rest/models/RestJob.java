package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for RestJob complex type.

 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 03/12/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "job", propOrder = {
        "id",
        "name",
        "tdlId",
        "owner",
        "mtdlParameters"
})
@XmlRootElement(name = "job")
public class RestJob {
    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String name;

    @XmlElement(required = false)
    protected String tdlId;

    @XmlElement(required = false)
    protected String owner;

    @XmlElement(required = false)
    protected String mtdlParameters;

    @XmlElementWrapper(name="parametersForWrappers")
    @XmlElement(required = false)
    protected List<RestParametersForWrappers> parametersForWrappers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTdlId() {
        return tdlId;
    }

    public void setTdlId(String tdlId) {
        this.tdlId = tdlId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMtdlParameters() {
        return mtdlParameters;
    }

    public void setMtdlParameters(String mtdlParameters) {
        this.mtdlParameters = mtdlParameters;
    }

    public List<RestParametersForWrappers> getParametersForWrappers() {
        return parametersForWrappers;
    }

    public void setParametersForWrappers(List<RestParametersForWrappers> parametersForWrappers) {
        this.parametersForWrappers = parametersForWrappers;
    }
}
