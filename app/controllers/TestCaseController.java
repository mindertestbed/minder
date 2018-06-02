package controllers;


import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.Utils;
import editormodels.TestCaseEditorModel;
import utils.Util;
import minderengine.AdapterIdentifier;
import minderengine.TestEngine;
import models.*;
import mtdl.MinderTdl;
import mtdl.AdapterFunction;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.testCase.childViews.testCaseEditor;
import views.html.testCase.mainView;

import javax.inject.Inject;
import java.util.*;


/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestCaseController extends Controller {
  Authentication authentication;
  TestEngine testEngine;

  public final Form<TestCaseEditorModel> TEST_CASE_FORM;

  @Inject
  public TestCaseController(Authentication authentication, TestEngine testEngine, FormFactory formFactory) {
    this.authentication = authentication;
    this.testEngine = testEngine;
    TEST_CASE_FORM = formFactory.form(TestCaseEditorModel.class);
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result getCreateCaseEditorView(Long assertionId) {
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
      Form<TestCaseEditorModel> bind = TEST_CASE_FORM
          .fill(testCaseEditorModel);
      return ok(testCaseEditor.render(bind, false, authentication));

    }
  }


  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doCreateCase() {
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, false, authentication));

    } else {
      TestCaseEditorModel model = filledForm.get();

      TestCase tc = TestCase.findByName(model.name);
      if (tc != null) {
        filledForm.reject("The test case with name [" + tc.name
            + "] already exists");
        return badRequest(testCaseEditor.render(filledForm, false, authentication));

      }

      TestAssertion ta = TestAssertion.findById(model.assertionId);
      if (ta == null) {
        filledForm.reject("No assertion found with id [" + ta.id + "]");
        return badRequest(testCaseEditor.render(filledForm, false, authentication));

      }

      final User localUser = authentication.getLocalUser();

      tc = new TestCase();
      tc.name = model.name;
      Tdl tdl = new Tdl();
      tdl.version = model.version;
      tdl.testCase = tc;
      tdl.tdl = model.tdl;
      tdl.creationDate = new Date();
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
        return badRequest(testCaseEditor.render(filledForm, false, authentication));

      } finally {
        Ebean.endTransaction();
      }

      tc = TestCase.findByName(tc.name);

      Logger.info("Test Case with name " + tc.id + ":" + tc.name
          + " was created");
      return redirect(routes.TestAssertionController.getAssertionDetailView(ta.id, "cases"));
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result getEditCaseEditorView(Long tdlId) {
    Tdl tdl = Tdl.findById(tdlId);
    if (tdl == null) {
      return badRequest("TDL with id [" + tdlId + "] not found!");
    } else {
      tdl.testCase = TestCase.findById(tdl.testCase.id);
      if (!Util.canAccess(authentication.getLocalUser(), tdl.testCase.owner))
        return badRequest("You don't have permission to modify this resource");

      TestCaseEditorModel tcModel = new TestCaseEditorModel();
      tcModel.id = tdlId;
      tcModel.name = tdl.testCase.name;
      tcModel.tdl = tdl.tdl;
      tcModel.version = tdl.version;

      Form<TestCaseEditorModel> bind = TEST_CASE_FORM.fill(tcModel);
      return ok(testCaseEditor.render(bind, true, authentication));

    }
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doEditCase() {

    System.out.println("Hello world");
    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    System.out.println(request().body().asFormUrlEncoded());
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testCaseEditor.render(filledForm, true, authentication));

    } else {
      TestCaseEditorModel model = filledForm.get();

      Tdl tdl = Tdl.findById(model.id);
      TestCase tc = tdl.testCase = TestCase.findById(tdl.testCase.id);
      if (tc == null) {
        filledForm.reject("The test case with ID [" + model.id
            + "] does not exist");
        return badRequest(testCaseEditor.render(filledForm, true, authentication));

      }
      if (!Util.canAccess(authentication.getLocalUser(), tc.owner))
        return badRequest("You don't have permission to modify this resource");

      if (model.version.equals(tdl.version)) {
        //do nothing, just save
        tdl.tdl = model.tdl;
        try {
          testEngine.describeTdl(tdl);
        } catch (Exception ex) {
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(testCaseEditor.render(filledForm, true, authentication));

        }
        tdl.update();
        return redirect(routes.TestCaseController.viewTestCase(tc.id, "code"));
      } else {

        try {
          Ebean.beginTransaction();
          Tdl newTdl = new Tdl();
          newTdl.creationDate = new Date();
          newTdl.version = model.version;
          newTdl.testCase = tc;
          newTdl.tdl = model.tdl;
          newTdl.save();
          detectAndSaveParameters(newTdl);
          Ebean.commitTransaction();
        } catch (Exception ex) {
          filledForm.reject(ex.getMessage());
          return badRequest(testCaseEditor.render(filledForm, true, authentication));

        } finally {
          Ebean.endTransaction();
        }

        return redirect(routes.TestCaseController.viewTestCase(tc.id, "code"));
      }
    }
  }

  /**
   * TODO: move this method to another class
   *
   * @param newTdl
   */
  public static void detectAndSaveParameters(Tdl newTdl) {
    Logger.debug("Detect parameters for the newTdl");
    LinkedHashMap<String, Set<AdapterFunction>> descriptionMap = TestEngine.describeTdl(newTdl);

    List<AdapterParam> adapterParamList = new ArrayList<>();
    for (Map.Entry<String, Set<AdapterFunction>> entry : descriptionMap.entrySet()) {
      //make sure that we are looping on variables.
      final String key = entry.getKey();
      if (!key.startsWith("$")) {
        //make sure that the entry really exists.

        AdapterIdentifier adapterIdentifier = AdapterIdentifier.parse(key);

        if (adapterIdentifier.getName().equals(MinderTdl.NULL_ADAPTER_NAME())) {
          //skip null adapter
          continue;
        }
        Adapter adapter = Adapter.findByName(adapterIdentifier.getName());
        if (adapter == null) {
          //oops
          throw new IllegalArgumentException("No adapter with name " + adapterIdentifier.getName());
        }
        //check if a version is used in the name
        if (adapterIdentifier.getVersion() != null) {
          //we have a version, check if the version exists
          AdapterVersion adapterVersion = AdapterVersion.findAdapterAndVersion(adapter, adapterIdentifier.getVersion());
          if (adapterVersion == null) {
            throw new IllegalArgumentException("No adapter version " + adapterIdentifier.getVersion() + " for " + adapterIdentifier.getName());
          }
        }
        continue;
      }

      AdapterParam adapterParam;
      adapterParam = new AdapterParam();
      adapterParam.name = key;
      adapterParam.signatures = new ArrayList<>();
      adapterParam.tdl = newTdl;
      adapterParamList.add(adapterParam);

      Logger.debug("\t" + key + " detected");

      for (AdapterFunction signalSlot : entry.getValue()) {
        ParamSignature ps = new ParamSignature();
        ps.signature = signalSlot.signature().replaceAll("\\s", "");
        ps.adapterParam = adapterParam;
        adapterParam.signatures.add(ps);
      }

      newTdl.parameters.add(adapterParam);
    }

    Logger.debug("Save parameters");
    for (AdapterParam adapterParam : adapterParamList) {
      adapterParam.save();
      for (ParamSignature signature : adapterParam.signatures) {
        signature.save();
      }
    }
    Logger.debug("Detect parameters done");
  }


  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doDeleteCase(Long id) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      // it does not exist. error
      return badRequest("Test case with id " + id + " does not exist.");
    }


    if (!Util.canAccess(authentication.getLocalUser(), tc.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      tc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return redirect(routes.TestAssertionController.getAssertionDetailView(tc.testAssertion.id, "testCases"));
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER, Role.TEST_DEVELOPER})
  public Result viewTestCase(long id, String display) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      return badRequest("No test case with id " + id + ".");
    }
    return ok(mainView.render(tc, Tdl.getLatestTdl(tc), authentication.getLocalUser(), display, authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER, Role.TEST_DEVELOPER})
  public Result viewTestCase2(long id, long tdlId, String display) {
    TestCase tc = TestCase.findById(id);
    Tdl tdl = Tdl.findById(tdlId);
    if (tc == null) {
      return badRequest("No test case with id " + id + ".");
    }
    if (tdl == null) {
      return badRequest("No tdl with id " + id + ".");
    }
    return ok(mainView.render(tc, tdl, authentication.getLocalUser(), display, authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doEditCaseField() {
    JsonNode jsonNode = request().body().asJson();

    Result res = Utils.doEditField(TestCaseEditorModel.class, TestCase.class, jsonNode, authentication.getLocalUser());

    if (res.asScala().header().status() == BAD_REQUEST) {
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
