package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for RestTdlWrapperParam complex type.

 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 04/12/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tdlWrapperParam", propOrder = {
        "id",
        "name"
})
@XmlRootElement(name = "tdlWrapperParam")
public class RestTdlWrapperParam {
    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String name;

    @XmlElementWrapper(name="signatures")
    @XmlElement(required = false)
    public List<String> signatures;

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

    public List<String> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<String> signatures) {
        this.signatures = signatures;
    }
}
