package editormodels;

import models.ModelConstants;
import play.data.validation.Constraints;

/**
* Created by yerlibilgin on 03/05/15.
*/
public class AssertionEditorModel {
  public Long id;

  public Long groupId;

  @Constraints.Required
  public String taId;

  @Constraints.Required
  @Constraints.MaxLength(ModelConstants.DESCRIPTION_LENGTH)
  public String normativeSource;

  @Constraints.Required
  public String target;

  @Constraints.MaxLength(ModelConstants.DESCRIPTION_LENGTH)
  public String prerequisites;

  @Constraints.Required
  @Constraints.MaxLength(ModelConstants.DESCRIPTION_LENGTH)
  public String predicate;

  @Constraints.MaxLength(ModelConstants.K)
  public String variables;


  @Constraints.MaxLength(ModelConstants.K)
  public String tag;

  @Constraints.Required
  @Constraints.MinLength(ModelConstants.MIN_DESC_LENGTH)
  @Constraints.MaxLength(ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;


  @Constraints.MaxLength(ModelConstants.DESCRIPTION_LENGTH)
  public String description;

  public String prescriptionLevel;
}
