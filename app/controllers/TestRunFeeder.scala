package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Source
import models.{TestRun, TestRunStatus}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.json._
import play.api.libs.streams.Streams
import play.api.mvc._

import scala.collection.JavaConversions._

@Singleton
class TestRunFeeder @Inject()(implicit testQueueController: TestQueueController) extends Controller {
  val (jobQueueOut, jobQueueChannel) = Concurrent.broadcast[String];
  val (testStatusOut, testProgressChannel) = Concurrent.broadcast[String];
  val (jobHistoryEnumerator, jobHistoryChannel) = Concurrent.broadcast[TestRun];

  def historyFilter(user: models.User) = Enumeratee.filter[TestRun] {
    testRun: TestRun => utils.Util.canAccess(user, testRun.runner, testRun.visibility)
  }

  def historyJsonRenderer(): Enumeratee[TestRun, String] = Enumeratee.map[TestRun] {
    testRun: TestRun => {
      var id = testRun.id
      if (id == null) id = -1L
      JsObject(Seq(
        "success" -> JsBoolean(testRun.status == TestRunStatus.SUCCESS),
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
      testQueueController.jobQueue.synchronized {
        val queue = testQueueController.jobQueue
        if (queue.isEmpty && testQueueController.activeRunContext == null)
          ""
        else {
          val sb = new StringBuilder
          sb.append("[")

          //if we have an active run context, send it with an -1 index.
          if (testQueueController.activeRunContext != null) {
            val cotx = testQueueController.activeRunContext
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
    *
    * @return
    */
  def jobHistoryFeed() = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      val user = Authentication.getLocalUser(java_session);

      val source = Source.fromPublisher(Streams.enumeratorToPublisher(jobHistoryEnumerator &> historyFilter(user) &> historyJsonRenderer));
      Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }


  /**
    * An action that provides information about the current
    * jobs in the queue.
    *
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

      val source = Source.fromPublisher(Streams.enumeratorToPublisher(jobQueueOut &> queueRenderer(user)));
      Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }


  /**
    * An action that provides information about the current
    * running job.
    *
    * @return
    */
  def testProgressFeed() = Action {
    val source = Source.fromPublisher(Streams.enumeratorToPublisher(testStatusOut));
    Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }


  def jobHistoryUpdate(run: TestRun): Unit = {
    jobHistoryChannel.push(run)
  }

  def jobQueueUpdate(): Unit = {
    jobQueueChannel.push("")
  }

  def testProgressUpdate(progress: Int): Unit = {
    testProgressChannel.push(progress + "");
  }
}