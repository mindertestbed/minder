package controllers;

import models.User;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;
import views.html.rootViews.rootPage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Application extends Controller {

  public static Result index() {
    return ok(index.render());
  }

  @Security.Authenticated(Secured.class)
  public static Result restrictedObserver() {
    final User localUser = Authentication.getLocalUser();
    return ok(restrictedObserver.render(localUser));
  }

  public static Result root(String display) {
    final User localUser = Authentication.getLocalUser();
    System.out.println(localUser.email);
    if (localUser.email.equals("root@minder")) {
      return ok(rootPage.render(display));
    } else {
      return badRequest("You cannot access this resoruce.");
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result restrictedTestDesigner(String display) {
    final User localUser = Authentication.getLocalUser();

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(restrictedTestDesigner.render(localUser, display));
  }

  @Security.Authenticated(Secured.class)
  public static Result createNewTest() {
    final User localUser = Authentication.getLocalUser();
    session().put("testPageMode", "new");
    return ok(restrictedTestDesigner.render(localUser, "designWithGui"));
  }

  @Security.Authenticated(Secured.class)
  public static Result restrictedTestDeveloper() {
    final User localUser = Authentication.getLocalUser();
    return ok(restrictedTestDeveloper.render(localUser));
  }

  @Security.Authenticated(Secured.class)
  public static Result profile() {
    final User localUser = Authentication.getLocalUser();
    return ok(profile.render(localUser));
  }

  public static Result jsRoutes() {
    return ok(
        Routes.javascriptRouter("jsRoutes",
            routes.javascript.Authentication.login()))
        .as("text/javascript");
  }

  public static String formatTimestamp(final long t) {
    return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
  }
}