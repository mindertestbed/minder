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

  public Long testCaseId;

  public boolean obsolete;

  public String tdl;

  public List<MappedWrapperModel> mappedWrappers;
}
