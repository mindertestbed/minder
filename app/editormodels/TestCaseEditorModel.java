package editormodels;

import models.ModelConstants;
import play.data.validation.Constraints;

/**
* Created by yerlibilgin on 03/05/15.
*/
public class TestCaseEditorModel {
  public Long id;

  public Long assertionId;

  @Constraints.Required
  public String name;

  @Constraints.Required
  @Constraints.MinLength(ModelConstants.MIN_DESC_LENGTH)
  @Constraints.MaxLength(ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;

  @Constraints.Required
  public String tdl;

  @Constraints.Required
  public String version;
}
