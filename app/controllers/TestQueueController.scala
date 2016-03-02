package controllers

import java.util
import java.util.Date
import java.util.concurrent.LinkedBlockingQueue

import com.avaje.ebean.Ebean
import com.gitb.core.v1.StepStatus
import controllers.LogFeeder.LogRecord
import controllers.common.enumeration.OperationType
import minderengine.{MinderSignalRegistry, SessionMap}
import models._
import play.Logger
import play.api.mvc._
import rest.controllers.GitbTestbedController
import scala.collection.JavaConversions._

/**
  * Manages the job lifecycle.
  * Holds a job queue.
  */
object TestQueueController extends Controller {
  /**
    * The queue that holds test runs. When a job is started, a test run is created for it.
    * One can access all the information about a test run and a job here.
    */
  val jobQueue = new LinkedBlockingQueue[TestRunContext]();
  var activeRunContext: TestRunContext = null;

  /**
    * A reusable thread pool for server side events
    */
  val threadPool = java.util.concurrent.Executors.newFixedThreadPool(20);

  //a flag indicating the life of the thread.
  var goon = true;

  /**
    * The main test thread
    */
  val testThread = new Thread() {
    override def run(): Unit = {
      while (goon) {
        LogFeeder.clear()
        var run1: TestRun = null;
        try {
          LogFeeder.log("--> Test Thread waiting on job queue");
          activeRunContext = TestQueueController.jobQueue.take();
          activeRunContext.updateNumber()
          TestRunFeeder.jobQueueUpdate()
          run1 = activeRunContext.testRun
          SessionMap.registerObject(run1.runner.email, "signalRegistry", new MinderSignalRegistry());

          LogFeeder.log("--> Job with id [" + run1.job.id + "] arrived. Start");
          Thread.sleep(1000);
          LogFeeder.log("--> Run Job #[" + run1.number + "]")
          activeRunContext.run()
        } catch {
          case inter: InterruptedException => {
            //someone interrupted me.
            //check the exit flag and go back.
            activeRunContext.failed("Job Cancelled", inter)
            LogFeeder.log(LogRecord(run1, "<-- Job Interrupted"))
          }
          case t: Throwable => {
            LogFeeder.log(LogRecord(run1, "<-- Error [" + t.getMessage + "]"))
          }
        } finally {
          LogFeeder.log(">>> Test Run  #[" + run1.number + "] finished.")
          activeRunContext = null
          TestRunFeeder.jobQueueUpdate()
          Thread.sleep(1000)
          TestRunFeeder.jobHistoryUpdate(run1)
        }
      }
    }
  }

  testThread.start();


