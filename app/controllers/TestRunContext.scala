package controllers

import java.security.MessageDigest
import java.util
import java.util.Date

import builtin.ReportGenerator
import controllers.common.enumeration.{TestStatus, OperationType}
import minderengine.{UserDTO, TestProcessWatcher, SignalData, TestEngine}
import models._
import mtdl.{TdlCompiler, Rivet}
import play.Logger

import scala.collection.JavaConversions._
import scala.io.Source

/**
 * This class starts a test in a new actor and
 * provides information to the main actor (status)
 * Created by yerlibilgin on 13/01/15.
 */
class TestRunContext(val testRun: TestRun) extends Runnable with TestProcessWatcher {
  //prepare a mapping
  val variableWrapperMapping = collection.mutable.Map[String, String]();
  val mappedWrappers = MappedWrapper.findByJob(testRun.job)
  var wrappers: java.util.Set[String] = null;
  var error = ""
  val job = Job.findById(testRun.job.id);
  val user = testRun.runner;
  val testCase = TestCase.findById(job.testCase.id)
  val testAssertion = TestAssertion.findById(testCase.testAssertion.id)
  val testGroup = TestGroup.findById(testAssertion.testGroup.id)
  job.testCase = testCase

  val packageRoot = "_" + testGroup.id;
  val packagePath = packageRoot + "/_" + testCase.id;
  val cls = TdlCompiler.compileTdl(packageRoot, packagePath, testGroup.dependencyString,testCase.name, source = testCase.tdl)
  val logStringBuilder = new StringBuilder;
  val reportLogBuilder = new StringBuilder;
  var status = TestStatus.PENDING

  /**
   * Number of steps that will be calculated at the beginning for percentage
   * calculation.
   */
  var totalSteps = 1 //for divide by zero

  /**
   * Where we are?
   *
   * The progress will be calculated as: progress = currentStep * 100 / totalSteps
   */
  var currentStep = 0;

  /**
   * progress = currentStep * 100 / totalSteps
   */
  var progressPercent = 0;


  for (map: MappedWrapper <- mappedWrappers) {
    val parm = WrapperParam.findById(map.parameter.id)
    val wrp = Wrapper.findById(map.wrapper.id)
    Logger.debug(parm.name + "<--" + wrp.name)
    variableWrapperMapping += parm.name -> wrp.name
  }


  val rg = new ReportGenerator {
    override def getCurrentTestUserInfo: UserDTO = {
      null
    }
  }
  rg.startTest()
  rg.setReportTemplate(Source.fromInputStream(this.getClass.getResourceAsStream("/taReport.xml")).mkString.getBytes())
  rg.setReportAuthor(user.name, user.email);

  describe(TestEngine.describe(cls))

  override def run(): Unit = {
    status = TestStatus.RUNNING
    TestEngine.runTest(user.email, cls,
      variableWrapperMapping.toMap, TestRunContext.this)
  }

  /**
   * This callback comes from the engine so that we can create our status data structure and later update it.
   * @param slotDefs
   */
  def describe(slotDefs: util.List[Rivet]): Unit = {
    slotDefs.foreach { rivet =>
      if (rivet.freeVariablePipes.length > 0) {
        totalSteps += 1
      }

      totalSteps += rivet.signalPipeMap.size
    }
  }

  def stepUp(): Unit = {
    currentStep += 1
    progressPercent = currentStep * 100 / totalSteps
    if (progressPercent > 100)
      progressPercent = 100

    TestEngineController.queueFeedUpdate()
    TestEngineController.jobFeedUpdate()
  }

  override def rivetFinished(rivetIndex: Int): Unit = {
    stepUp()
    Logger.info("Rivet " + rivetIndex + " finished")
  }

  override def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit = {
    stepUp()
  }

  override def finished() {
    status = TestStatus.GOOD
    updateRun()
  }

  override def failed(err: String, t: Throwable) {
    status = TestStatus.BAD
    error = err
    Logger.error(error, t)
    updateRun()
    stepUp()
  }

  override def addLog(log: String): Unit = {
    logStringBuilder.append(log)
    TestEngineController.logFeedUpdate(log)
  }


  override def addReportLog(log: String): Unit = {
    reportLogBuilder.append(log)
    logStringBuilder.append(log)
    TestEngineController.logFeedUpdate(log)
  }

  def updateRun(): Unit = {
    rg.setTestDetails(testGroup, testAssertion, testCase, job, wrappers, reportLogBuilder.toString())
    testRun.history.systemOutputLog = logStringBuilder.toString()
    testRun.history.save();
    testRun.success = (status == TestStatus.GOOD)
    testRun.report = rg.generateReport();
    testRun.errorMessage = error;
    if (wrappers != null) {
      val sb = new StringBuilder()
      var i: Int = 1
      for (wrapper <- wrappers) {
        sb.append(i).append(". ").append(wrapper).append('\n')
        i += 1
      }
      testRun.wrappers = sb.toString();
    }

    testRun.save()
  }

  override def updateWrappers(set: Set[String]): Unit = {
    wrappers = set;
  }

  def updateNumber() = {
    testRun.number = TestRun.getMaxNumber() + 1
  }

}
