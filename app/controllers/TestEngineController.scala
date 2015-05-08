package controllers


import java.util.Date
import java.util.concurrent.LinkedBlockingQueue

import controllers.common.enumeration.{OperationType, TestStatus}
import minderengine.{MinderWrapperRegistry, MinderSignalRegistry, SessionMap, TestEngine}
import models._
import mtdl.SignalSlotInfoProvider
import play.Logger
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.mvc._
import play.mvc.Http
import views.html._

import scala.collection.JavaConversions._

/**
 * Manages the job lifecycle.
 * Holds a job queue.
 */
object TestEngineController extends Controller {

  /**
   * SSE tunnel for listing the active jobs running now.
   */
  //val (jobListOut, jobListChannel) = Concurrent.broadcast[JsValue];
  val (jobQueueOut, jobQueueChannel) = Concurrent.broadcast[String];
  val (jobStatusOut, jobStatusChannel) = Concurrent.broadcast[String];
  val (jobHistoryOut, jobHistoryChannel) = Concurrent.broadcast[String];
  val (logOut, logChannel) = Concurrent.broadcast[String];

  /**
   * The queue that holds test runs. When a job is started, a test run is created for it.
   * One can access all the information about a test run and a job here.
   */
  val jobQueue = new LinkedBlockingQueue[TestRunContext]();
  var activeRunContext: TestRunContext = null;


  //add some dummy test runs for development

  //for (i <- 0 to 5) {
  //  jobQueue.offer(createDummyTestRun(i))
  //}

  /**
   * A reusable thread pool for server side events
   */
  val threadPool = java.util.concurrent.Executors.newFixedThreadPool(20);

  /**
   * The main test thread
   */
  val testThread = new Thread() {
    override def run(): Unit = {
      while (true) {
        Logger.info(">>> Test Thread waiting on job queue")
        logFeedUpdate(">>> Test Thread waiting on job queue");
        activeRunContext = TestEngineController.jobQueue.take();
        SessionMap.registerObject(activeRunContext.testRun.runner.email, "signalRegistry", new MinderSignalRegistry());
        SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())

        jobFeedUpdate()
        queueFeedUpdate()
        Logger.info(">>> Job with id [" + activeRunContext.testRun.job.id + "] arrived. Wait 5 seconds...")
        logFeedUpdate(">>> Job with id [" + activeRunContext.testRun.job.id + "] arrived. Start in 5 seconds...");
        Thread.sleep(5000);
        Logger.info("Run Job [" + activeRunContext.testRun.job.id + "]")
        logFeedUpdate(">>> Run Job [" + activeRunContext.testRun.job.id + "]")
        try {
          activeRunContext.run()
        } catch {
          case th: Throwable =>
            Logger.error(th.getMessage, th)
        } finally {
          logFeedUpdate(">>> Test Run  #[" + activeRunContext.testRun.number + "] finished.")
          activeRunContext = null
        }

        queueFeedUpdate()
        jobFeedUpdate()
      }
    }
  };

  testThread.start();


  /**
   * An action that provides information about the current
   * jobs in the queue.
   * @return
   */
  def jobQueueFeed() = Action {
    println("Incoming request")
    threadPool.submit(new Runnable {
      override def run(): Unit = {
        Thread.sleep(1000);
        try {
          queueFeedUpdate()
        } catch {
          case th: Throwable => {
            th.printStackTrace()
          }
        }

      }
    });
    Ok.chunked(jobQueueOut &> EventSource()).as("text/event-stream")
  }

  /**
   * An action that provides information about the current
   * running job.
   * @return
   */
  def jobStatusFeed() = Action {
    println("Incoming request")
    threadPool.submit(new Runnable {
      override def run(): Unit = {
        Thread.sleep(1000);
        try {
          jobFeedUpdate()
        } catch {
          case th: Throwable => {
            th.printStackTrace()
          }
        }
      }
    });
    Ok.chunked(jobStatusOut &> EventSource()).as("text/event-stream")
  }

  /**
   * An online feed for listing jobs runned
   * @return
   */
  def historyFeed() = Action {
    Ok.chunked(jobHistoryOut &> EventSource()).as("text/event-stream")
  }


  /**
   * An action that provides information about the current
   * running job.
   * @return
   */
  def logFeed() = Action {
    println("Log feed")
    Ok.chunked(logOut &> EventSource()).as("text/event-stream")
  }


  /**
   * an action for enqueuing a new job for test engine running
   * @return
   */
  def enqueueJob(id: Long) = Action { implicit request =>
    if (jobQueue.size >= 10) {
      BadRequest("The job queue is full.")
    } else {
      //check if the job is already in queue.
      var already = false;
      for (tr <- jobQueue) {
        if (tr.job != null && tr.job.id == id) {
          already = true;
        }
      }
      if (already) {
        BadRequest("The job is already enqueued.")
      } else {

        val job = Job.findById(id)

        if (job == null) {
          BadRequest("A job with id [" + id + "] was not found!")
        } else {
          //everything is fine. Create a test run and add to queue.
          // if (jobQueue.contains())

          val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
          val java_session = java_ctx.session()

          jobQueue.offer(createTestRunContext(job, Application.getLocalUser(java_session)))
          queueFeedUpdate();
          Ok;
        }
      }
    }
  }


  /**
   * Update the queue feed with the current active job and queue status
   */
  def queueFeedUpdate(): Unit = {
    //Json.toJson(jobQueue.map(tr => {
    //  Map("jobId" -> (tr.job.id + ""), "jobName" -> tr.job.name, "startedBy" -> tr.runner.email, "status" -> tr.status.description,
    //  "progress" -> (tr.progress + ""))
    //}))

    jobQueueChannel.push(jobQueueList.render().toString())
    jobHistoryChannel.push(jobHistoryList.render().toString())
  }

  def jobFeedUpdate(): Unit = {
    jobStatusChannel.push(testStatusMonitor.apply().toString());
  }


  def logFeedUpdate(log: String): Unit = {
    logChannel.push(log);
  }

  /**
   * Utility method to create a test run
   * @return
   */

  def createTestRunContext(job: Job, user: User): TestRunContext = {
    Logger.debug("Create Run")
    val testRun = new TestRun()
    testRun.date = new Date()
    testRun.job = job;
    val userHistory = new UserHistory
    userHistory.user = user;
    userHistory.operationType = new TOperationType
    userHistory.operationType.name = OperationType.RUN_TEST_CASE
    userHistory.operationType.save()
    testRun.history = userHistory
    testRun.runner = user;
    new TestRunContext(testRun)
  }

  //
  def createDummyTestRun(i: Int): TestRun = {

    val tpl = Array((5,6))

    for( (l,r) <- tpl){

    }
    val tr = new TestRun()
    tr.date = new Date()
    tr.job = Job.findById(361L);
    tr.runner = User.findByEmail("myildiz83@gmail.com")
    tr;
  }

  def cancelJob(index: Int) = Action { implicit request =>
    if (index >= 0 && index < jobQueue.size()) {
      val arr = jobQueue.toArray

      val tr = arr(index).asInstanceOf[TestRun]
      jobQueue.remove(tr)
      queueFeedUpdate();
    }
    Ok
  }
}
