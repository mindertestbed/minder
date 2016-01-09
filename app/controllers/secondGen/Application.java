package controllers.secondGen;


import play.mvc.Controller;
import play.mvc.Result;
import views.html.ui2.*;

/**
 * @author: yerlibilgin
 * @date: 08/12/15.
 */
public class Application extends Controller {
  public static Result main() {
    return ok(main.render());
  }
  public static Result loginPage() {
    return ok(login.render());
  }
  public static Result mainLayout() {
    return ok(mainLayout.render());
  }

}
