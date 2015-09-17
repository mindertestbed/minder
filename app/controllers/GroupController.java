package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dependencyutils.DependencyClassLoaderCache;
import editormodels.GroupEditorModel;
import global.Global;
import global.Util;
import models.TestGroup;
import models.User;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.AllowedRoles;
import security.Role;
import views.html.testDesigner.group.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class GroupController extends Controller {
  public static final Form<GroupEditorModel> TEST_GROUP_FORM = form(GroupEditorModel.class);

  @Security.Authenticated(Secured.class)
  public static Result getCreateGroupEditorView() {
    return ok(testGroupEditor.render(TEST_GROUP_FORM));
  }

  @Security.Authenticated(Secured.class)
  public static Result doCreateTestGroup() {
    final Form<GroupEditorModel> filledForm = TEST_GROUP_FORM
        .bindFromRequest();
    final User localUser = Authentication.getLocalUser();
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testGroupEditor.render(filledForm));
    } else {
      GroupEditorModel model = filledForm.get();

      TestGroup group = TestGroup.findByName(model.name);
      if (group != null) {
        filledForm.reject("The group with name [" + group.name + "] already exists");
        return badRequest(testGroupEditor.render(filledForm));
      }

      group = new TestGroup();
      group.owner = localUser;
      group.shortDescription = model.shortDescription;
      group.description = model.description;
      group.name = model.name;
      group.dependencyString = "";

      group.save();
      return redirect(routes.Application.restrictedTestDesigner("testGroups"));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result editGroupForm(Long id) {
    final User localUser = Authentication.getLocalUser();
    TestGroup tg = TestGroup.findById(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {

      if (!Util.canAccess(Authentication.getLocalUser(), tg.owner))
        return badRequest("You don't have permission to modify this resource");

      GroupEditorModel tgem = new GroupEditorModel();
      tgem.id = tg.id;
      tgem.name = tg.name;
      tgem.shortDescription = tg.shortDescription;
      tgem.description = tg.description;
      Form<GroupEditorModel> bind = TEST_GROUP_FORM.fill(tgem);

      return ok(testGroupEditor.render(bind));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditGroupField() {
    JsonNode jsonNode = request().body().asJson();

    Result result = ok();
    try {
      Ebean.beginTransaction();
      result = doEditField(GroupEditorModel.class, TestGroup.class, jsonNode);

      String field = jsonNode.findPath("field").asText();

      if (field.equals("dependencyString")) {
        String dependencyString = jsonNode.findPath("newValue").asText();
        if (dependencyString != null)
          dependencyString = dependencyString.trim();
        if (dependencyString == null || dependencyString.length() == 0) {
          return result; // do nothing
        } else {
          try {
            DependencyClassLoaderCache.getDependencyClassLoader(dependencyString);
            Ebean.commitTransaction();
          } catch (Exception ex) {
            Logger.error(ex.getMessage(), ex);
            return badRequest("There was a problem with the dependency string.<br /> \n" +
                "Please make sure that the dependencies are in format:<br />\n " +
                "groupId:artifactId[:extension[:classifier]]:version]]");
          }
        }
      }
    } finally {
      Ebean.endTransaction();
    }
    return result;
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditField(Class<?> editorClass, Class<?> cls, JsonNode jsonNode) {
    long id = jsonNode.findPath("id").asInt();
    String field = jsonNode.findPath("field").asText();
    String newValue = jsonNode.findPath("newValue").asText();
    String converter = jsonNode.findPath("converter").asText();

    System.out.println("Converter3 [" + (converter == null) + "]");
    try {
      try {
        Util.checkField(editorClass, field, newValue);
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }

      //check access.
      // first lets see if we have a field named owner
      try {
        Field fld = cls.getDeclaredField("owner");

        //we have an owner. Lets verify if we are the rightfull owner

        String queryString = "find " + cls.getSimpleName() + " where id = :id";
        Query<?> query = Ebean.createQuery(cls, queryString);
        query.setParameter("id", id);
        Object o = query.findUnique();
        User user = (User) fld.get(o);
        System.out.println("UNique " + user.email);

        User localUser = Authentication.getLocalUser();

        if (localUser == null || !localUser.email.equals(user.email)) {
          return badRequest("You don't have the permission to modify this resource");
        }
      } catch (NoSuchFieldException ex) {

      }
      String updStatement = "update " + cls.getSimpleName() + " set " + field + " = :value where id = :id";
      Update<?> update = Ebean.createUpdate(cls, updStatement);

      Object newValueConverted = newValue;
      if (converter != null && converter.length() != 0)
        newValueConverted = Util.convertValue(converter, newValue);

      update.set("value", newValueConverted);
      update.set("id", id);
      update.execute();
      ObjectNode node = Json.newObject();
      node.put("value", newValueConverted.toString());
      return ok(node.toString());
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      e.printStackTrace();
      return badRequest(sw.toString());
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteGroup(Long id) {
    TestGroup tg = TestGroup.findById(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {
      if (!Util.canAccess(Authentication.getLocalUser(), tg.owner))
        return badRequest("You don't have permission to modify this resource");

      try {
        tg.delete();
      } catch (Exception ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }
      return ok();
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result getGroupDetailView(Long id, String display) {
    TestGroup tg = TestGroup.findById(id);
    if (tg == null) {
      return badRequest("No job with id " + id + ".");
    }
    return ok(groupDetailView.render(tg, display));
  }


}
