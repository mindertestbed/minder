package controllers

import java.util.Date
import java.util.concurrent.LinkedBlockingQueue
import controllers.common.enumeration.OperationType
import minderengine.{MinderSignalRegistry, SessionMap}
import models._
import play.Logger
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.mvc._
import views.html.testDesigner.job._
import rest.controllers.GitbTestbedController
import com.gitb.core.v1.StepStatus

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

  //a flag indicating the life of the thread.
  var goon = true;

  val currentLog = new StringBuilder
  /**
    * The main test thread
    */
  val testThread = new Thread() {
    override def run(): Unit = {
      while (goon) {
        currentLog.clear()
        try {
          logFeedUpdate("--> Test Thread waiting on job queue");
          activeRunContext = TestEngineController.jobQueue.take();
          SessionMap.registerObject(activeRunContext.testRun.runner.email, "signalRegistry", new MinderSignalRegistry());

          jobFeedUpdate()
          queueFeedUpdate()
          logFeedUpdate("--> Job with id [" + activeRunContext.testRun.job.id + "] arrived. Start in 5 seconds...");
          Thread.sleep(5000);
          activeRunContext.updateNumber()
          logFeedUpdate("--> Run Job #[" + activeRunContext.testRun.number + "]")
          activeRunContext.run()
        } catch {
          case inter: InterruptedException => {
            //someone interrupted me.
            //check the exit flag and go back.
            logFeedUpdate("<-- Job Interrupted")
          }
          case t: Throwable => {
            logFeedUpdate("<-- Error [" + t.getMessage + "]")
          }
        } finally {
          logFeedUpdate(">>> Test Run  #[" + activeRunContext.testRun.number + "] finished.")
          activeRunContext = null
          queueFeedUpdate()
          jobFeedUpdate()
        }
      }
    }
  }

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
    println("Job Status Feed")
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
    println("Job History Feed")
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
  def enqueueJob(id: Long) = Action {
    implicit request =>
      if (jobQueue.size >= 30) {
        BadRequest("The job queue is full.")
      } else {
        val job = Job.findById(id)
        if (job == null) {
          BadRequest("A job with id [" + id + "] was not found!")
        } else {
          val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
          val java_session = java_ctx.session()
          val user = Authentication.getLocalUser(java_session);
          jobQueue.offer(createTestRunContext(job, user))
          queueFeedUpdate();
          Ok;
        }
      }
  }

  /**
   * an action for enqueuing a new gitb job for test engine running
    * @return
    */
  def enqueueGitbJobWithUser(id: Long, user: User, replyToUrlAddress: String){
      if (jobQueue.size >= 30) {
        BadRequest("The job queue is full.")
      } else {
        val job = GitbJob.findById(id)
        if (job == null) {
          BadRequest("A job with id [" + id + "] was not found!")
        } else {
          var testRunContext = createTestRunContext(job, user);
          testRunContext.gitbReplyToUrlAddress = replyToUrlAddress
          jobQueue.offer(testRunContext)
          queueFeedUpdate();
        }
      }
  }
      
  /**
    * Update the queue feed with the current active job and queue status
    */
  def queueFeedUpdate(): Unit = {
    jobQueueChannel.push(jobQueueList.render().toString())
    jobHistoryChannel.push(jobHistoryList.render().toString())
  }

  def jobFeedUpdate(): Unit = {
    jobStatusChannel.push(testStatusMonitor.apply().toString());
  }

  def logFeedUpdate(log: String): Unit = {
    Logger.debug(log)
    currentLog.append(log)
    logChannel.push(log)
  }
  
  def gitbLogFeedUpdate(log: String, step: StepStatus, stepId: Long): Unit = {
    if (activeRunContext != null && activeRunContext.job != null && activeRunContext.job.isInstanceOf[GitbJob])
    {
      GitbTestbedController.performUpdateStatusOperation(activeRunContext.gitbReplyToUrlAddress, activeRunContext.job.id, step, stepId, log);
    }
  }

  /**
    * Utility method to create a test run
    * @return
    */

  def createTestRunContext(job: AbstractJob, user: User): TestRunContext = {
    Logger.debug("Create Run")
    val testRun = new TestRun()
    testRun.date = new Date()
    testRun.job = job;
    val userHistory = new UserHistory
    userHistory.email = user.email;
    userHistory.operationType = new TOperationType
    userHistory.operationType.name = OperationType.RUN_TEST_CASE
    userHistory.operationType.save()
    testRun.history = userHistory
    testRun.runner = user;
    new TestRunContext(testRun)
  }

  //
  def createDummyTestRun(i: Int): TestRun = {

    val tpl = Array((5, 6))

    for ((l, r) <- tpl) {

    }
    val tr = new TestRun()
    tr.date = new Date()
    tr.job = Job.findById(361L);
    tr.runner = User.findByEmail("myildiz83@gmail.com")
    tr;
  }

  def cancelJob(index: Int) = Action {
    implicit request =>
      if (index >= 0 && index < jobQueue.size()) {
        val arr = jobQueue.toArray

        val tr = arr(index).asInstanceOf[TestRunContext]
        jobQueue.remove(tr)
        queueFeedUpdate();
      }
      Ok
  }

  def cancelActiveJob() = Action {
    implicit request =>

      val runContext = activeRunContext; //take it before its null

      if (runContext != null) {
        // REMEMBER: if you need to convert scala session to java session
        val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
        val java_session = java_ctx.session()
        val user = Authentication.getLocalUser(java_session);
        //only allow root or the runner to do that
        if (user.email == "root@minder" || user.email == runContext.testRun.runner.email) {
          //cancel

          //now interrupt the thread.
          testThread.interrupt();

          Ok
        } else {
          BadRequest("Hey! <br/>Only root or the runner can cancel an active job!!!")
        }
      } else {
        Ok
      }
  }

  /**
   * an action for cancelling a given gitb job for test engine running
   * @return
   */
  def cancelGitbJob(id: Long, user: User) {
    val job = GitbJob.findById(id)
    if (job == null) {
      println("A job with id [" + id + "] was not found!")
      return
    }

    //now look at running gitb jobs
    if (activeRunContext != null && activeRunContext.job.id == id) {
      if (user.email == "root@minder" || user.email == activeRunContext.job.owner.email) {
        //now interrupt the thread.
        testThread.interrupt();  
        return
      }
      else
      {
        println(user.email + " not permitted for" + " job with id [" + id + "]")
        return
      } 
    }
    
    //now look at waiting gitb jobs
    var index = 0;
    val arr = jobQueue.toArray
    if(arr == null || arr.size == 0)
    {
      println("A waiting job with id [" + id + "] was not found!")
      return
    }
          
    for (index <- 0 to (arr.size - 1)) {
      val tr = arr(index).asInstanceOf[TestRunContext]
      if (user.email == "root@minder" || user.email == tr.testRun.runner.email) {
        if (tr.job.isInstanceOf[GitbJob] && tr.job.id == id) {
          jobQueue.remove(tr)
          queueFeedUpdate();
          return
        }
      }
    }
  }

}
