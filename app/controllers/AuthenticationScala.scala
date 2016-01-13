package controllers

import java.util
import java.util.{Observable, Observer}

import minderengine.MinderWrapperRegistry
import models.{User, Job, WrapperVersion}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object AuthenticationScala extends Controller {
  def getLocalUser()(implicit req: RequestHeader) : User = {
    val email = request2session.get("email").get;
    if (email == null)
      return null;
    val user = User.findByEmail(email);
    return user;
  }
}