package rest.models;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for ParametersForAdapters complex type.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 04/12/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parametersForAdapters", propOrder = {
    "id",
    "adapterParamId",
    "adapterVersionId"
})
@XmlRootElement(name = "ParametersForAdapters")
public class RestParametersForAdapters {
  @XmlElement(required = false)
  protected String id;

  @XmlElement(required = true)
  protected String adapterParamId;

  @XmlElement(required = true)
  protected String adapterVersionId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAdapterParamId() {
    return adapterParamId;
  }

  public void setAdapterParamId(String adapterParamId) {
    this.adapterParamId = adapterParamId;
  }

  public String getAdapterVersionId() {
    return adapterVersionId;
  }

  public void setAdapterVersionId(String adapterVersionId) {
    this.adapterVersionId = adapterVersionId;
  }
}
