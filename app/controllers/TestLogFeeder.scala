package controllers

import java.util
import java.util.Collections
import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.{Flow, Sink, Source}
import models.{TestRun, User}
import play.Logger
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.streams.Streams
import play.api.mvc._

import scala.collection.JavaConversions._

@Singleton
class TestLogFeeder @Inject()() extends Controller {
  val (logEnumerator, logChannel) = Concurrent.broadcast[LogRecord];

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
        utils.Util.canAccess(user, logRecord.testRun.runner, logRecord.testRun.visibility)
      } else {
        true
      }
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

      val source = Source.fromPublisher(Streams.enumeratorToPublisher(logEnumerator &> logFilter(user)))
      Ok.chunked(source.map { logRecord => logRecord.log } via EventSource.flow).as("text/event-stream")
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
        if (utils.Util.canAccess(localUser, logRecord.testRun.runner, logRecord.testRun.visibility)) {
          stringBuilder append logRecord.log
        }
      } else {
        stringBuilder append logRecord.log
      }
    }
    stringBuilder toString
  }


}

case class LogRecord(testRun: TestRun, log: String) {
}
