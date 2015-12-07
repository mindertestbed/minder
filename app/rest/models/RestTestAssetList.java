package rest.models;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 20/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testAssetList", propOrder = {
        "restTestAssets"
})
@XmlRootElement(name = "testAssetList")
public class RestTestAssetList {

    @XmlElementWrapper(name="testAssets")
    @XmlElement(required = false)
    public List<RestTestAsset> restTestAssets;

    public List<RestTestAsset> getRestTestAssets() {
        return restTestAssets;
    }

    public void setRestTestAssets(List<RestTestAsset> restTestAssets) {
        this.restTestAssets = restTestAssets;
    }
}
