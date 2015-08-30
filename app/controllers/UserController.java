package controllers;

import com.avaje.ebean.Ebean;
import editormodels.UserEditorModel;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.rootViews.userEditor;

import java.util.ArrayList;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class UserController extends Controller {
  public static final Form<UserEditorModel> USER_FORM = form(UserEditorModel.class);

  @Security.Authenticated(Secured.class)
  public static Result getUserEditorView() {
    User localUser = Authentication.getLocalUser(session());
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }

    return ok(userEditor.render(USER_FORM, true));
  }

  public static Result doCreateUser() {
    User localUser = Authentication.getLocalUser(session());
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }
    final Form<UserEditorModel> filledForm = USER_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(userEditor.render(filledForm, true));
    } else {
      UserEditorModel model = filledForm.get();
      User user = User.findByEmail(model.email);
      if (user != null) {
        filledForm.reject("The group with name [" + user.email
            + "] already exists");
        return badRequest(userEditor.render(filledForm, true));
      }

      if (!model.password.equals(model.repeatPassword)) {
        filledForm.reject("Passwords don't match!");
        return badRequest(userEditor.render(filledForm, true));
      }

      user = new User();

      user.email = model.email;
      user.name = model.name;

      user.roles = new ArrayList<>();

      if (toBoolean(model.istd)) {
        user.roles.add(Role.TEST_DEVELOPER);
      }

      if (toBoolean(model.ists)) {
        user.roles.add(Role.TEST_DESIGNER);
      }

      if (toBoolean(model.isto)) {
        user.roles.add(Role.TEST_OBSERVER);
      }

      user.password = Util.sha256(model.password.getBytes());

      user.save();
      return redirect(routes.Application.root("users"));
    }
  }

  private static boolean toBoolean(String active) {
    if (active == null) return false;
    return true;
  }

  @Security.Authenticated(Secured.class)
  public static Result editUserForm(Long id) {
    User localUser = Authentication.getLocalUser(session());
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }

    User user = User.findById(id);
    if (user == null) {
      return badRequest("User with id [" + id + "] not found!");
    } else {
      UserEditorModel model = new UserEditorModel();

      model.id = user.id;
      model.email = user.email;
      model.name = user.name;
      model.istd = user.isDeveloper() ? "true" : null;
      model.isto = user.isObserver() ? "true" : null;
      model.ists = user.isTester() ? "true" : null;

      Form<UserEditorModel> bind = USER_FORM.fill(model);

      return ok(userEditor.render(bind, false));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditUser() {
    User localUser = Authentication.getLocalUser(session());
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }

    final Form<UserEditorModel> filledForm = USER_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(userEditor.render(filledForm, false));
    } else {
      UserEditorModel model = filledForm.get();

      System.out.println(model.password);
      System.out.println(model.repeatPassword);
      if (model.password != null && !model.password.equals(model.repeatPassword)) {
        filledForm.reject("Passwords don't match!");
        return badRequest(userEditor.render(filledForm, false));
      }

      try {
        User user = User.findById(model.id);
        user.name = model.name;
        if (model.password != null) {
          user.password = Util.sha256(model.password.getBytes());
        }

        if (toBoolean(model.istd)) {
          user.roles.add(Role.TEST_DEVELOPER);
        } else {
          user.roles.remove(Role.TEST_DEVELOPER);
        }

        if (toBoolean(model.ists)) {
          user.roles.add(Role.TEST_DESIGNER);
        } else {
          user.roles.remove(Role.TEST_DESIGNER);
        }

        if (toBoolean(model.isto)) {
          user.roles.add(Role.TEST_OBSERVER);
        } else {
          user.roles.remove(Role.TEST_OBSERVER);
        }
        user.update();
        Logger.info("Done updating user " + user.email);
        return redirect(routes.Application.root("users"));

      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        filledForm.reject(ex.getMessage());
        return badRequest(userEditor.render(filledForm, false));
      } finally {
      }
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteUser(Long id) {
    User localUser = Authentication.getLocalUser(session());
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }

    User user = User.findById(id);
    if (user == null) {
      return badRequest("User with id [" + id + "] not found!");
    } else {

      if (user.email.equals("root@minder")) {
        return badRequest("Seriously, is this kind of a joke?");
      }
      try {
        Ebean.beginTransaction();

        TestGroup.updateUser(user, localUser);
        TestAssertion.updateUser(user, localUser);
        //TestCase.updateUser(user, localUser);
        //Job.updateUser(user, localUser);
        TestRun.updateUser(user, localUser);
        user.delete();

        Ebean.commitTransaction();
      } catch (Exception ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      } finally {
        Ebean.endTransaction();
      }
      return redirect(routes.Application.root("users"));
    }
  }

}
