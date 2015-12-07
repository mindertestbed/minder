package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 20/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testCaseList", propOrder = {
        "restTestCases"
})
@XmlRootElement(name = "testCaseList")
public class RestTestCaseList {

    @XmlElementWrapper(name="testCases")
    @XmlElement(required = false)
    public List<RestTestCase> restTestCases;

    public List<RestTestCase> getRestTestCases() {
        return restTestCases;
    }

    public void setRestTestCases(List<RestTestCase> restTestCases) {
        this.restTestCases = restTestCases;
    }
}
