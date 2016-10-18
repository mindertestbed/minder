package rest.models;

import javax.xml.bind.annotation.*;

/**
 * <p>Java class for ParametersForWrappers complex type.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 04/12/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parametersForWrappers", propOrder = {
    "id",
    "wrapperParamId",
    "wrapperVersionId"
})
@XmlRootElement(name = "ParametersForWrappers")
public class RestParametersForWrappers {
  @XmlElement(required = false)
  protected String id;

  @XmlElement(required = true)
  protected String wrapperParamId;

  @XmlElement(required = true)
  protected String wrapperVersionId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getWrapperParamId() {
    return wrapperParamId;
  }

  public void setWrapperParamId(String wrapperParamId) {
    this.wrapperParamId = wrapperParamId;
  }

  public String getWrapperVersionId() {
    return wrapperVersionId;
  }

  public void setWrapperVersionId(String wrapperVersionId) {
    this.wrapperVersionId = wrapperVersionId;
  }
}
