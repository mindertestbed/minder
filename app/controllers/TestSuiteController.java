package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import editormodels.TestSuiteEditorModel;
import global.Util;
import models.*;
import play.Logger;
import play.api.libs.json.Json;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.testDesigner.testSuite.*;
import views.html.testDesigner.testSuite.tdlList.existingTdls;
import views.html.testDesigner.testSuite.tdlList.suitableTdls;

import java.util.ArrayList;
import java.util.List;

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
      return ok(editor.render(bind));
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doCreateTestSuite() {
    final Form<TestSuiteEditorModel> filledForm = TEST_SUITE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(editor.render(filledForm));
    } else {
      TestSuiteEditorModel model = filledForm.get();

      TestGroup tg = TestGroup.findById(model.groupId);

      if (tg == null) {
        filledForm.reject("No group found with id [" + tg.id + "]");
        return badRequest(editor.render(filledForm));
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
    return ok(editor.render(bind));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditTestSuite() {
    final Form<TestSuiteEditorModel> filledForm = TEST_SUITE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(editor.render(filledForm));
    } else {
      TestSuiteEditorModel model = filledForm.get();

      TestSuite testSuite = TestSuite.findById(model.id);
      if (testSuite == null) {
        filledForm.reject("The test TestSuite with ID [" + model.id
            + "] does not exist");
        return badRequest(editor.render(filledForm));
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
    return ok(mainView.render(testSuite, display));
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditTestSuiteField() {
    JsonNode jsonNode = request().body().asJson();

    return GroupController.doEditField(TestSuiteEditorModel.class, TestSuite.class, jsonNode);
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result listAvailableTdlsForSuite(long testSuiteId) {

    TestSuite testSuite = TestSuite.findById(testSuiteId);

    if (testSuite == null) {
      return badRequest("Test Suite Not found");
    }

    TestGroup testGroup = TestGroup.findById(testSuite.testGroup.id);

    List<Tdl> list = new ArrayList<>();
    testGroup.testAssertions = TestAssertion.findByGroup(testGroup);
    for (TestAssertion testAssertion : testGroup.testAssertions) {
      testAssertion.testGroup = testGroup;
      testAssertion.testCases = TestCase.listByTestAssertion(testAssertion);
      for (TestCase testCase : testAssertion.testCases) {
        testCase.testAssertion = testAssertion;
        testCase.tdls = Tdl.listByTestCase(testCase);
        for (Tdl tdl : testCase.tdls) {
          tdl.testCase = testCase;
          list.add(tdl);
        }
      }

    }
    return ok(suitableTdls.render(list));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result renderJoblistView(long id) {
    return ok(jobList.render(TestSuite.findById(id)));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result renderTestRunListView(long id) {
    return ok(testRunList.render(TestSuite.findById(id)));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result renderDetailView(long id) {
    return ok(details.render(TestSuite.findById(id)));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result renderTdlList(long id) {
    return ok(existingTdls.render(TestSuite.findById(id)));
  }
}
