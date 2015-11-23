package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 23/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "utilClassList", propOrder = {
        "restUtilClasses"
})
@XmlRootElement(name = "utilClassList")
public class RestUtilClassList {

    @XmlElementWrapper(name="utilClasses")
    @XmlElement(required = false)
    public List<RestUtilClass> restUtilClasses;

    public List<RestUtilClass> getRestUtilClasses() {
        return restUtilClasses;
    }

    public void setRestUtilClasses(List<RestUtilClass> restUtilClasses) {
        this.restUtilClasses = restUtilClasses;
    }
}
