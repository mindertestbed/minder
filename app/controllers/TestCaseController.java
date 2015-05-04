package controllers;


import editormodels.TestCaseEditorModel;
import global.Util;
import models.TestAssertion;
import models.TestCase;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.testCaseEditor;
import views.html.testCaseLister;
import views.html.testCaseView;

import static play.data.Form.form;


/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestCaseController  extends Controller {
  public static final Form<TestCaseEditorModel> TEST_CASE_FORM = form(TestCaseEditorModel.class);


  public static Result getCreateCaseEditorView(Long assertionId) {
    TestAssertion ta = TestAssertion.find.byId(assertionId);
    if (ta == null) {
      return badRequest("Test assertion with id [" + assertionId
          + "] not found!");
    } else {
      TestCaseEditorModel testCaseEditorModel = new TestCaseEditorModel();
      testCaseEditorModel.assertionId = assertionId;

      Form<TestCaseEditorModel> bind = TEST_CASE_FORM
          .fill(testCaseEditorModel);
      return ok(testCaseEditor.render(bind, null));
    }
  }

  public static Result doCreateCase() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, null));
    } else {
      TestCaseEditorModel model = filledForm.get();

      TestCase tc = TestCase.findByName(model.name);
      if (tc != null) {
        filledForm.reject("The test case with name [" + tc.name
            + "] already exists");
        return badRequest(testCaseEditor.render(filledForm, null));
      }

      TestAssertion ta = TestAssertion.findById(model.assertionId);
      if (ta == null) {
        filledForm.reject("No assertion found with id [" + ta.id + "]");
        return badRequest(testCaseEditor.render(filledForm, null));
      }

      final User localUser = Application.getLocalUser(session());

      tc = new TestCase();
      tc.name = model.name;
      tc.tdl = model.tdl;
      tc.shortDescription = model.shortDescription;
      tc.testAssertion = ta;
      tc.owner = Application.getLocalUser(session());
      try {
        tc.save();
      }catch (Exception ex){
        filledForm.reject("Compilation Failed [" + ex.getMessage() + "]");
        Logger.error(ex.getMessage(), ex);
        return badRequest(testCaseEditor.render(filledForm, null));
      }

      tc = TestCase.findByName(tc.name);

      Logger.info("Test Case with name " + tc.id + ":" + tc.name
          + " was created");
      return redirect(routes.AssertionController.getAssertionDetailView(ta.id, "testCases"));
    }
  }

  public static Result editCaseForm(Long id) {
    TestCase tc = TestCase.find.byId(id);
    if (tc == null) {
      return badRequest("Test case with id [" + id + "] not found!");
    } else {
      TestCaseEditorModel tcModel = new TestCaseEditorModel();
      tcModel.id = id;
      tcModel.assertionId = tc.testAssertion.id;
      tcModel.name = tc.name;
      tcModel.shortDescription = tc.shortDescription;
      tcModel.tdl = tc.tdl;
      tcModel.description = tc.description;

      Form<TestCaseEditorModel> bind = TEST_CASE_FORM.fill(tcModel);
      return ok(testCaseEditor.render(bind, null));
    }
  }

  public static Result doEditCase() {
    System.out.println("doEditCase\n");
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, null));
    } else {
      TestCaseEditorModel model = filledForm.get();

      TestCase tc = TestCase.findById(model.id);
      if (tc == null) {
        filledForm.reject("The test case with ID [" + model.id
            + "] does not exist");
        return badRequest(testCaseEditor.render(filledForm, null));
      }

      tc.description = model.description;
      tc.name = model.name;
      tc.shortDescription = model.shortDescription;
      tc.setTdl(model.tdl);

      // check if the name is not duplicate
      TestCase tmp = TestCase.findByName(model.name);

      if (tmp == null || tmp.id == tc.id) {
        // either no such name or it is already this object. so update
        tc.update();
        Logger.info("Test Case with id " + tc.id + ":" + tc.name
            + " was updated");
        return ok(testCaseLister.render(tc.testAssertion, null));
      } else {
        filledForm.reject("The ID [" + model.name
            + "] is used by another test case");
        return badRequest(testCaseEditor.render(filledForm, null));
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

    try {
      tc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return ok(testCaseLister.render(tc.testAssertion, null));
  }

  public static Result viewTestCase(long id, boolean showJobs) {
    TestCase tc = TestCase.findById(id);
    if (tc == null){
      return badRequest("No test case with id " + id + ".");
    }
    return ok(testCaseView.render(tc, Application.getLocalUser(session()), showJobs

    ));
  }

}
