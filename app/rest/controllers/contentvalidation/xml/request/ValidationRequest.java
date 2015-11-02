
package rest.controllers.contentvalidation.xml.request;

import javax.xml.bind.annotation.*;

/**
 *
 * <p>Java class for validationRequest complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="validationRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schema" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="schemaType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="schemaSubType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pathToSchema" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="document" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
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
@XmlType(name = "validationRequest", propOrder = {
    "schema",
    "schemaType",
    "schemaSubType",
    "pathToSchema",
    "document"
})
public class ValidationRequest {

    @XmlElement(required = true)
    protected byte[] schema;
    @XmlElement(required = true)
    protected String schemaType;
    @XmlElement(required = true)
    protected String schemaSubType;
    @XmlElement(required = true)
    protected byte[] document;
    @XmlElement(required = false)
    protected String pathToSchema;

    /**
     * Gets the value of the schema property.
     *
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     *
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSchema(byte[] value) {
        this.schema = value;
    }

    /**
     * Gets the value of the schemaType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSchemaType() {
        return schemaType;
    }

    /**
     * Sets the value of the schemaType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSchemaType(String value) {
        this.schemaType = value;
    }

    /**
     * Gets the value of the schemaSubType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSchemaSubType() {
        return schemaSubType;
    }

    /**
     * Sets the value of the schemaSubType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSchemaSubType(String value) {
        this.schemaSubType = value;
    }


    /**
     * Gets the value of the document property.
     *
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     *
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setDocument(byte[] value) {
        this.document = value;
    }

    /**
     * Gets the value of the pathToSchema property.
     *
     * @return
     *     possible object is
     *     String
     */
    public String getPathToSchema() {
        return pathToSchema;
    }

    /**
     * Sets the value of the pathToSchema property.
     *
     * @param value
     *     allowed object is
     *     String
     */
    public void setPathToSchema(String value) {
        this.pathToSchema = value;
    }

}
