
package rest.models.runModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for minderError complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="minderError">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="inventoryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="errorSummary" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="errorLog" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "minderError", propOrder = {
    "inventoryId",
    "errorCode",
    "errorSummary",
    "errorLog"
})
public class MinderError {

    protected long inventoryId;
    protected int errorCode;
    @XmlElement(required = true)
    protected String errorSummary;
    protected byte[] errorLog;

    /**
     * Gets the value of the inventoryId property.
     * 
     */
    public long getInventoryId() {
        return inventoryId;
    }

    /**
     * Sets the value of the inventoryId property.
     * 
     */
    public void setInventoryId(long value) {
        this.inventoryId = value;
    }

    /**
     * Gets the value of the errorCode property.
     * 
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     */
    public void setErrorCode(int value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the errorSummary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorSummary() {
        return errorSummary;
    }

    /**
     * Sets the value of the errorSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorSummary(String value) {
        this.errorSummary = value;
    }

    /**
     * Gets the value of the errorLog property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getErrorLog() {
        return errorLog;
    }

    /**
     * Sets the value of the errorLog property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setErrorLog(byte[] value) {
        this.errorLog = value;
    }

}
