package controllers

import java.util
import java.util.{Collections, Timer, TimerTask}
import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Source
import models.{TestRun, User}
import play.Logger
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator, Iteratee}
import play.api.libs.streams.Streams
import play.api.mvc._

import scala.collection.JavaConversions._

@Singleton
class TestLogFeeder @Inject()() extends Controller {
  val timerMap = new util.HashMap[String, (Timer, Enumerator[LogRecord])]
  val (logEnumerator, firstLevelLogChannel) = Concurrent.broadcast[LogRecord]

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
      val user = Authentication.getLocalUser(java_session)

      val enumerator = createTimerForUser(user)

      val toPublisher = Streams.enumeratorToPublisher(enumerator &> logFilter(user))
      //create
      val source = Source.fromPublisher(toPublisher)

      Ok.chunked(source.map(lr => lr.log) via EventSource.flow).as("text/event-stream")
  }

  def buffer(logRecord: LogRecord): Unit = {
    firstLevelLogChannel push logRecord
  }

  def log(logRecord: LogRecord): Unit = {
    Thread.sleep(5)
    Logger.debug(logRecord.log)
    currentLog.add(logRecord)
    buffer(logRecord)
  }

  def log(log: String): Unit = {
    Thread.sleep(5)
    val logRecord = LogRecord(null, log)
    Logger.debug(log)
    currentLog.add(logRecord)
    buffer(logRecord)
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
  //creating a timer for each request was bad design.
  // Now we create a timer for each person, no matter how many conncetions she has
  def createTimerForUser(user: User): Enumerator[LogRecord] = {
    timerMap.synchronized {
      if (!timerMap.containsKey(user.email)) {
        val name = "logTimer-[" + user.email + "]"


        val (secondLevelLogEnumerator, secondLevelLogChannel) = Concurrent.broadcast[LogRecord]

        val timer = new Timer(name) {
          override def finalize(): Unit = {
            super.finalize()
            Logger.debug(s"Timer [$name] GCed")
          }
        }
        val task = new TimerTask {
          Logger.debug(s"Create new timer task for ${user.email}")
          val logBuffer = new util.LinkedList[LogRecord]()

          val firstLevelConsumer = Iteratee.foreach[LogRecord](m => {
            logBuffer.synchronized {
              logBuffer.add(m)
            }
          })

          //bind the first level channel to the first level consumer
          logEnumerator |>>> firstLevelConsumer

          override def run(): Unit = {
            try {
              logBuffer.synchronized {
                if (logBuffer.size() > 0) {
                  while (!logBuffer.isEmpty) {
                    secondLevelLogChannel.push(logBuffer.remove(0))
                  }
                }
              }
            } catch {
              case th: Throwable => {
                Logger.error(th.getMessage, th)
              }
            }
          }
        }

        timer.scheduleAtFixedRate(task, 5000, 5000)
        timerMap.put(user.email, (timer, secondLevelLogEnumerator))
        secondLevelLogEnumerator
      } else {
        timerMap.get(user.email)._2
      }
    }
  }

}

case class LogRecord(testRun: TestRun, log: String) {
}
