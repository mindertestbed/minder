package controllers

import java.util
import java.util.Date
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.{Inject, Provider, Singleton}

import com.avaje.ebean.Ebean
import com.gitb.core.v1.StepStatus
import controllers.common.enumeration.OperationType
import minderengine._
import models._
import org.slf4j.LoggerFactory
import play.api.mvc._
import rest.controllers.GitbTestbedController

import scala.collection.JavaConversions._

/**
  * Manages the job lifecycle.
  * Holds a job queue.
  */
@Singleton
class TestQueueController @Inject()(implicit testLogFeeder: Provider[TestLogFeeder],
                                    testRunFeeder: Provider[TestRunFeeder],
                                    gitbTestbedController: Provider[GitbTestbedController],
                                    testEngine: Provider[TestEngine],
                                    ePQueueManager: Provider[EPQueueManager]) extends Controller {

  val  LOGGER  = LoggerFactory.getLogger(classOf[TestQueueController])

  /**
    * The queue that holds test runs. When a job is started, a test run is created for it.
    * One can access all the information about a test run and a job here.
    */
  val jobQueue = new LinkedBlockingQueue[TestRunContext]();
  var activeRunContext: TestRunContext = null;

  //a flag indicating the life of the thread.
  var goon = true;

  val MAX_QUE_SIZE = 100
  /**
    * The main test thread
    */
  var testThread = createTestThread();
  testThread.start();

  /**
    * an action for enqueuing a new job for test engine running
    *
    * @return
    */
  def enqueueJob(id: Long, visibility: String) = Action {
    implicit request =>
      if (jobQueue.size >= MAX_QUE_SIZE) {
        BadRequest("The job queue is full.")
      } else {
        val job = Job.findById(id)
        if (job == null) {
          BadRequest("A job with id [" + id + "] was not found!")
        } else {
          val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
          val java_session = java_ctx.session()
          val user = Authentication.getLocalUser(java_session);
          enqueueJobDirect(job, user, visibility)
          Ok;
        }
      }
  }

  def enqueueJobDirect(job: AbstractJob, user: User, visibility: String) = {
    jobQueue.synchronized {
      jobQueue.offer(createTestRunContext(job, user, Visibility.valueOf(visibility)))
    }
    testRunFeeder.get().jobQueueUpdate()
  }

  /**
    * an action for enqueuing a new gitb job for test engine running
    *
    * @return
    */
  def enqueueJobWithUser(id: Long, user: User, replyToUrlAddress: String, visibility: Visibility): TestSession = {
    if (jobQueue.size >= MAX_QUE_SIZE) {
      throw new IllegalStateException("Test queue is full")
    } else {
      val job = AbstractJob.findById(id)
      if (job == null) {
        throw new IllegalStateException("A job with id [" + id + "] was not found!")
      } else {
        val testRunContext = createTestRunContext(job, user, visibility)
        testRunContext.gitbReplyToUrlAddress = replyToUrlAddress
        jobQueue.synchronized {
          jobQueue.offer(testRunContext)
        }
        testRunFeeder.get().jobQueueUpdate()
        val ts = new TestSession()
        ts.setSession(testRunContext.sessionID)
        return ts;
      }
    }
  }

  def gitbLogFeedUpdate(log: String, step: StepStatus, stepId: Long): Unit = {
    if (activeRunContext != null && activeRunContext.job != null && activeRunContext.job.isInstanceOf[GitbJob]) {
      gitbTestbedController.get().performUpdateStatusOperation(activeRunContext.gitbReplyToUrlAddress, activeRunContext.job.id, step, stepId, log)
    }
  }

  /**
    * Utility method to create a test run
    *
    * @return
    */

  def createTestRunContext(job: AbstractJob, user: User, visibility: Visibility): TestRunContext = {
    LOGGER.info("Create Run with visibility " + visibility)
    val testRun = new TestRun()
    testRun.job = job
    testRun.visibility = visibility
    val userHistory = new UserHistory
    userHistory.email = user.email;
    userHistory.operationType = OperationType.RUN_TEST_CASE
    testRun.history = userHistory
    testRun.runner = user;
    testRun.status = TestRunStatus.PENDING;
    //testRun.history.save();
    //testRun.save();
    LOGGER.info(s"Testrun created, ID: ${testRun.number}")
    new TestRunContext(testRun, testRunFeeder.get(), testLogFeeder.get(), testEngine.get())
  }


  def createTestRunContext(job: AbstractJob, user: User, visibility: Visibility, suiteRun: SuiteRun): TestRunContext = {
    LOGGER.info("Create Run with visibility " + visibility)
    val testRun = new TestRun()
    testRun.job = job
    testRun.visibility = visibility
    val userHistory = new UserHistory
    userHistory.email = user.email
    userHistory.operationType = OperationType.RUN_TEST_CASE
    testRun.history = userHistory
    testRun.runner = user
    testRun.suiteRun = suiteRun
    testRun.status = TestRunStatus.PENDING;
    //testRun.history.save();
    //testRun.save();
    LOGGER.info(s"Testrun created, ID: ${testRun.number}")
    new TestRunContext(testRun, testRunFeeder.get(), testLogFeeder.get(), testEngine.get())
  }

  def cancelJob(index: Int) = Action {
    implicit request =>
      if (index >= 0 && index < jobQueue.size()) {
        val arr = jobQueue.toArray

        val testRunContext = arr(index).asInstanceOf[TestRunContext]
        jobQueue.synchronized {
          jobQueue.remove(testRunContext)
        }
        //try to remove it from suspension context
        SuspensionContext.get().remove(testRunContext.session)

        //try to remove it from the EQQueue
        ePQueueManager.get().removeTestContext(testRunContext)

        testRunFeeder.get().jobQueueUpdate()
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
        val user = Authentication.getLocalUser(java_session)
        //only allow root or the runner to do that
        if (user.email == "root@minder" || user.email == runContext.testRun.runner.email) {

          runContext.finalRunnable = new Runnable {
            def run(): Unit = {
              runContext.testRun.status = TestRunStatus.CANCELLED;
            }
          }

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
        activeRunContext.finalRunnable = new Runnable {
          def run(): Unit = {
            activeRunContext.testRun.status = TestRunStatus.CANCELLED;
          }
        }

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
          testRunFeeder.get().jobQueueUpdate()
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
  def enqueueTestSuite(id: Long, visibility: String, jobIdList: String) = Action {
    implicit request => {
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      val user = Authentication.getLocalUser(java_session)
      if (jobQueue.size >= MAX_QUE_SIZE) {
        BadRequest("The job queue is full.")
      } else {
        try {
          Ebean.beginTransaction()
          val testSuite = TestSuite.findById(id)
          val suiteRun = new SuiteRun()
          suiteRun.date = new Date()
          suiteRun.number = SuiteRun.getMaxNumber + 1
          suiteRun.testSuite = testSuite
          suiteRun.visibility = Visibility.valueOf(visibility)
          suiteRun.testRuns = new util.ArrayList[TestRun]()
          suiteRun.runner = testSuite.owner
          suiteRun.save()
          if (testSuite == null) {
            BadRequest("A test suite with id [" + id + "] was not found!")
          } else {
            //we are in a transaction, create a list, commit and then enque
            //to avoid optimisticlockingexception.
            val list = new util.ArrayList[TestRunContext]()
            LOGGER.debug("Create and enqueue test runs for suite")
            jobQueue.synchronized {
              //if the job id list is empty, then enqueue all jobs
              if (jobIdList == null || jobIdList.isEmpty) {
                Job.getAllByTestSuite(testSuite).foreach(job => {
                  list.add(createTestRunContext(job, user, Visibility.valueOf(visibility), suiteRun))
                })
              } else {
                //split the job id list with respect to comma and enqueue all
                jobIdList.split(",").foreach(jobId => {
                  list.add(createTestRunContext(Job.findById(jobId.toLong), user,
                    Visibility.valueOf(visibility), suiteRun))
                })
              }
            }
            testRunFeeder.get().jobQueueUpdate()
            Ebean.commitTransaction()

            //after committing, now enque them
            list.foreach(trc=>jobQueue.offer(trc))

            LOGGER.debug(s"Done enqueing the jobs. JobQueue Size: ${jobQueue.size()}")
            Ok("")
          }
        } catch {
          case th: Throwable => {
            LOGGER.error(th.getMessage, th)
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

  def enqueueTestRunContext(testRunContext: TestRunContext): Unit = {
    jobQueue.offer(testRunContext)
    testRunFeeder.get().jobQueueUpdate()
  }

  def reset(): Unit = {
    testThread.interrupt();
    testThread.join(5000);
    jobQueue.clear();
    testRunFeeder.get().jobQueueUpdate()
    testThread = createTestThread();
    testThread.start();
  }

  def createTestThread(): Thread = {
    new Thread() {
      override def run(): Unit = {
        try {
          while (goon) {
            val maxNumber = TestRun.getMaxNumber;

            LOGGER.debug("MAX NUMBER " + maxNumber)
            testLogFeeder.get().clear()
            var run1: TestRun = null;
            try {
              testLogFeeder.get().log("--> Test Thread waiting on job queue");
              activeRunContext = jobQueue.take();
              testLogFeeder.get().log("--> New Job Taken");
              activeRunContext.init()
              testRunFeeder.get().jobQueueUpdate()
              run1 = activeRunContext.testRun

              val session: TestSession = new TestSession(activeRunContext.sessionID)

              if (!MinderSignalRegistry.get().hasSession(session))
                MinderSignalRegistry.get().initTestSession(session)

              testLogFeeder.get().log("--> Job with id [" + run1.job.id + "] arrived. Start");
              Thread.sleep(1000);
              testLogFeeder.get().log("--> Run Job #[" + run1.number + ":" + run1.id + "]")
              activeRunContext.run()
            } finally {
              activeRunContext = null
              Thread.sleep(1000)
              testRunFeeder.get().jobQueueUpdate()
              testRunFeeder.get().jobHistoryUpdate()
            }
          }
        } catch {
          case th: Throwable => LOGGER.error(th.getMessage, th);
        }
        finally {
          LOGGER.info("Test Thread exit")
        }
      }
    }
  }

  def jobQueueUpdate() = {
    testRunFeeder.get().jobQueueUpdate()
  }

}

