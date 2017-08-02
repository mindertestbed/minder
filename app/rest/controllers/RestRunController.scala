package rest.controllers

import java.util
import java.util.{Date, GregorianCalendar}
import javax.inject.Inject
import javax.xml.datatype.DatatypeFactory

import akka.util.ByteString
import com.avaje.ebean.Ebean
import controllers.TestQueueController
import minderengine.Visibility
import models._
import play.Logger
import play.api.mvc.Controller
import rest.models.runModel._
import utils.UserAction
import scala.collection.JavaConversions._

/**
  * @author yerlibilgin
  */

class RestRunController @Inject()(implicit testQueueController: TestQueueController) extends Controller {
  def runJob = UserAction(parse.raw) {
    implicit request => {
      val reqObj = request.requestObject.get.asInstanceOf[TestCaseRunRequest]
      Logger.debug(s"${request.username} called runJob with id ${reqObj.getJobId}")

      val user = User.findByEmail(request.username);

      val job = AbstractJob.findById(reqObj.getJobId)

      if (job == null) {
        BadRequest(ObjectUtil.newMinderError(reqObj.getJobId, MinderRestErrorCodes.E_INVALID_JOB_ID, "No such job", null))
      } else {
        val context = testQueueController.createTestRunContext(job, user, Visibility.PROTECTED);
        testQueueController.enqueueTestRunContext(context)

        val resp = new TestCaseRunResponse
        resp.setJobId(reqObj.getJobId);
        resp.setTestRunId(context.testRun.id);
        val bytes = ObjectUtil.marshalAsByteArray(resp)
        Ok(ByteString.fromArray(bytes))
      }
    }
  }


  def testRunStatus = UserAction(parse.raw) {
    implicit request => {
      val reqObj = request.requestObject.get.asInstanceOf[TestRunStatusRequest]
      Logger.debug(s"${request.username} called testRunStatus with id ${reqObj.getTestRunId}")

      //first check the active run context

      val (testRun, isActive) = locateTestRun(reqObj.getTestRunId)

      if (testRun == null) {
        BadRequest(ObjectUtil.newMinderError(reqObj.getTestRunId, MinderRestErrorCodes.E_INVALID_RUN_ID, "No such test run", null))
      } else {
        val resp: TestRunStatusResponse = createTestRunStatusResponse(testRun, isActive)
        val bytes = ObjectUtil.marshalAsByteArray(resp)
        Ok(ByteString.fromArray(bytes))
      }
    }
  }

  private def locateTestRun(testRunId: Long) = {
    if (testQueueController.activeRunContext != null && testRunId == testQueueController.activeRunContext.testRun.id) {
      (testQueueController.activeRunContext.testRun, true)
    } else {
      (TestRun.findById(testRunId), false)
    }
  }

  private def createTestRunStatusResponse(testRun: TestRun, isActive: Boolean) = {
    val resp = new TestRunStatusResponse
    resp.setTestRunId(testRun.id);
    resp.setLog(testRun.report)
    if (testRun.date != null) {
      val c = new GregorianCalendar()
      c.setTime(testRun.date)
      resp.setStartDate(DatatypeFactory.newInstance.newXMLGregorianCalendar(c));
    }
    resp.setStatus(RunStatus.mapFrom(testRun.status));
    resp
  }

