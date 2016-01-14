package controllers

import java.util.concurrent.LinkedBlockingQueue

import models.TestRun
import net.sf.jasperreports.compilers.JavaScriptEvaluatorScope.JSValue
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object TestRunFeeder extends Controller {
  val (jobQueueOut, jobQueueChannel) = Concurrent.broadcast[String];
  val (testStatusOut, testStatusChannel) = Concurrent.broadcast[String];
  val (jobHistoryOut, jobHistoryChannel) = Concurrent.broadcast[TestRun];

  def historyFilter(user: models.User) = Enumeratee.filter[TestRun] {
    testRun: TestRun => global.Util.canAccess(user, testRun.runner, testRun.visibility)
  }

  def historyJsonRenderer(): Enumeratee[TestRun, String] = Enumeratee.map[TestRun] {
    testRun: TestRun => {
      var id = testRun.id
      if (id == null) id = -1L
      JsObject(Seq(
        "success" -> JsBoolean(testRun.success),
        "no" -> JsString(testRun.number + ""),
        "id" -> JsString(id + ""),
        "jobId" -> JsString(testRun.job.id + ""),
        "visibility" -> JsString(testRun.visibility.name()),
        "name" -> JsString(testRun.job.name + "")
      )).toString()
    }
  }

  def queueRenderer(user: models.User): Enumeratee[String, String] = Enumeratee.map[String] {
    dummy => {
      TestQueueController.jobQueue.synchronized {
        val queue = TestQueueController.jobQueue

        if (queue.isEmpty && TestQueueController.activeRunContext == null)
          ""
        else {
          val sb = new StringBuilder
          sb.append("[")

          //if we have an active run context, send it with an -1 index.
          if (TestQueueController.activeRunContext != null) {
            val cotx = TestQueueController.activeRunContext
            sb.append("{\"jobId\":\"").append(cotx.testRun.job.id).append("\",").
              append("\"jobName\":\"").append(cotx.testRun.job.name).append("\",").
              append("\"email\":\"").append(cotx.testRun.runner.email).append("\",").
              append("\"progress\":\"").append(cotx.progressPercent).append("\",").
              append("\"index\":\"").append(-1).append("\"").
              append("}")
            if (!queue.isEmpty) {
              sb.append(",")
            }
          }

          var index = 0
          for (cotx <- queue) {
            sb.append("{\"jobId\":\"").append(cotx.testRun.job.id).append("\",").
              append("\"jobName\":\"").append(cotx.testRun.job.name).append("\",").
              append("\"email\":\"").append(cotx.testRun.runner.email).append("\",").
              append("\"progress\":\"").append(cotx.progressPercent).append("\",").
              append("\"index\":\"").append(index).append("\"").
              append("}")
            if (index != queue.size() - 1)
              sb.append(",")

            index = index + 1
          }
          sb.append("]")

          sb.toString()
        }
      }
    }
  }

  /**
    * An online feed for listing jobs runned
    * @return
    */
  def jobHistoryFeed() = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      val user = Authentication.getLocalUser(java_session);

      Ok.chunked(jobHistoryOut &> historyFilter(user) &> historyJsonRenderer() &> EventSource()).as("text/event-stream")
  }


  /**
    * An action that provides information about the current
    * jobs in the queue.
    * @return
    */
  def jobQueueFeed() = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      val user = Authentication.getLocalUser(java_session);

      new Thread() {
        override def run() {
          Thread.sleep(1000);
          jobQueueUpdate()
        }
      }.start()
      Ok.chunked(jobQueueOut &> queueRenderer(user) &> EventSource()).as("text/event-stream")
  }


  /**
    * An action that provides information about the current
    * running job.
    * @return
    */
  def testStatusFeed() = Action {
    Ok.chunked(testStatusOut &> EventSource()).as("text/event-stream")
  }


  def jobHistoryUpdate(run: TestRun): Unit = {
    jobHistoryChannel.push(run)
  }

  def jobQueueUpdate(): Unit = {
    jobQueueChannel.push("")
  }

  def testStatusUpdate(progress: Int): Unit = {
    testStatusChannel.push(progress + "");
  }
}