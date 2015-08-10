package editormodels;

import controllers.MappedWrapperModel;
import play.data.validation.Constraints;

import java.util.List;

/**
* Created by yerlibilgin on 03/05/15.
*/
public class JobEditorModel {
  public Long id;

  @Constraints.Required
  public String name;

  public Long tdlID;

  public List<MappedWrapperModel> wrapperMappingList;

  public String mtdlParameters;
}
