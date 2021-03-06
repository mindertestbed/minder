package editormodels;

import models.ModelConstants;
import minderengine.Visibility;
import play.data.validation.Constraints;

import java.util.List;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestSuiteEditorModel {
  public Long id;

  @Constraints.Required
  public String name;

  @Constraints.Required
  @Constraints.MinLength(ModelConstants.MIN_DESC_LENGTH)
  @Constraints.MaxLength(ModelConstants.SHORT_DESC_LENGTH)
  public String shortDescription;

  @Constraints.MaxLength(ModelConstants.DESCRIPTION_LENGTH)
  public String description;

  public Long groupId;

  public String mtdlParameters;

  public String selectedCandidateMap;

  public String tdlArray;

  public Visibility visibility;

  public PreemptionPolicy preemptionPolicy;
}
