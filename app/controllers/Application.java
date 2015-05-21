package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import minderengine.MinderSignalRegistry;
import minderengine.SessionMap;
import minderengine.XoolaServer;
import models.User;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

import play.Logger;
import play.Routes;
import play.api.Play;
import play.api.mvc.RequestHeader;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import views.html.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import views.html.rootViews.rootPage;

public class Application extends Controller {

  public static final String FLASH_MESSAGE_KEY = "message";
  public static final String FLASH_ERROR_KEY = "error";
  public static final String OBSERVER_ROLE = "observer";
  public static final String TEST_DESIGNER_ROLE = "Test Designer";
  public static final String TEST_DEVELOPER_ROLE = "Test Developer";

  public static Result index() {
    return ok(index.render());
  }

  public static User getLocalUser(final Session session) {
    final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
    final User localUser = User.findByAuthUserIdentity(currentAuthUser);
    return localUser;
  }

  @Restrict(@Group(Application.OBSERVER_ROLE))
  public static Result restrictedObserver() {
    final User localUser = getLocalUser(session());
    return ok(restrictedObserver.render(localUser));
  }

  public static Result root(String display) {
    final User localUser = getLocalUser(session());
    System.out.println(localUser.email);
    if (localUser.email.equals("root@minder")) {
      return ok(rootPage.render(display));
    } else {
      return badRequest("You cannot access this resoruce.");
    }
  }



  @Restrict(@Group(Application.TEST_DESIGNER_ROLE))
  public static Result restrictedTestDesigner(String display) {
    final User localUser = getLocalUser(session());

    if (!session().containsKey("testPageMode")) {
      session().put("testPageMode", "none");
    }
    return ok(restrictedTestDesigner.render(localUser, display));
  }

  @Restrict(@Group(Application.TEST_DESIGNER_ROLE))
  public static Result createNewTest() {
    final User localUser = getLocalUser(session());
    session().put("testPageMode", "new");
    return ok(restrictedTestDesigner.render(localUser, "designWithGui"));
  }

  @Restrict(@Group(Application.TEST_DEVELOPER_ROLE))
  public static Result restrictedTestDeveloper() {
    final User localUser = getLocalUser(session());
    return ok(restrictedTestDeveloper.render(localUser));
  }

  @Restrict({@Group(Application.OBSERVER_ROLE), @Group(Application.TEST_DESIGNER_ROLE), @Group(Application.TEST_DEVELOPER_ROLE)})
  public static Result profile() {
    final User localUser = getLocalUser(session());
    return ok(profile.render(localUser));
  }

  public static Result login() {
    return ok(login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
  }
  public static Result doLogin() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      System.out.println("There are errors");
      // User did not fill everything properly
      return badRequest(login.render(filledForm));
    } else {
      MyLogin lgn = filledForm.get();
      // Everything was filled
      Result result = UsernamePasswordAuthProvider.handleLogin(ctx());

      if (result.toScala().header().status() == SEE_OTHER) {
        //if the user successfully logged in, then we have to create a new signal registry for him.
        SessionMap.registerObject(lgn.getEmail(), "signalRegistry", new MinderSignalRegistry());
      } else {
        Logger.debug("Different status " + result.toScala().header().status());
      }
      return result;
    }
  }

  public static Result signup() {
    return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
  }

  public static Result jsRoutes() {
    return ok(
        Routes.javascriptRouter("jsRoutes",
            controllers.routes.javascript.Signup.forgotPassword()))
        .as("text/javascript");
  }

  public static Result doSignup() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
        .bindFromRequest();
    if (filledForm.hasErrors()) {
      // User did not fill everything properly
      return badRequest(signup.render(filledForm));
    } else {
      // Everything was filled
      // do something with your part of the form before handling the owner
      // signup
      return UsernamePasswordAuthProvider.handleSignup(ctx());
    }
  }

  public static String formatTimestamp(final long t) {
    return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
  }
}