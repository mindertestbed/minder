package controllers

import java.util

import controllers.Tester._
import play.Logger
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.mvc.Action
import play.mvc.Controller

object UserManager extends Controller{
  def activeUsers() = Action { implicit request =>
    com.feth.play.module.pa.controllers.Authenticate.noCache(Controller.response())
    Logger.debug("Something is happening")
    Ok.chunked(timeOut &>  EventSource()).as("text/event-stream")
  }

  val (timeOut, timeChannel) = Concurrent.broadcast[String]
  val activeUserList: util.ArrayList[String] = new util.ArrayList[String]

  val timeThread = new Thread() {
    override def run(): Unit = {
      val arrs = Array(".", "..", "...")
      var i = 0
      while (true) {
        Thread.sleep(1000);
        timeChannel.push("data:" + activeUserList.size() + arrs(i) + Thread.currentThread().getId);
        i += 1
        i = i % 3
      }
    }
  }

  timeThread.start()

  def addUser(email: String): Unit = {
    Logger.debug("New user " + email)
    activeUserList.add(email);
  }
}