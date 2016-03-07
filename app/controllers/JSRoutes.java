package controllers;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

public class JSRoutes extends Controller {
  public static Result jsRoutes() {
    return ok(
        Routes.javascriptRouter("jsRoutes",
            routes.javascript.Authentication.login(),
            routes.javascript.Authentication.doLogin(),
            routes.javascript.GroupController.renderDetails(),
            routes.javascript.GroupController.renderTestAssertionList(),
            routes.javascript.GroupController.renderTestSuites(),
            routes.javascript.GroupController.renderTestAssets(),
            routes.javascript.GroupController.renderUtilClasses(),
            routes.javascript.GroupController.renderDependencies(),
            routes.javascript.GroupController.renderJobTemplates(),
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
            routes.javascript.TestSuiteController.renderEditor()
        ))
        .as("text/javascript");
  }
}