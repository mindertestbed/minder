
package rest.models.runModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for suiteRunStatusRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="suiteRunStatusRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="suiteRunId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "suiteRunStatusRequest", propOrder = {
    "suiteRunId"
})
public class SuiteRunStatusRequest {

    protected long suiteRunId;

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

}
