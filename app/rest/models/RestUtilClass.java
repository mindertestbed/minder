package rest.models;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for util class complex type.
 * <p>
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 23/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "utilClass", propOrder = {
        "id",
        "groupId",
        "name",
        "shortDescription",
        "source",
        "ownerName"
})
@XmlRootElement(name = "utilClass")
public class RestUtilClass {
    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String groupId;

    @XmlElement(required = false)
    protected String name;

    @XmlElement(required = false)
    protected String shortDescription;

    @XmlElement(required = false)
    protected byte[] source;

    @XmlElement(required = false)
    protected String ownerName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
