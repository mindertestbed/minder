package controllers;

import editormodels.UserLoginEditorModel;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import utils.Util;
import views.html.authentication.changePasswordForm;
import views.html.index;

import javax.inject.Inject;

import static play.data.Form.form;

public class Authentication extends Controller {
  public static final String FLASH_MESSAGE_KEY = "message";
  public static final String FLASH_ERROR_KEY = "error";


  private final Form<ChangePasswordModel> form;

  public static class ChangePasswordModel {
    @Constraints.Required
    public String password;

    @Constraints.Required
    public String passwordAgain;
  }


  @Inject
  public Authentication(FormFactory formFactory) {
    this.form = formFactory.form(ChangePasswordModel.class);
  }

  public User getLocalUser() {
    String email = session().get("email");
    if (email == null)
      return null;
    User user = User.findByEmail(email);
    return user;
  }


  public String getRequestURI() {
    return request().uri();
  }

  public static User getLocalUser(Session session) {
    String email = session.get("email");
    if (email == null)
      return null;
    User user = User.findByEmail(email);
    return user;
  }

  public Result login() {
    return ok(index.render(null, this));
  }

  public Result loginToTargetURL(String nextTarget) {
    return ok(index.render(nextTarget, this));
  }

  public Result doLogin() {
    final Form<UserLoginEditorModel> filledForm = form(UserLoginEditorModel.class).bindFromRequest();

    if (filledForm.hasErrors()) {
      Logger.debug("Login Form Has Errors");
      return unauthorized("Plese provide a valid username and password");
    }

    UserLoginEditorModel lgn = filledForm.get();

    final User byEmail = User.findByEmail(lgn.email);
    if (byEmail == null) {
      return unauthorized("Plese provide a valid username and password");
    }

    if (!Util.compareArray(Util.sha256(lgn.password.getBytes()), byEmail.password)) {
      return unauthorized("Plese provide a valid username and password");
    }

    session().clear();
    session().put("email", lgn.email);
    if (lgn.path != null) {
      return redirect(lgn.path);
    }
    return ok("");
  }

  public Result doLogout() {
    session().clear();
    return redirect(routes.Authentication.login());
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public Result changePassword() {
    return ok(changePasswordForm.render(form, this));
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public Result doChangePassword() {
    ChangePasswordModel changePasswordModel = form.bindFromRequest(request()).get();
    if (changePasswordModel.password.equals(changePasswordModel.passwordAgain)) {
      User user = getLocalUser();
      if (user != null) {
        user.setPlainPassword(changePasswordModel.password);
        user.save();
        return redirect(routes.Application.index());
      }
      return badRequest("User does not exist");
    }
    return badRequest("Passwords don't match");
  }
}