  def runSuite = UserAction(parse.raw) {
    implicit request => {
      val reqObj = request.requestObject.get.asInstanceOf[SuiteRunRequest]
      Logger.debug(s"${request.username} called runSuite with id ${reqObj.getSuiteId}")
      val user = User.findByEmail(request.username);
      val testSuite = TestSuite.findById(reqObj.getSuiteId)

      if (testSuite == null) {
        BadRequest(ObjectUtil.newMinderError(reqObj.getSuiteId, MinderRestErrorCodes.E_INVALID_SUITE_ID, "No such test suite", null))
      } else {
        try {
          Ebean.beginTransaction()
          val suiteRun = new SuiteRun()
          suiteRun.date = new Date()
          suiteRun.number = SuiteRun.getMaxNumber + 1
          suiteRun.testSuite = testSuite
          suiteRun.visibility = Visibility.PROTECTED
          suiteRun.testRuns = new util.ArrayList[TestRun]()
          suiteRun.runner = testSuite.owner
          suiteRun.save()
          if (testSuite == null) {
            BadRequest("A test suite with id [" + testSuite + "] was not found!")
          } else {
            val testRuns = new util.ArrayList[Long]()
            testQueueController.jobQueue.synchronized {
              //if the job id list is empty, then enqueue all jobs
              if (reqObj.getJobs == null || reqObj.getJobs.isEmpty) {
                Job.getAllByTestSuite(testSuite).foreach(job => {
                  val ctx = testQueueController.createTestRunContext(job, user, Visibility.PROTECTED, suiteRun)
                  testQueueController.enqueueTestRunContext(ctx)
                  testRuns.add(ctx.testRun.id)
                })
              } else {
                //split the job id list with respect to comma and enqueue all
                reqObj.getJobs.foreach(jobId => {
                  val job = Job.findById(jobId)
                  val ctx = testQueueController.createTestRunContext(job, user, Visibility.PROTECTED, suiteRun)
                  testQueueController.enqueueTestRunContext(ctx)
                  testRuns.add(ctx.testRun.id)
                })
              }
            }

            Ebean.commitTransaction()

            val resp = new SuiteRunResponse
            resp.setSuiteId(reqObj.getSuiteId)
            resp.setSuiteRunId(suiteRun.id)
            val runs = resp.getTestRuns

            testRuns.foreach(rid => runs.add(rid))

            val bytes = ObjectUtil.marshalAsByteArray(resp)
            Ok(ByteString.fromArray(bytes))
          }
        } catch {
          case th: Throwable => {
            Logger.error(th.getMessage, th)
            val errMessage = if (th.getMessage == null) {
              "Unknown Error"
            } else {
              th.getMessage
            }
            BadRequest(ObjectUtil.newMinderError(reqObj.getSuiteId, MinderRestErrorCodes.E_RUN_SUITE, errMessage, null))
          }
        } finally {
          Ebean.endTransaction()
        }
      }
    }
  }

  def suiteRunStatus = UserAction(parse.raw) {
    implicit request =>
      val reqObj = request.requestObject.get.asInstanceOf[SuiteRunStatusRequest]
      Logger.debug(s"${request.username} called suiteRunStatus with id ${reqObj.getSuiteRunId}")
      val user = User.findByEmail(request.username);

      val suiteRun = SuiteRun.findById(reqObj.getSuiteRunId)

      if (suiteRun == null) {
        BadRequest(ObjectUtil.newMinderError(reqObj.getSuiteRunId, MinderRestErrorCodes.E_INVALID_SUITE_RUN_ID, "No such suite run", null))
      } else {
        val suiteRunStatusResponse = new SuiteRunStatusResponse
        suiteRunStatusResponse.setSuiteRunId(reqObj.getSuiteRunId)

        val testRuns = TestRun.findBySuiteRun(suiteRun)

        testRuns.foreach(testRunTmp => {
          val testRun = TestRun.findById(testRunTmp.id)
          val isActive = (testQueueController.activeRunContext != null && testRun.id == testQueueController.activeRunContext.testRun.id)
          if (isActive) //if active, then the number is not persisted, borrow  it
            testRun.number = testQueueController.activeRunContext.testRun.number;

          val resp: TestRunStatusResponse = createTestRunStatusResponse(testRun, isActive)
          suiteRunStatusResponse.getSuiteRunStates.add(resp)
        })
        val bytes = ObjectUtil.marshalAsByteArray(suiteRunStatusResponse)
        Ok(ByteString.fromArray(bytes))
      }
  }



  def hello = UserAction(parse.raw) {
    implicit request =>
      val reqObj = request.requestObject.get.asInstanceOf[SuiteRunStatusRequest]
      Logger.debug(s"${request.username} called suiteRunStatus with id ${reqObj.getSuiteRunId}")
      val user = User.findByEmail(request.username);

      Ok("Hi")
  }
}
