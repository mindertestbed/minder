package controllers;

import models.User;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.AllowedRoles;
import security.Role;
import views.html.authentication.profile;
import views.html.rootViews.rootPage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Application extends Controller {

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER, Role.TEST_DEVELOPER})
  public static Result index() {
    final User localUser = Authentication.getLocalUser();
    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.index.render());
  }

  public static Result root(String display) {
    final User localUser = Authentication.getLocalUser();
    if (localUser == null)
      return badRequest("You cannot access this resoruce.");

    if (localUser.email.equals("root@minder")) {
      return ok(rootPage.render(display));
    } else {
      return badRequest("You cannot access this resoruce.");
    }
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result testGroups() {
    final User localUser = Authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.group.testGroupListView.render());
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result adapters() {
    final User localUser = Authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.adapters.wrapperManager.render());
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result jobQueue() {
    final User localUser = Authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.job.jobQueue.render());
  }

  public static Result about() {
    final User localUser = Authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.aboutPage.render());
  }

  @AllowedRoles({Role.TEST_DEVELOPER, Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public static Result profile() {
    final User localUser = Authentication.getLocalUser();
    return ok(profile.render(localUser));
  }

  public static Result jsRoutes() {
    return ok(
        Routes.javascriptRouter("jsRoutes",
            routes.javascript.Authentication.login(),
            routes.javascript.Authentication.doLogin(),
            routes.javascript.TestSuiteController.listAvailableTdlsForSuite(),
            routes.javascript.TestSuiteController.getTestSuiteDetailView(),
            routes.javascript.TestSuiteController.renderJoblistView(),
            routes.javascript.TestSuiteController.renderTestRunListView(),
            routes.javascript.TestSuiteController.renderDetailView(),
            routes.javascript.TestSuiteController.renderTdlList(),
            routes.javascript.GroupController.renderDetails(),
            routes.javascript.GroupController.renderTestAssertionList(),
            routes.javascript.GroupController.renderTestSuites(),
            routes.javascript.GroupController.renderTestAssets(),
            routes.javascript.GroupController.renderUtilClasses(),
            routes.javascript.GroupController.renderDependencies(),
            routes.javascript.TestAssertionController.renderDetails(),
            routes.javascript.TestAssertionController.renderTestCases(),
            routes.javascript.JobController.changeTestRunVisibility(),
            routes.javascript.JobController.changeJobVisibility(),
            routes.javascript.TestQueueController.enqueueJob(),
            routes.javascript.UserController.listUsers(),
            routes.javascript.UserController.viewSettings(),
                routes.javascript.GroupController.renderJobTemplates()
        ))
        .as("text/javascript");
  }

  public static String formatTimestamp(final long t) {
    return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
  }
}