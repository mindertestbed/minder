package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for JobList complex type.
 * </pre>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 07/12/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jobList", propOrder = {
        "restJobs"
})
@XmlRootElement(name = "jobList")
public class RestJobList {

    @XmlElementWrapper(name="restJobs")
    @XmlElement(required = false)
    public List<RestJob> restJobs;

    public List<RestJob> getRestJobs() {
        return restJobs;
    }

    public void setRestJobs(List<RestJob> restJobs) {
        this.restJobs = restJobs;
    }
}
