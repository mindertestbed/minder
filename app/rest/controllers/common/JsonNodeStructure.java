package rest.controllers.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 10/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jsonnodestructure", propOrder = {
        "id",
        "field",
        "newValue"
})
public class JsonNodeStructure {
    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String field;
    @XmlElement(required = true)
    protected String newValue;

    public String getId() {
        return id;
    }

    public String getField() {
        return field;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void setField(String field) {
        this.field = field;
    }
}
