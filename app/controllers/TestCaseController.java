package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import editormodels.TestCaseEditorModel;
import global.Util;
import models.Job;
import models.TestAssertion;
import models.TestCase;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.testCaseEditor;
import views.html.testCaseView;

import java.util.List;

import static play.data.Form.form;


/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestCaseController extends Controller {
  public static final Form<TestCaseEditorModel> TEST_CASE_FORM = form(TestCaseEditorModel.class);


  public static Result getCreateCaseEditorView(Long assertionId) {
    TestAssertion ta = TestAssertion.findById(assertionId);
    if (ta == null) {
      return badRequest("Test assertion with id [" + assertionId
          + "] not found!");
    } else {
      TestCaseEditorModel testCaseEditorModel = new TestCaseEditorModel();
      testCaseEditorModel.assertionId = assertionId;

      List<TestCase> list = TestCase.listByTestAssertionId(assertionId);
      int max = 0;
      if (list != null) {
        for (TestCase testCase : list) {
          System.out.println(testCase.name + "------" + ta.taId);
          if (testCase.name.matches("^" + ta.taId + "_TC\\d+$")) {
            int val = Integer.parseInt(testCase.name.split("_TC")[1]);
            if (max < val)
              max = val;
          }
        }
      }
      max += 1;
      String maxx = max < 10 ? ("0" + max) : "" + max;

      testCaseEditorModel.name = ta.taId + "_TC" + maxx;
      testCaseEditorModel.shortDescription = testCaseEditorModel.name;
      Form<TestCaseEditorModel> bind = TEST_CASE_FORM
          .fill(testCaseEditorModel);
      return ok(testCaseEditor.render(bind, null, false));
    }
  }

  public static Result doCreateCase() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, null, false));
    } else {
      TestCaseEditorModel model = filledForm.get();

      TestCase tc = TestCase.findByName(model.name);
      if (tc != null) {
        filledForm.reject("The test case with name [" + tc.name
            + "] already exists");
        return badRequest(testCaseEditor.render(filledForm, null, false));
      }

      TestAssertion ta = TestAssertion.findById(model.assertionId);
      if (ta == null) {
        filledForm.reject("No assertion found with id [" + ta.id + "]");
        return badRequest(testCaseEditor.render(filledForm, null, false));
      }

      final User localUser = Application.getLocalUser(session());

      tc = new TestCase();
      tc.name = model.name;
      tc.tdl = model.tdl;
      tc.shortDescription = model.shortDescription;
      tc.testAssertion = ta;
      tc.owner = localUser;
      try {
        tc.save();
      } catch (Exception ex) {
        filledForm.reject("Compilation Failed [" + ex.getMessage() + "]");
        Logger.error(ex.getMessage(), ex);
        return badRequest(testCaseEditor.render(filledForm, null, false));
      }

      tc = TestCase.findByName(tc.name);

      Logger.info("Test Case with name " + tc.id + ":" + tc.name
          + " was created");
      return redirect(routes.AssertionController.getAssertionDetailView(ta.id, "testCases"));
    }
  }

  public static Result getEditCaseEditorView(Long id) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      return badRequest("Test case with id [" + id + "] not found!");
    } else {
      if (!Util.canAccess(Application.getLocalUser(session()), tc.owner))
        return badRequest("You don't have permission to modify this resource");

      TestCaseEditorModel tcModel = new TestCaseEditorModel();
      tcModel.id = id;
      tcModel.assertionId = tc.testAssertion.id;
      tcModel.name = tc.name;
      tcModel.shortDescription = tc.shortDescription;
      tcModel.tdl = tc.tdl;
      tcModel.description = tc.description;

      Form<TestCaseEditorModel> bind = TEST_CASE_FORM.fill(tcModel);
      return ok(testCaseEditor.render(bind, null, true));
    }
  }

  public static Result doEditCase() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, null, true));
    } else {
      TestCaseEditorModel model = filledForm.get();

      TestCase tc = TestCase.findById(model.id);
      if (tc == null) {
        filledForm.reject("The test case with ID [" + model.id
            + "] does not exist");
        return badRequest(testCaseEditor.render(filledForm, null, true));
      }

      if (!Util.canAccess(Application.getLocalUser(session()), tc.owner))
        return badRequest("You don't have permission to modify this resource");

      tc.description = model.description;
      tc.name = model.name;
      tc.shortDescription = model.shortDescription;
      tc.setTdl(model.tdl);

      // check if the name is not duplicate
      TestCase tmp = TestCase.findByName(model.name);

      if (tmp == null || tmp.id == tc.id) {
        // either no such name or it is already this object. so update
        try {
          tc.update();
        }catch(Exception ex){
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(testCaseEditor.render(filledForm, null, true));
        }
        Logger.info("Test Case with id " + tc.id + ":" + tc.name
            + " was updated");
        return redirect(routes.TestCaseController.viewTestCase(tc.id, false));
      } else {
        filledForm.reject("The ID [" + model.name
            + "] is used by another test case");
        return badRequest(testCaseEditor.render(filledForm, null, true));
      }

    }
  }

  public static Result doDeleteCase(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      // it does not exist. error
      return badRequest("Test case with id " + id + " does not exist.");
    }


    if (!Util.canAccess(Application.getLocalUser(session()), tc.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      tc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return redirect(routes.AssertionController.getAssertionDetailView(tc.testAssertion.id, "testCases"));
  }

  public static Result viewTestCase(long id, boolean showJobs) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      return badRequest("No test case with id " + id + ".");
    }
    return ok(testCaseView.render(tc, Application.getLocalUser(session()), showJobs

    ));
  }

  public static Result doEditCaseField() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    JsonNode jsonNode = request().body().asJson();

    Result res = GroupController.doEditField(TestCaseEditorModel.class, TestCase.class, jsonNode);

    if (res.toScala().header().status() == BAD_REQUEST) {
      return res;
    } else {
      long id = jsonNode.findPath("id").asInt();
      TestCase tc = TestCase.findById(id);
      //just trigger recompile and stuff
      try {
        tc.save();
        return res;
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        return badRequest("Compilation Failed [" + ex.getMessage() + "]");
      }
    }
  }
}
