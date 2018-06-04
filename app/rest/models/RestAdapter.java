package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for Adapter complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="adapter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="adapterName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shortDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 17/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adapter", propOrder = {
        "id",
         "adapterName",
        "shortDescription",
        "description",
        "userName",
        "restAdapterVersion"
})
@XmlRootElement(name = "adapter")
public class RestAdapter {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String adapterName;

    @XmlElement(required = false)
    protected String shortDescription;

    @XmlElement(required = false)
    protected String description;

    @XmlElement(required = false)
    protected String userName;

    @XmlElementWrapper(name="adapterversions")
    @XmlElement(required = false)
    public List<RestAdapterVersion> restAdapterVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<RestAdapterVersion> getRestAdapterVersion() {
        return restAdapterVersion;
    }

    public void setRestAdapterVersion(List<RestAdapterVersion> restAdapterVersion) {
        this.restAdapterVersion = restAdapterVersion;
    }
}
