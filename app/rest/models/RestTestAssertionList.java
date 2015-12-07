package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 20/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testAssertionList", propOrder = {
        "restTestAssertions"
})
@XmlRootElement(name = "testAssertionList")
public class RestTestAssertionList {
    @XmlElementWrapper(name="testAssertions")
    @XmlElement(required = false)
    public List<RestTestAssertion> restTestAssertions;

    public List<RestTestAssertion> getRestTestAssertions() {
        return restTestAssertions;
    }

    public void setRestTestAssertions(List<RestTestAssertion> restTestAssertions) {
        this.restTestAssertions = restTestAssertions;
    }
}
