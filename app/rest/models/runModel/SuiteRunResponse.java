
package rest.models.runModel;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for suiteRunResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="suiteRunResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="suiteId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="suiteRunId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="testRuns" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "suiteRunResponse", propOrder = {
    "suiteId",
    "suiteRunId",
    "testRuns"
})
public class SuiteRunResponse {

    protected long suiteId;
    protected long suiteRunId;
    @XmlElement(nillable = true)
    protected List<Long> testRuns;

    /**
     * Gets the value of the suiteId property.
     * 
     */
    public long getSuiteId() {
        return suiteId;
    }

    /**
     * Sets the value of the suiteId property.
     * 
     */
    public void setSuiteId(long value) {
        this.suiteId = value;
    }

    /**
     * Gets the value of the suiteRunId property.
     * 
     */
    public long getSuiteRunId() {
        return suiteRunId;
    }

    /**
     * Sets the value of the suiteRunId property.
     * 
     */
    public void setSuiteRunId(long value) {
        this.suiteRunId = value;
    }

    /**
     * Gets the value of the testRuns property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the testRuns property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTestRuns().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getTestRuns() {
        if (testRuns == null) {
            testRuns = new ArrayList<Long>();
        }
        return this.testRuns;
    }

}
