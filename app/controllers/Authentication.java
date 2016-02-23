package controllers;

import editormodels.UserLoginEditorModel;
import global.Util;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import security.AllowedRoles;
import security.Role;
import views.html.authentication.login;
import views.html.authentication.*;
import views.html.index;

import static play.data.Form.form;

public class Authentication extends Controller {
  public static final String FLASH_MESSAGE_KEY = "message";
  public static final String FLASH_ERROR_KEY = "error";


  public static User getLocalUser() {
    String email = session().get("email");
    if (email == null)
      return null;
    User user = User.findByEmail(email);
    return user;
  }

  public static String getRequestURI() {
    return request().uri();
  }

  public static User getLocalUser(Session session) {
    String email = session.get("email");
    if (email == null)
      return null;
    User user = User.findByEmail(email);
    return user;
  }

  public static Result login() {
    return ok(index.render(null));
  }

  public static Result loginToTargetURL(String nextTarget) {
    return ok(index.render(nextTarget));
  }

  public static Result doLogin() {
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

  public static Result doLogout() {
    session().clear();
    return redirect(routes.Authentication.login());
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result changePassword() {
    return Results.badRequest("Not supported");
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result doChangePassword() {
    return Results.badRequest("Not supported");
  }
}