package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.authentication.profile;
import views.html.rootViews.rootPage;

import javax.inject.Inject;

public class Application extends Controller {
  Authentication authentication;
  TestLogFeeder testLogFeeder;


  @Inject
  public Application(Authentication authentication, TestLogFeeder testLogFeeder) {
    this.authentication = authentication;
    this.testLogFeeder = testLogFeeder;
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER, Role.TEST_DEVELOPER})
  public Result index() {
    final User localUser = authentication.getLocalUser();
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
    final User localUser = authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.group.testGroupListView.render(authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result adapters() {
    final User localUser = authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.adapters.wrapperManager.render(authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result jobQueue() {
    final User localUser = authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.job.jobQueue.render(authentication, testLogFeeder));
  }

  public Result about() {
    final User localUser = authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(views.html.aboutPage.render(authentication));
  }

  @AllowedRoles({Role.TEST_DEVELOPER, Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result profile() {
    final User localUser = authentication.getLocalUser();
    return ok(profile.render(localUser, authentication));
  }
}
