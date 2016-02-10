package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.enumeration.Utils;
import dependencyutils.DependencyClassLoaderCache;
import editormodels.GroupEditorModel;
import global.Util;
import models.TestGroup;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import rest.controllers.TestGroupImportExportController;
import security.AllowedRoles;
import security.Role;
import views.html.group.childViews.*;
import views.html.group.*;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class GroupController extends Controller {
  public static final Form<GroupEditorModel> TEST_GROUP_FORM = form(GroupEditorModel.class);

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result getCreateGroupEditorView() {
    return ok(testGroupEditor.render(TEST_GROUP_FORM));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
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
      return redirect(routes.Application.testGroups());
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
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

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditGroupField() {
    JsonNode jsonNode = request().body().asJson();

    Result result = ok();
    try {
      Ebean.beginTransaction();
      result = Utils.doEditField(GroupEditorModel.class, TestGroup.class, jsonNode, Authentication.getLocalUser());

      String field = jsonNode.findPath("field").asText();

      String fieldName = null;
      try {
        fieldName = TestGroup.class.getField("dependencyString").getName();
      } catch (NoSuchFieldException e) {
        return internalServerError(e.getCause().toString());
      }

      if (field.equals(fieldName)) {
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


  @AllowedRoles(Role.TEST_DESIGNER)
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
    return ok(mainView.render(tg, display));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER})
  public static Result exportTestGroup() {
    return TestGroupImportExportController.exportTestGroupData();
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER})
  public static Result importTestGroup() {
    return TestGroupImportExportController.exportTestGroupData();
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result renderTestAssertionList(long id) {
    return ok(testAssertionList.render(TestGroup.findById(id)));
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result renderTestSuites(long id) {
    return ok(testSuiteList.render(TestGroup.findById(id)));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result renderTestAssets(long id) {
    return ok(testAssetList.render(TestGroup.findById(id)));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result renderUtilClasses(long id) {
    return ok(utilClassList.render(TestGroup.findById(id)));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result renderDependencies(long id) {
    return ok(dependencies.render(TestGroup.findById(id)));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result renderDetails(long id) {
    return ok(details.render(TestGroup.findById(id)));
  }

}
