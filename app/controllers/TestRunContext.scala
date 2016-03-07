package controllers

import java.util

import builtin.ReportGenerator
import controllers.TestLogFeeder.LogRecord
import controllers.common.enumeration.TestStatus
import minderengine._
import models.Wrapper
import models._
import mtdl.{Rivet, TdlCompiler}
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
  val variableWrapperMapping = collection.mutable.Map[String, MappedWrapper]();
  val mappedWrappers = MappedWrapper.findByJob(testRun.job)
  var sutNames: java.util.Set[String] = null;
  var error = ""
  var job: AbstractJob = AbstractJob.findById(testRun.job.id)
  val user = testRun.runner;
  val tdl = Tdl.findById(job.tdl.id);
  val testCase = TestCase.findById(tdl.testCase.id)
  val testAssertion = TestAssertion.findById(testCase.testAssertion.id)
  val testGroup = TestGroup.findById(testAssertion.testGroup.id)
  job.tdl = tdl;
  tdl.testCase = testCase;

  val packageRoot = "_" + testGroup.id;
  val packagePath = packageRoot + "/_" + testCase.id;
  val cls = TdlCompiler.compileTdl(packageRoot, packagePath, testGroup.dependencyString, testCase.name, source = tdl.tdl, version = tdl.version);
  val logStringBuilder = new StringBuilder;
  val reportLogBuilder = new StringBuilder;
  var status = TestStatus.PENDING

  var sessionID: String = null;

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

  var gitbReplyToUrlAddress = "";

  for (mappedWrapper: MappedWrapper <- mappedWrappers) {
    mappedWrapper.parameter = WrapperParam.findById(mappedWrapper.parameter.id);
    mappedWrapper.wrapperVersion = WrapperVersion.findById(mappedWrapper.wrapperVersion.id);
    mappedWrapper.wrapperVersion.wrapper = Wrapper.findById(mappedWrapper.wrapperVersion.wrapper.id);
    variableWrapperMapping.put(mappedWrapper.parameter.name, mappedWrapper)
  }


  val rg = new ReportGenerator {
    override def getCurrentTestUserInfo: UserDTO = null

    override def getSUTIdentifiers: SUTIdentifiers = null
  }

  rg.startTest()
  rg.setReportTemplate(Source.fromInputStream(this.getClass.getResourceAsStream("/taReport.xml")).mkString.getBytes())
  rg.setReportAuthor(user.name, user.email);

  describe(TestEngine.describe(cls))

  override def run(): Unit = {
    status = TestStatus.RUNNING
    TestEngine.runTest(sessionID,user.email, cls, variableWrapperMapping, TestRunContext.this, job.mtdlParameters)
  }

  /**
    * This callback comes from the engine so that we can create our status data structure and later update it.
    *
    * @param slotDefs
    */
  def describe(slotDefs: util.List[Rivet]): Unit = {
    totalSteps = slotDefs.size() * 2; //one rivet call, one rivet finished
  }

  def stepUp(): Unit = {
    currentStep += 1
    progressPercent = currentStep * 100 / totalSteps
    if (progressPercent > 100)
      progressPercent = 100

    TestRunFeeder.testProgressUpdate(progressPercent)
  }

  override def rivetFinished(rivetIndex: Int): Unit = {
    stepUp()
    Logger.info("Rivet " + rivetIndex + " finished")
  }

  override def rivetInvoked(rivetIndex: Int): Unit = {
    stepUp()
    Logger.info("Rivet " + rivetIndex + " finished")
  }

  override def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit = {
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
  }

  override def addLog(log: String): Unit = {
    logStringBuilder.append(log)
    TestLogFeeder.log(LogRecord(testRun, log))
  }


  override def addReportLog(log: String): Unit = {
    reportLogBuilder.append(log)
    logStringBuilder.append(log)
    TestLogFeeder.log(LogRecord(testRun, log))
  }

  def updateRun(): Unit = {
    rg.setTestDetails(testGroup, testAssertion, testCase, job, sutNames, reportLogBuilder.toString())
    testRun.history.setSystemOutputLog(logStringBuilder.toString());
    testRun.history.save();
    testRun.success = (status == TestStatus.GOOD)
    testRun.report = rg.generateReport();
    testRun.errorMessage = error.getBytes("utf-8");
    if (sutNames != null) {
      val sb = new StringBuilder()
      var i: Int = 1
      for (sut <- sutNames) {
        sb.append(i).append("- ").append(sut).append('\n')
        i += 1
      }
      testRun.sutNames = sb.toString();
    }

    testRun.save()
  }

  override def updateSUTNames(set: scala.collection.Set[String]): Unit = {
    sutNames = set;
  }

  def updateNumber() = {
    testRun.number = TestRun.getMaxNumber() + 1
  }

}
