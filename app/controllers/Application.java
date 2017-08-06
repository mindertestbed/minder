package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.rootViews.rootPage;

import javax.inject.Inject;

public class Application extends Controller {
  Authentication authentication;
  TestLogFeeder testLogFeeder;
  TestQueueController testQueueController;

  @Inject
  public Application(Authentication authentication, TestLogFeeder testLogFeeder, TestQueueController testQueueController) {
    this.authentication = authentication;
    this.testLogFeeder = testLogFeeder;
    this.testQueueController = testQueueController;
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER, Role.TEST_DEVELOPER})
  public Result index() {
    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.index.render(null, authentication));
  }

  public Result root(String display) {
    final User localUser = authentication.getLocalUser();
    if (localUser == null)
      return badRequest("You cannot access this resoruce.");

    if (localUser.email.equals("root@minder")) {
      return ok(rootPage.render(display, authentication));
    } else {
      return badRequest("You cannot access this resoruce.");
    }
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result testGroups() {
    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.group.testGroupListView.render(authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result adapters() {
    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.adapters.wrapperManager.render(authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result jobQueue() {
    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.job.jobQueue.render(authentication, testLogFeeder));
  }


  public Result restartQueThread() {
    final User localUser = authentication.getLocalUser();
    if (localUser == null)
      return badRequest("You cannot access this resoruce.");

    if (localUser.email.equals("root@minder")) {
      //only root
      testQueueController.reset();
      return ok();
    } else {
      return badRequest("You cannot access this resoruce.");
    }

  }

  public Result about() {
    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.aboutPage.render(authentication));
  }
}
