package controllers

import models.User
import play.api.mvc._

object AuthenticationScala extends Controller {
  def getLocalUser()(implicit req: RequestHeader) : User = {
    val email = request2session.get("email").get;
    if (email == null)
      return null;
    val user = User.findByEmail(email);
    return user;
  }
}