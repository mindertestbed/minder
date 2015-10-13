package rest.controllers;


import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author: yerlibilgin
 * @date: 13/10/15.
 */
public class LoginController extends Controller {
  /**
   * Render login state
   * @return
   */
  public static Result login() {
    /// check the ACCEPT-TYPE header,
    // if the clients wants application/xml then send xml
    // if  ... ... . .... . application/json then send json


    return ok("   <?xml version=\"1.0\"?> \n" +
        "    <minderLogin> \n" +
        "       <userName></userName> \n" +
        "       <password></password> \n" +
        "       <link rel=\"login\" href=\"http://minder/login\" /> \n" +
        "     </minderLogin >");
  }

  public static Result doLogin() {
    return ok("   <?xml version=\"1.0\"?> \n" +
        "    <minderLogin> \n" +
        "       <userName></userName> \n" +
        "       <password></password> \n" +
        "       <link rel=\"login\" href=\"http://minder/login\" /> \n" +
        "     </minderLogin >");
  }

}
