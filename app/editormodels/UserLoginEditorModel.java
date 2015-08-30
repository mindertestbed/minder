package editormodels;

import play.data.validation.Constraints;

/**
* Created by yerlibilgin on 29/08/2015.
*/
public class UserLoginEditorModel {
  public Long id;

  @Constraints.Email
  @Constraints.Required
  public String email;

  @Constraints.Required
  @Constraints.MinLength(5)
  public String password;
}
