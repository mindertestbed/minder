package controllers

import java.util
import java.util.Collections

import models.{User, TestRun}
import play.Logger
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.mvc._
import scala.collection.JavaConversions._

object TestLogFeeder extends Controller {
  val (logOut, logChannel) = Concurrent.broadcast[LogRecord];

  val currentLog = Collections.synchronizedList(new util.ArrayList[LogRecord]())

  def clear() = currentLog.clear()

  /**
    * This filter makes sure that only the people allowed will see the log record
    *
    * @param user
    * @return
    */
  def logFilter(user: models.User) = Enumeratee.filter[LogRecord] {
    logRecord =>
      if (logRecord.testRun != null && logRecord.testRun.runner != null) {
        global.Util.canAccess(user, logRecord.testRun.runner, logRecord.testRun.visibility)
      } else {
        true
      }
  }

  /**
    * This filter renders the log
    *
    * @return
    */
  def logRenderer(): Enumeratee[LogRecord, String] = Enumeratee.map[LogRecord] {
    logRecord => logRecord.log
  }

  /**
    * An action that provides information about the current
    * running job.
    *
    * @return
    */
  def logFeed() = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      val user = Authentication.getLocalUser(java_session);
      Ok.chunked(logOut &> logFilter(user) &> logRenderer() &> EventSource()).as("text/event-stream")
  }


  def log(logRecord: LogRecord): Unit = {
    Thread.sleep(5);
    Logger.debug(logRecord.log)
    currentLog.add(logRecord)
    logChannel.push(logRecord)
  }

  def log(log: String): Unit = {
    Thread.sleep(5);
    val logRecord = LogRecord(null, log)
    Logger.debug(log)
    currentLog.add(logRecord)
    logChannel.push(logRecord)
  }

  /**
    * Renders the current log with respect to the user access
    *
    * @return
    */
  def currentLogString(localUser: User): String = {
    val stringBuilder = new StringBuilder
    for (logRecord <- currentLog) {
      if (logRecord.testRun != null) {
        if (global.Util.canAccess(localUser, logRecord.testRun.runner, logRecord.testRun.visibility)) {
          stringBuilder append logRecord.log
        }
      } else {
        stringBuilder append logRecord.log
      }
    }
    stringBuilder toString
  }

  case class LogRecord(testRun: TestRun, log: String) {
  }

}
