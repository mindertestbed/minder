package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for Wrapper complex type.
 * </pre>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 19/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testGroupList", propOrder = {
        "restTestGroups"
})
@XmlRootElement(name = "testGroupList")
public class RestTestGroupList {

    @XmlElementWrapper(name="testGroups")
    @XmlElement(required = false)
    public List<RestTestGroup> restTestGroups;

    public List<RestTestGroup> getRestTestGroups() {
        return restTestGroups;
    }

    public void setRestTestGroups(List<RestTestGroup> restTestGroups) {
        this.restTestGroups = restTestGroups;
    }
}
