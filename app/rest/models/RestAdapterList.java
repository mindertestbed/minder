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
 * @date: 18/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adapterList", propOrder = {
    "restAdapters"
})
@XmlRootElement(name = "adapterList")
public class RestAdapterList {

    @XmlElementWrapper(name="adapters")
    @XmlElement(required = false)
    public List<RestAdapter> restAdapters;

    public List<RestAdapter> getRestAdapters() {
        return restAdapters;
    }

    public void setRestAdapters(List<RestAdapter> restAdapters) {
        this.restAdapters = restAdapters;
    }
}
