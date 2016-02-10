package controllers;

import com.avaje.ebean.Ebean;
import editormodels.UserCreatorModel;
import editormodels.UserEditorModel;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.Role;
import views.html.rootViews.userEditor;
import views.html.rootViews.userListView;
import views.html.rootViews.settingsView;
import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class UserController extends Controller {
  public static final Form<UserEditorModel> USER_EDIT_FORM = form(UserEditorModel.class);
  public static final Form<UserCreatorModel> USER_CREATE_FORM = form(UserCreatorModel.class);

  @Security.Authenticated(Secured.class)
  public static Result getUserEditorView() {
    User localUser = Authentication.getLocalUser();
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }

    return ok(userEditor.render(USER_CREATE_FORM, true));
  }

  public static Result doCreateUser() {
    User localUser = Authentication.getLocalUser();
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }
    final Form<UserCreatorModel> filledForm = USER_CREATE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(userEditor.render(filledForm, true));
    } else {
      UserCreatorModel model = filledForm.get();
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
        user.roles.add(new DBRole(user, Role.TEST_DEVELOPER));
      }

      if (toBoolean(model.ists)) {
        user.roles.add(new DBRole(user, Role.TEST_DESIGNER));
      }

      if (toBoolean(model.isto)) {
        user.roles.add(new DBRole(user, Role.TEST_OBSERVER));
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
    User localUser = Authentication.getLocalUser();
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

      Form<UserEditorModel> bind = USER_EDIT_FORM.fill(model);

      return ok(userEditor.render(bind, false));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditUser() {
    User localUser = Authentication.getLocalUser();
    if (localUser == null || !localUser.email.equals("root@minder")) {
      return badRequest("You don't have the permission for this service");
    }

    final Form<UserEditorModel> filledForm = USER_EDIT_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(userEditor.render(filledForm, false));
    } else {
      UserEditorModel model = filledForm.get();
      if (model.password != null && !model.password.equals(model.repeatPassword)) {
        filledForm.reject("Passwords don't match!");
        return badRequest(userEditor.render(filledForm, false));
      }

      try {
        Ebean.beginTransaction();
        User user = User.findById(model.id);
        user.name = model.name;

        DBRole.deleteAllByUser(user);
        if (model.password != null && model.password.length() >= 5) {
          user.password = Util.sha256(model.password.getBytes());
        }

        if (toBoolean(model.istd)) {
          user.roles.add(new DBRole(user, Role.TEST_DEVELOPER));
        }

        if (toBoolean(model.ists)) {
          user.roles.add(new DBRole(user, Role.TEST_DESIGNER));
        }

        if (toBoolean(model.isto)) {
          user.roles.add(new DBRole(user, Role.TEST_OBSERVER));
        }

        user.update();
        Logger.info("Done updating user " + user.email);
        Ebean.commitTransaction();
        return redirect(routes.Application.root("users"));
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        filledForm.reject(ex.getMessage());
        return badRequest(userEditor.render(filledForm, false));
      } finally {
        Ebean.endTransaction();
      }
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteUser(Long id) {
    User localUser = Authentication.getLocalUser();
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
  @Security.Authenticated(Secured.class)
  public static Result listUsers(String string) {
    final User localUser = Authentication.getLocalUser();
    if (localUser == null)
      return badRequest("You cannot access this resoruce.");

    if (localUser.email.equals("root@minder")) {
      return ok(userListView.render());
    } else {
      return badRequest("You cannot access this resoruce.");
    }
  }
  @Security.Authenticated(Secured.class)
  public static Result viewSettings(String string) {
    final User localUser = Authentication.getLocalUser();
    if (localUser == null)
      return badRequest("You cannot access this resoruce.");

    if (localUser.email.equals("root@minder")) {
      return ok(settingsView.render());
    } else {
      return badRequest("You cannot access this resoruce.");
    }
  }

}
