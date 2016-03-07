package controllers;

import editormodels.TestSuiteEditorModel;
import global.ReportUtils;
import models.SuiteRun;
import models.TestRun;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;

import java.util.ArrayList;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class SuiteRunController extends Controller {
  public static final Form<TestSuiteEditorModel> TEST_SUITE_FORM = form(TestSuiteEditorModel.class);

  static ThreadLocal<String> threadLocal = new ThreadLocal<>();
  /*
   */
  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result getSuiteRunDetailView(Long suiteRunId) {
    SuiteRun suiteRun = SuiteRun.findById(suiteRunId);
    if (suiteRun == null) {
      return badRequest("Suite Run with id [" + suiteRunId + "] not found!");
    } else {
      return ok(views.html.testSuite.childViews.suiteRunDetails.render(suiteRun));
    }
  }


  /*
   */
  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result generateReport(Long suiteRunId, String testRuns, String subTitle) {
    SuiteRun suiteRun = SuiteRun.findById(suiteRunId);
    if (suiteRun == null) {
      return badRequest("Suite Run with id [" + suiteRunId + "] not found!");
    } else {


      try {
        byte[] content;
        if (testRuns == null || testRuns.isEmpty()) {
          content = ReportUtils.toPdf(TestRun.findBySuiteRun(suiteRun), subTitle);
        } else {
          ArrayList<TestRun> selectedTestRuns = new ArrayList<>();
          for (String id : testRuns.split(",")) {
            selectedTestRuns.add(TestRun.findById(Long.parseLong(id)));
          }
          content = ReportUtils.toPdf(selectedTestRuns, subTitle);
        }

        response().setContentType("application/x-download");
        response().setHeader("Content-disposition", "attachment; filename=" + suiteRun.testSuite.name + "-" + suiteRun.number + ".pdf");
        return ok(content);
      } catch (Exception ex) {
        Logger.debug(ex.getMessage(), ex);
        return badRequest(ex.getMessage());
      }
    }
  }
}
