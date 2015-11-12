
package rest.models;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for minderResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="minderResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 22/10/15.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "minderResponse", propOrder = {
    "result",
    "description"
})
@XmlRootElement(name = "minderResponse")
public class RestMinderResponse {

    @XmlElement(required = true)
    protected String result;

    @XmlElement(required = true)
    protected String description;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }


}
