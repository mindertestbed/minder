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
import views.html.authentication.login;
import views.html.authentication.*;

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
  public static User getLocalUser(Session session) {
    String email = session.get("email");
    if (email == null)
      return null;
    User user = User.findByEmail(email);
    return user;
  }

  public static Result login() {
    return ok(login.render(form(UserLoginEditorModel.class)));
  }

  public static Result doLogin() {
    final Form<UserLoginEditorModel> filledForm = form(UserLoginEditorModel.class).bindFromRequest();

    if (filledForm.hasErrors()) {
      Logger.debug("Login Form Has Errors");
      // User did not fill everything properly
      return badRequest(login.render(filledForm));
    }

    UserLoginEditorModel lgn = filledForm.get();

    final User byEmail = User.findByEmail(lgn.email);
    if (byEmail == null) {
      filledForm.reject("No User with email [" + lgn.email + "]");
      return badRequest(login.render(filledForm));
    }

    if(!Util.compareArray(Util.sha256(lgn.password.getBytes()), byEmail.password)){
      filledForm.reject("Wrong Password");
      return badRequest(login.render(filledForm));
    }

    session().clear();
    session().put("email", lgn.email);
    return redirect(routes.Application.testGroups());
  }

  public static Result doLogout() {
    session().clear();
    return redirect(routes.Authentication.login());
  }

  @Security.Authenticated(Secured.class)
  public static Result changePassword(){
    return Results.badRequest("Not supported");
  }

  @Security.Authenticated(Secured.class)
  public static Result doChangePassword(){
    return Results.badRequest("Not supported");
  }
}