  /**
    * an action for enqueuing a new job for test engine running
    *
    * @return
    */
  def enqueueJob(id: Long, visibility: String) = Action {
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
          jobQueue.synchronized {
            jobQueue.offer(createTestRunContext(job, user, Visibility.valueOf(visibility)))
          }
          TestRunFeeder.jobQueueUpdate()
          Ok;
        }
      }
  }

  /**
    * an action for enqueuing a new gitb job for test engine running
    *
    * @return
    */
  def enqueueGitbJobWithUser(id: Long, user: User, replyToUrlAddress: String) {
    if (jobQueue.size >= 30) {
      BadRequest("The job queue is full.")
    } else {
      val job = GitbJob.findById(id)
      if (job == null) {
        BadRequest("A job with id [" + id + "] was not found!")
      } else {
        var testRunContext = createTestRunContext(job, user, Visibility.PROTECTED);
        testRunContext.gitbReplyToUrlAddress = replyToUrlAddress
        jobQueue.synchronized {
          jobQueue.offer(testRunContext)
        }
        TestRunFeeder.jobQueueUpdate()
      }
    }
  }


  def gitbLogFeedUpdate(log: String, step: StepStatus, stepId: Long): Unit = {
    if (activeRunContext != null && activeRunContext.job != null && activeRunContext.job.isInstanceOf[GitbJob]) {
      GitbTestbedController.performUpdateStatusOperation(activeRunContext.gitbReplyToUrlAddress, activeRunContext.job.id, step, stepId, log);
    }
  }

  /**
    * Utility method to create a test run
    *
    * @return
    */

  def createTestRunContext(job: AbstractJob, user: User, visibility: Visibility): TestRunContext = {
    Logger.info("Create Run with visibility " + visibility)
    val testRun = new TestRun()
    testRun.date = new Date()
    testRun.job = job;
    testRun.visibility = visibility;
    val userHistory = new UserHistory
    userHistory.email = user.email;
    userHistory.operationType = new TOperationType
    userHistory.operationType.name = OperationType.RUN_TEST_CASE
    userHistory.operationType.save()
    testRun.history = userHistory
    testRun.runner = user;
    new TestRunContext(testRun)
  }

  def createTestRunContext(job: AbstractJob, user: User, visibility: Visibility, suiteRun: SuiteRun): TestRunContext = {
    Logger.info("Create Run with visibility " + visibility)
    val testRun = new TestRun()
    testRun.date = new Date()
    testRun.job = job;
    testRun.visibility = visibility;
    val userHistory = new UserHistory
    userHistory.email = user.email;
    userHistory.operationType = new TOperationType
    userHistory.operationType.name = OperationType.RUN_TEST_CASE
    userHistory.operationType.save()
    testRun.history = userHistory
    testRun.runner = user;
    testRun.suiteRun = suiteRun
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
        jobQueue.synchronized {
          jobQueue.remove(tr)
        }
        TestRunFeeder.jobQueueUpdate()
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
    *
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
      else {
        println(user.email + " not permitted for" + " job with id [" + id + "]")
        return
      }
    }

    //now look at waiting gitb jobs
    var index = 0;
    val arr = jobQueue.toArray
    if (arr == null || arr.size == 0) {
      println("A waiting job with id [" + id + "] was not found!")
      return
    }

    for (index <- 0 to (arr.size - 1)) {
      val tr = arr(index).asInstanceOf[TestRunContext]
      if (user.email == "root@minder" || user.email == tr.testRun.runner.email) {
        if (tr.job.isInstanceOf[GitbJob] && tr.job.id == id) {
          jobQueue.synchronized {
            jobQueue.remove(tr)
          }
          TestRunFeeder.jobQueueUpdate()
          return
        }
      }
    }
  }


  /**
    * Enqueue all the jobs declared inside a test suite
    *
    * @return
    */
  def enqueueTestSuite(id: Long, visibility: String) = Action {
    implicit request => {
      if (jobQueue.size >= 30) {
        BadRequest("The job queue is full.")
      } else {
        try {
          Ebean.beginTransaction()
          val testSuite = TestSuite.findById(id);
          val suiteRun = new SuiteRun()
          suiteRun.date = new Date()
          suiteRun.number = SuiteRun.getMaxNumber + 1
          suiteRun.testSuite = testSuite
          suiteRun.visibility = Visibility.valueOf(visibility)
          suiteRun.testRuns = new util.ArrayList[TestRun]()
          suiteRun.runner = testSuite.owner;
          suiteRun.save()
          if (testSuite == null) {
            BadRequest("A test suite with id [" + id + "] was not found!")
          } else {
            val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
            val java_session = java_ctx.session()
            val user = Authentication.getLocalUser(java_session)
            jobQueue.synchronized {
              SuiteJob.getAllByTestSuite(testSuite).foreach(sj => {
                jobQueue.offer(createTestRunContext(sj, user, Visibility.valueOf(visibility), suiteRun))
              })
            }
            TestRunFeeder.jobQueueUpdate()
            Ebean.commitTransaction()
            Ok("");
          }
        } catch {
          case th: Throwable => {
            Logger.error(th.getMessage, th)
            val errMessage = if (th.getMessage == null) {
              "Unknown Error"
            } else {
              th.getMessage
            }
            BadRequest("A problem occurred [" + errMessage + "]")
          }
        } finally {
          Ebean.endTransaction()
        }
      }
    }
  }
}
