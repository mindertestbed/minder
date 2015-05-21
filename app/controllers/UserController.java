package controllers;

import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import editormodels.GroupEditorModel;
import editormodels.UserEditorModel;
import global.Util;
import models.*;
import org.mindrot.jbcrypt.BCrypt;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.App;
import views.html.groupDetailView;
import views.html.rootViews.userEditor;
import views.html.testGroupEditor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class UserController extends Controller {
  public static final Form<UserEditorModel> USER_FORM = form(UserEditorModel.class);


  public static Result getUserEditorView() {
    User localUser = Application.getLocalUser(session());
    if (localUser==null || !localUser.email.equals("root@minder")){
      return badRequest("You don't have the permission for this service");
    }

    return ok(userEditor.render(USER_FORM, true));
  }

  public static Result doCreateUser() {
    User localUser = Application.getLocalUser(session());
    if (localUser==null || !localUser.email.equals("root@minder")){
      return badRequest("You don't have the permission for this service");
    }
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
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
      user.active = toBoolean(model.active);
      user.name = model.name;

      user.roles  = new ArrayList<>();

      if (toBoolean(model.istd)){
        user.roles.add(SecurityRole.findByRoleName(Application.TEST_DEVELOPER_ROLE));
      }

      if (toBoolean(model.ists)){
        user.roles.add(SecurityRole.findByRoleName(Application.TEST_DESIGNER_ROLE));
      }

      if (toBoolean(model.isto)){
        user.roles.add(SecurityRole.findByRoleName(Application.OBSERVER_ROLE));
      }


      user.emailValidated=true;

      LinkedAccount la = new LinkedAccount();
      la.user = user;
      la.providerKey="password";
      la.providerUserId=BCrypt.hashpw(model.password, BCrypt.gensalt());

      user.save();
      la.save();
      return redirect(routes.Application.root("users"));
    }
  }

  private static boolean toBoolean(String active) {
    if (active == null) return false;
    return true;
  }

  public static Result editUserForm(Long id) {
    User localUser = Application.getLocalUser(session());
    if (localUser==null || !localUser.email.equals("root@minder")){
      return badRequest("You don't have the permission for this service");
    }

    User user = User.find.byId(id);
    if (user == null) {
      return badRequest("User with id [" + id + "] not found!");
    } else {
      UserEditorModel model = new UserEditorModel();

      model.id = user.id;
      model.email = user.email;
      model.name = user.name;
      model.active = user.active ? "true" : null;
      model.istd = user.isDeveloper() ? "true" : null;
      model.isto = user.isObserver() ? "true" : null;
      model.ists = user.isTester() ? "true" : null;

      Form<UserEditorModel> bind = USER_FORM.fill(model);

      return ok(userEditor.render(bind, false));
    }
  }

  public static Result doEditUser() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    User localUser = Application.getLocalUser(session());
    if (localUser==null || !localUser.email.equals("root@minder")){
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
        User user = User.find.byId(model.id);
        user.name = model.name;
        user.active = toBoolean(model.active);
        if (model.password != null) {
          LinkedAccount la = LinkedAccount.find.where().eq("user", user).findUnique();
          la.providerKey = "password";
          la.providerUserId = BCrypt.hashpw(model.password, BCrypt.gensalt());
          la.update();
        }

        if (toBoolean(model.istd)) {
          user.roles.add(SecurityRole.findByRoleName(Application.TEST_DEVELOPER_ROLE));
        } else {
          user.roles.remove(SecurityRole.findByRoleName(Application.TEST_DEVELOPER_ROLE));
        }

        if (toBoolean(model.ists)) {
          user.roles.add(SecurityRole.findByRoleName(Application.TEST_DESIGNER_ROLE));
        } else {
          user.roles.remove(SecurityRole.findByRoleName(Application.TEST_DESIGNER_ROLE));
        }

        if (toBoolean(model.isto)) {
          user.roles.add(SecurityRole.findByRoleName(Application.OBSERVER_ROLE));
        } else {
          user.roles.remove(SecurityRole.findByRoleName(Application.OBSERVER_ROLE));
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

  public static Result doDeleteUser(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    User localUser = Application.getLocalUser(session());
    if (localUser==null || !localUser.email.equals("root@minder")){
      return badRequest("You don't have the permission for this service");
    }

    User user = User.find.byId(id);
    if (user == null) {
      return badRequest("User with id [" + id + "] not found!");
    } else {

      if (user.email.equals("root@minder")){
        return badRequest("Seriously, is this kind of a joke?");
      }
      try {
        Ebean.beginTransaction();

        TestGroup.updateUser(user, localUser);
        TestAssertion.updateUser(user, localUser);
        TestCase.updateUser(user, localUser);
        Job.updateUser(user, localUser);
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
