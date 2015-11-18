package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for Wrapper complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="wrapper">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wrapperName" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "wrapperList", propOrder = {
        "restWrappers"
})
@XmlRootElement(name = "wrapperList")
public class RestWrapperList {

    @XmlElementWrapper(name="wrappers")
    @XmlElement(required = false)
    public List<RestWrapper> restWrappers;

    public List<RestWrapper> getRestWrappers() {
        return restWrappers;
    }

    public void setRestWrappers(List<RestWrapper> restWrappers) {
        this.restWrappers = restWrappers;
    }
}
