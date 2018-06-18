package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Source
import minderengine.SuspensionContext
import play.api.Logger
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.streams.Streams
import play.api.mvc._

import scala.collection.JavaConversions._

@Singleton
class TestRunFeeder @Inject()(implicit testQueueController: TestQueueController, authentication: Authentication, ePQueueManager: EPQueueManager) extends Controller {
  val (jobQueueOut, jobQueueChannel) = Concurrent.broadcast[String];
  val (testStatusOut, testProgressChannel) = Concurrent.broadcast[String];
  val (jobHistoryEnumerator, jobHistoryChannel) = Concurrent.broadcast[String];

  def historyJsonRenderer(): Enumeratee[String, String] = Enumeratee.map[String] {
    _ => " "
  }

  def queueRenderer(user: models.User): Enumeratee[String, String] = Enumeratee.map[String] {
    dummy => {
      testQueueController.jobQueue.synchronized {
        val queue = testQueueController.jobQueue
        val sb = new StringBuilder
        sb.append("[")

        //if we have an active run context, send it with an -1 index.
        if (testQueueController.activeRunContext != null) {
          val context = testQueueController.activeRunContext
          sb.append("{\"jobId\":\"").append(context.testRun.job.id).append("\",").
            append("\"jobName\":\"").append(context.testRun.job.name).append("\",").
            append("\"email\":\"").append(context.testRun.runner.email).append("\",").
            append("\"number\":\"").append(context.testRun.number).append("\",").
            append("\"progress\":\"").append(context.progressPercent).append("\",").
            append("\"status\":\"activeJob\"").
            append("}")
          sb.append(",")
        }

        var index = 0
        for (context <- queue) {
          sb.append("{\"jobId\":\"").append(context.testRun.job.id).append("\",").
            append("\"jobName\":\"").append(context.testRun.job.name).append("\",").
            append("\"email\":\"").append(context.testRun.runner.email).append("\",").
            append("\"number\":").append(context.testRun.number).append(",").
            append("\"progress\":\"").append(context.progressPercent).append("\",").
            append("\"status\":\"pendingJob\"").
            append("}")
          sb.append(",")
          index = index + 1
        }
        Logger.debug(" size " + ePQueueManager.endpointQueueMap.size())
        //list the suspended tests
        for (queue <- ePQueueManager.endpointQueueMap.values()) {
          for (context <- queue) {
            sb.append("{\"jobId\":\"").append(context.testRun.job.id).append("\",").
              append("\"jobName\":\"").append(context.testRun.job.name).append("\",").
              append("\"email\":\"").append(context.testRun.runner.email).append("\",").
              append("\"number\":").append(context.testRun.number).append(",").
              append("\"progress\":\"").append(context.progressPercent).append("\",").
              append("\"status\":\"suspendedJob\"").
              append("}")
            sb.append(",")
          }
        }
        //list the suspended tests
        Logger.debug(" size " + SuspensionContext.get().getTestContextMap().size())
        for (context <- SuspensionContext.get().getTestContextMap().values()) {
          sb.append("{\"jobId\":\"").append(context.testRun.job.id).append("\",").
            append("\"jobName\":\"").append(context.testRun.job.name).append("\",").
            append("\"email\":\"").append(context.testRun.runner.email).append("\",").
            append("\"number\":").append(context.testRun.number).append(",").
            append("\"progress\":\"").append(context.progressPercent).append("\",").
            append("\"status\":\"suspendedJob\"").
            append("}")
          sb.append(",")
          index = index + 1
        }

        if (sb.charAt(sb.length - 1) == ',')
          sb.deleteCharAt(sb.length - 1)

        sb.append("]")

        sb.toString()
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

      val source = Source.fromPublisher(Streams.enumeratorToPublisher(jobHistoryEnumerator &> historyJsonRenderer));
      Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }


  /**
    * Render the small view for job history
    */
  def jobHistorySM(page: Int) = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      implicit val localUser = Authentication.getLocalUser(java_session)

      Ok(views.html.job.jobHistorySM(page));
  }

  /**
    * Render the small view for job history
    */
  def pagedHistoryList(page: Int, maxPages: Int, pageSize: Int) = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      implicit val localUser = Authentication.getLocalUser(java_session)

      Ok(views.html.job.pagedHistoryList(page, maxPages, pageSize));
  }

  /**
    * Render the page navigation for history
    */
  def pagedHistoryNav(page: Int, maxPages: Int, pageSize: Int) = Action {
    implicit request =>
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      implicit val localUser = Authentication.getLocalUser(java_session)

      Ok(views.html.job.pagedHistoryNav(page, maxPages, pageSize));
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


  def jobHistoryUpdate(): Unit = {
    jobHistoryChannel.push("")
  }

  def jobQueueUpdate(): Unit = {
    jobQueueChannel.push("")
  }

  def testProgressUpdate(progress: Int): Unit = {
    testProgressChannel.push(progress + "");
  }
}