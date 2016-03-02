package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.enumeration.Utils;
import editormodels.TestSuiteEditorModel;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.testSuite.childViews.details;
import views.html.testSuite.mainView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import views.html.testSuite.childViews.*;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class SuiteRunController extends Controller {
  public static final Form<TestSuiteEditorModel> TEST_SUITE_FORM = form(TestSuiteEditorModel.class);

  /*
   * Test Asertion CRUD
   */
  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result getSuiteRunDetailView(Long suiteRunId) {
    SuiteRun suiteRun = SuiteRun.findById(suiteRunId);
    if (suiteRun == null) {
      return badRequest("Suite Run with id [" + suiteRunId + "] not found!");
    } else {
      return ok(suiteRunDetails.render(suiteRun));
    }
  }
}
