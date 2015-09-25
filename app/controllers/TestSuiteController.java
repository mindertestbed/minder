package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import editormodels.TestSuiteEditorModel;
import global.Util;
import models.TestGroup;
import models.TestSuite;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.testDesigner.testSuite.*;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestSuiteController extends Controller {
  public static final Form<TestSuiteEditorModel> TEST_SUITE_FORM = form(TestSuiteEditorModel.class);

  /*
   * Test Asertion CRUD
   */
  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result getCreateTestSuiteView(Long groupId) {
    TestGroup tg = TestGroup.findById(groupId);
    if (tg == null) {
      return badRequest("Test group with id [" + groupId + "] not found!");
    } else {
      TestSuiteEditorModel testSuiteEditorModel = new TestSuiteEditorModel();
      testSuiteEditorModel.groupId = groupId;
      Form<TestSuiteEditorModel> bind = TEST_SUITE_FORM
          .fill(testSuiteEditorModel);
      return ok(testSuiteEditor.render(bind));
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doCreateTestSuite() {
    final Form<TestSuiteEditorModel> filledForm = TEST_SUITE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testSuiteEditor.render(filledForm));
    } else {
      TestSuiteEditorModel model = filledForm.get();

      TestGroup tg = TestGroup.findById(model.groupId);

      if (tg == null) {
        filledForm.reject("No group found with id [" + tg.id + "]");
        return badRequest(testSuiteEditor.render(filledForm));
      }

      TestSuite ts = new TestSuite();
      ts.name = model.name;
      ts.description = model.description;
      ts.shortDescription = model.shortDescription;
      ts.testGroup = tg;
      ts.mtdlParameters = model.mtdlParameters;
      ts.owner = Authentication.getLocalUser();
      ts.save();
      Logger.debug("TestSuite with id " + ts.id + " was created");
      return redirect(routes.GroupController.getGroupDetailView(tg.id, "suites"));
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result editTestSuiteForm(Long id) {
    TestSuite ts = TestSuite.findById(id);
    if (ts == null) {
      return badRequest("Test TestSuite with id [" + id + "] not found!");

    }

    if (!Util.canAccess(Authentication.getLocalUser(), ts.owner))
      return badRequest("You don't have permission to modify this resource");

    TestSuiteEditorModel tsModel = new TestSuiteEditorModel();
    tsModel.id = id;
    tsModel.name = ts.name;
    tsModel.description = ts.description;
    tsModel.shortDescription = ts.shortDescription;
    tsModel.groupId = ts.testGroup.id;
    tsModel.mtdlParameters = ts.mtdlParameters;

    Form<TestSuiteEditorModel> bind = TEST_SUITE_FORM
        .fill(tsModel);
    return ok(testSuiteEditor.render(bind));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditTestSuite() {
    final Form<TestSuiteEditorModel> filledForm = TEST_SUITE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testSuiteEditor.render(filledForm));
    } else {
      TestSuiteEditorModel model = filledForm.get();

      TestSuite testSuite = TestSuite.findById(model.id);
      if (testSuite == null) {
        filledForm.reject("The test TestSuite with ID [" + model.id
            + "] does not exist");
        return badRequest(testSuiteEditor.render(filledForm));
      }

      if (!Util.canAccess(Authentication.getLocalUser(), testSuite.owner))
        return badRequest("You don't have permission to modify this resource");

      testSuite.name = model.name;
      testSuite.description = model.description;
      testSuite.shortDescription = model.shortDescription;
      testSuite.mtdlParameters = model.mtdlParameters;
      return redirect(routes.TestSuiteController.getTestSuiteDetailView(testSuite.id, "jobs"));
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doDeleteTestSuite(Long id) {
    TestSuite ta = TestSuite.findById(id);
    if (ta == null) {
      // it does not exist. error
      return badRequest("Test TestSuite with id " + id
          + " does not exist.");
    }

    if (!Util.canAccess(Authentication.getLocalUser(), ta.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      ta.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return redirect(routes.GroupController.getGroupDetailView(ta.testGroup.id, "suites"));
  }

  public static Result getTestSuiteDetailView(Long id, String display) {
    TestSuite testSuite = TestSuite.findById(id);
    if (testSuite == null) {
      return badRequest("No test TestSuite with id " + id + ".");
    }
    return ok(testSuiteDetailView.render(testSuite, display));
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditTestSuiteField() {
    JsonNode jsonNode = request().body().asJson();

    return GroupController.doEditField(TestSuiteEditorModel.class, TestSuite.class, jsonNode);
  }


}
