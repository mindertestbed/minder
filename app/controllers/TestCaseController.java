package controllers;


import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.Utils;
import editormodels.TestCaseEditorModel;
import java.net.URL;
import java.net.URLClassLoader;
import org.slf4j.LoggerFactory;
import play.api.Play;
import utils.TdlUtils;
import utils.Util;
import minderengine.TestEngine;
import models.*;
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

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestCaseController.class);

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
          LOGGER.debug(testCase.name + "------" + ta.taId);
          if (testCase.name.matches("^" + ta.taId + "_TC\\d+$")) {
            int val = Integer.parseInt(testCase.name.split("_TC")[1]);
            if (max < val) {
              max = val;
            }
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
        TdlUtils.detectAndSaveParameters(tdl);
        Ebean.commitTransaction();
      } catch (Exception ex) {
        filledForm.reject("Compilation Failed [" + ex.getMessage() + "]");
        LOGGER.error(ex.getMessage(), ex);
        return badRequest(testCaseEditor.render(filledForm, false, authentication));

      } finally {
        Ebean.endTransaction();
      }

      tc = TestCase.findByName(tc.name);

      LOGGER.info("Test Case with name " + tc.id + ":" + tc.name
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
      if (!Util.canAccess(authentication.getLocalUser(), tdl.testCase.owner)) {
        return badRequest("You don't have permission to modify this resource");
      }

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

    final String property = System.getProperty("java.class.path");

    LOGGER.debug("Classpath: " + property);

    final URLClassLoader ldr = (URLClassLoader) Play.current().classloader();

    for(URL url : ldr.getURLs()){
      LOGGER.debug("Classpath: " + url);
    }

    final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
        .bindFromRequest();

    LOGGER.debug(request().body().asFormUrlEncoded().toString());
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
      if (!Util.canAccess(authentication.getLocalUser(), tc.owner)) {
        return badRequest("You don't have permission to modify this resource");
      }

      if (model.version.equals(tdl.version)) {
        //do nothing, just save
        tdl.tdl = model.tdl;
        try {
          testEngine.describeTdl(tdl);
        } catch (Exception ex) {
          LOGGER.error(ex.getMessage(), ex);
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
          TdlUtils.detectAndSaveParameters(newTdl);
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


  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doDeleteCase(Long id) {
    TestCase tc = TestCase.findById(id);
    if (tc == null) {
      // it does not exist. error
      return badRequest("Test case with id " + id + " does not exist.");
    }

    if (!Util.canAccess(authentication.getLocalUser(), tc.owner)) {
      return badRequest("You don't have permission to modify this resource");
    }

    try {
      tc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.error(ex.getMessage(), ex);
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
        LOGGER.error(ex.getMessage(), ex);
        return badRequest("Failed to save the test case [" + ex.getMessage() + "]");
      }
    }
  }


}
