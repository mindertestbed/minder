package rest.models;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for minderResponse complex type.
 * <p>
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 20/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testAsset", propOrder = {
        "id",
        "groupId",
        "name",
        "shortDescription",
        "description",
        "asset",
})
@XmlRootElement(name = "testAsset")
public class RestTestAsset {

    @XmlElement(required = false)
    protected String id;

    @XmlElement(required = false)
    protected String groupId;

    @XmlElement(required = false)
    protected String name;

    @XmlElement(required = false)
    protected String shortDescription;

    @XmlElement(required = false)
    protected String description;

    @XmlElement(required = false)
    protected byte[] asset;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getAsset() {
        return asset;
    }

    public void setAsset(byte[] asset) {
        this.asset = asset;
    }
}
