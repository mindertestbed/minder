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
  public String tdl;

  @Constraints.Required
  public String version;
}
