package controllers;


import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import editormodels.TestCaseEditorModel;
import global.Global;
import global.Util;
import minderengine.TestEngine;
import models.*;
import mtdl.SignalSlot;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.testCaseEditor;
import views.html.testCaseView;

import java.util.*;

import static play.data.Form.form;


/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestCaseController extends Controller {
  public static final Form<TestCaseEditorModel> TEST_CASE_FORM = form(TestCaseEditorModel.class);

  @Security.Authenticated(Secured.class)
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

  @Security.Authenticated(Secured.class)
  public static Result doCreateCase() {
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

      final User localUser = Authentication.getLocalUser();

      tc = new TestCase();
      tc.name = model.name;
      Tdl tdl = new Tdl();
      tdl.version = model.version;
      tdl.testCase = tc;
      tdl.tdl = model.tdl;
      tdl.creationDate = new Date();
      tc.shortDescription = model.shortDescription;
      tc.testAssertion = ta;
      tc.owner = localUser;
      try {
        Ebean.beginTransaction();
        tc.save();
        tdl.save();
        detectAndSaveParameters(tdl);
        Ebean.commitTransaction();
      } catch (Exception ex) {
        filledForm.reject("Compilation Failed [" + ex.getMessage() + "]");
        Logger.error(ex.getMessage(), ex);
        return badRequest(testCaseEditor.render(filledForm, null, false));
      } finally {
        Ebean.endTransaction();
      }

      tc = TestCase.findByName(tc.name);

      Logger.info("Test Case with name " + tc.id + ":" + tc.name
          + " was created");
      return redirect(routes.AssertionController.getAssertionDetailView(ta.id, "testCases"));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result getEditCaseEditorView(Long tdlId) {
    Tdl tdl = Tdl.findById(tdlId);
    if (tdl == null) {
      return badRequest("TDL with id [" + tdlId + "] not found!");
    } else {
      tdl.testCase = TestCase.findById(tdl.testCase.id);
      if (!Util.canAccess(Authentication.getLocalUser(), tdl.testCase.owner))
        return badRequest("You don't have permission to modify this resource");

      TestCaseEditorModel tcModel = new TestCaseEditorModel();
      tcModel.id = tdlId;
      tcModel.name = tdl.testCase.name;
      tcModel.shortDescription = tdl.testCase.name;
      tcModel.tdl = tdl.tdl;
      tcModel.version = tdl.version;

      Form<TestCaseEditorModel> bind = TEST_CASE_FORM.fill(tcModel);
      return ok(testCaseEditor.render(bind, null, true));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditCase() {
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, null, true));
    } else {
      TestCaseEditorModel model = filledForm.get();

      Tdl tdl = Tdl.findById(model.id);
      TestCase tc = tdl.testCase = TestCase.findById(tdl.testCase.id);
      if (tc == null) {
        filledForm.reject("The test case with ID [" + model.id
            + "] does not exist");
        return badRequest(testCaseEditor.render(filledForm, null, true));
      }
      if (!Util.canAccess(Authentication.getLocalUser(), tc.owner))
        return badRequest("You don't have permission to modify this resource");

      if (model.version.equals(tdl.version)) {
        //do nothing, just save
        tdl.tdl = model.tdl;
        try {
          TestEngine.describeTdl(tdl);
        } catch (Exception ex) {
          filledForm.reject(ex.getMessage());
          return badRequest(testCaseEditor.render(filledForm, null, true));
        }
        tdl.update();
        return redirect(routes.TestCaseController.viewTestCase(tc.id, "mtdl"));
      } else {

        Tdl newTdl = new Tdl();
        newTdl.creationDate = new Date();
        newTdl.version = model.version;
        newTdl.testCase = tc;
        newTdl.tdl = model.tdl;
        newTdl.save();

        try {
          detectAndSaveParameters(newTdl);
        } catch (Exception ex) {
          filledForm.reject(ex.getMessage());
          return badRequest(testCaseEditor.render(filledForm, null, true));
        }


        return redirect(routes.TestCaseController.viewTestCase(tc.id, "mtdl"));
      }
    }
  }

  @Security.Authenticated(Secured.class)
  public static void detectAndSaveParameters(Tdl newTdl) {
    Logger.debug("Detect parameters for newTdl");
    LinkedHashMap<String, Set<SignalSlot>> descriptionMap = TestEngine.describeTdl(newTdl);

    List<WrapperParam> wrapperParamList = new ArrayList<>();
    for (Map.Entry<String, Set<SignalSlot>> entry : descriptionMap.entrySet()) {
      //make sure that we are looping on variables.
      if (!entry.getKey().startsWith("$"))
        continue;

      WrapperParam wrapperParam;
      wrapperParam = new WrapperParam();
      wrapperParam.name = entry.getKey();
      wrapperParam.signatures = new ArrayList<>();
      wrapperParam.tdl = newTdl;
      wrapperParamList.add(wrapperParam);

      Logger.debug("\t" + entry.getKey() + " detected");

      for (SignalSlot signalSlot : entry.getValue()) {
        ParamSignature ps = new ParamSignature();
        ps.signature = signalSlot.signature().replaceAll("\\s", "");
        ps.wrapperParam = wrapperParam;
        wrapperParam.signatures.add(ps);
      }

      newTdl.parameters.add(wrapperParam);
    }

    Logger.debug("Save parameters");
    for (WrapperParam wrapperParam : wrapperParamList) {
      wrapperParam.save();
      for (ParamSignature signature : wrapperParam.signatures) {
        signature.save();
      }
    }
    Logger.debug("Detect parameters done");
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteCase(Long id) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      // it does not exist. error
      return badRequest("Test case with id " + id + " does not exist.");
    }


    if (!Util.canAccess(Authentication.getLocalUser(), tc.owner))
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

  @Security.Authenticated(Secured.class)
  public static Result viewTestCase(long id, String display) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      return badRequest("No test case with id " + id + ".");
    }
    return ok(testCaseView.render(tc, Tdl.getLatestTdl(tc), Authentication.getLocalUser(), display));
  }

  @Security.Authenticated(Secured.class)
  public static Result viewTestCase2(long id, long tdlId, String display) {
    TestCase tc = TestCase.findById(id);
    Tdl tdl = Tdl.findById(tdlId);
    if (tc == null) {
      return badRequest("No test case with id " + id + ".");
    }
    if (tdl == null) {
      return badRequest("No tdl with id " + id + ".");
    }
    return ok(testCaseView.render(tc, tdl, Authentication.getLocalUser(), display));
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditCaseField() {
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
        return badRequest("Failed to save the test case [" + ex.getMessage() + "]");
      }
    }
  }
}
