package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class JSRoutes extends Controller {
  public Result jsRoutes() {
    return ok(
        play.routing.JavaScriptReverseRouter.create("jsRoutes",
            routes.javascript.Application.restartQueThread(),
            routes.javascript.Authentication.login(),
            routes.javascript.Authentication.doLogin(),
            routes.javascript.GroupController.renderDetails(),
            routes.javascript.GroupController.renderTestAssertionList(),
            routes.javascript.GroupController.renderTestSuites(),
            routes.javascript.GroupController.renderTestAssets(),
            routes.javascript.GroupController.renderUtilClasses(),
            routes.javascript.GroupController.renderDependencies(),
            routes.javascript.GroupController.renderJobTemplates(),
            routes.javascript.GroupController.renderReportTemplates(),
            routes.javascript.ReportTemplateController.doCreateReportTemplate(),
            routes.javascript.ReportTemplateController.doEditReportTemplate(),
            routes.javascript.ReportTemplateController.viewReportTemplateView(),
            routes.javascript.ReportTemplateController.createReportTemplateView(),
            routes.javascript.ReportTemplateController.editReportTemplateView(),
            routes.javascript.TestAssertionController.renderDetails(),
            routes.javascript.TestAssertionController.renderTestCases(),
            routes.javascript.JobController.changeTestRunVisibility(),
            routes.javascript.JobController.changeJobVisibility(),
            routes.javascript.TestQueueController.enqueueJob(),
            routes.javascript.TestQueueController.enqueueTestSuite(),
            routes.javascript.UserController.listUsers(),
            routes.javascript.UserController.viewSettings(),
            routes.javascript.SuiteRunController.getSuiteRunDetailView(),
            routes.javascript.SuiteRunController.generateReport(),
            routes.javascript.TestSuiteController.getNamesAndAdaptersForTdls(),
            routes.javascript.TestSuiteController.renderStatus(),
            routes.javascript.TestSuiteController.renderDetails(),
            routes.javascript.TestSuiteController.renderEditor(),
            routes.javascript.AdvancedReporting.renderMain(),
            routes.javascript.AdvancedBatchReporting.step1(),
            routes.javascript.AdvancedBatchReporting.step2(),
            routes.javascript.AdvancedBatchReporting.step3(),
            routes.javascript.AdvancedBatchReporting.step4(),
            routes.javascript.AdvancedBatchReporting.listTestSuites(),
            routes.javascript.AdvancedBatchReporting.listSuiteRuns(),
            routes.javascript.AdvancedBatchReporting.listTestRuns(),
            routes.javascript.AdvancedBatchReporting.listTestCases(),
            routes.javascript.AdvancedBatchReporting.listJobs(),
            routes.javascript.AdvancedBatchReporting.listJobTestRuns(),
            routes.javascript.AdvancedBatchReporting.getReportParameters1(),
            routes.javascript.AdvancedBatchReporting.getReportParameters2(),
            routes.javascript.AdvancedBatchReporting.generateReport(),
            routes.javascript.Scheduling.listScheduledJobs(),
            routes.javascript.Scheduling.addScheduledJob(),
            routes.javascript.Scheduling.doAddScheduledJob(),
            routes.javascript.Scheduling.editScheduledJob(),
            routes.javascript.Scheduling.doEditScheduledJob(),
            routes.javascript.Scheduling.deleteScheduledJob(),
            routes.javascript.Scheduling.removeJobFromSchedule(),
            routes.javascript.Scheduling.deleteNextJob(),
            routes.javascript.Scheduling.setNextJob(),
            routes.javascript.Scheduling.listSchedulesJSON()
        ))
        .as("text/javascript");
  }
}