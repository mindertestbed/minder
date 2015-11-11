package rest.controllers.testgroup;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for minderResponse complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="dependencyString">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="groupId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 11/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dependencyString", propOrder = {
        "groupId",
        "value"
})
@XmlRootElement(name = "dependencyString")
public class DependencyString {

    @XmlElement(required = true)
    protected String groupId;

    @XmlElement(required = true)
    protected String value;

    public String getGroupId() {
        return groupId;
    }

    public String getValue() {
        return value;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
