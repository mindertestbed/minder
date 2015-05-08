package controllers

import java.util
import java.util.Date

import builtin.ReportGenerator
import controllers.common.enumeration.{TestStatus, OperationType}
import minderengine.{UserDTO, TestProcessWatcher, SignalData, TestEngine}
import models._
import mtdl.Rivet
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
  val cls = TestEngine.compileTest(user.email, testCase.name, testCase.tdl)
  var rivets: List[VisualRivet] = List();
  val logStringBuilder = new StringBuilder

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
    TestEngine.runTest(user.email, cls,
      variableWrapperMapping.toMap, TestRunContext.this)
  }

  var status: TestStatus = TestStatus.PENDING;

  /**
   * This callback comes from the engine so that we can create our status data structure and later update it.
   * @param slotDefs
   */
  def describe(slotDefs: util.List[Rivet]): Unit = {
    var index = 1;
    rivets = slotDefs.map { rivet =>

      println("describe rivet " + index)
      val vr = new VisualRivet(rivet, index)
      index = index + 1
      vr
    }.toList
  }

  def slotSet(rivetIndex: Int): Unit = {
    rivets(rivetIndex).slot.status = TestStatus.GOOD
  }

  def rivetFinished(rivetIndex: Int): Unit = {
    rivets(rivetIndex).status = TestStatus.GOOD
    Logger.info("Rivet " + rivetIndex + " finished")
  }

  def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit = {
    val sgn = rivets(rivetIndex).signals(signalIndex)
    sgn.status = TestStatus.GOOD
    if (signalData.args.length > 0) {
      val i = 0
      for (arg <- signalData.args) {
        sgn.params(i).value = arg
        sgn.params(i).status = TestStatus.GOOD
      }
    }
  }

  def slotParamSet(rivetIndex: Int, paramIndex: Int, value: Any): Unit = {
    val vl = rivets(rivetIndex).slot.params(paramIndex);
    vl.value = value;
    vl.status = TestStatus.GOOD
  }

  def finished() {
    status = TestStatus.GOOD
    updateRun()
  }

  override def failed(t: Throwable) {
    Logger.error(t.getMessage, t)
    status = TestStatus.BAD
    error = {
      var cause = t;
      var err: String = null;
      while (err == null && cause != null) {
        err = cause.getMessage;
        cause = cause.getCause
      }

      if (err == null) {
        err = "Unknown error";
      }
      err
    }
    updateRun()
  }

  def addLog(log: String): Unit = {
    Logger.debug(log)
    logStringBuilder.append(log)
  }

  def updateRun(): Unit = {
    Logger.debug("Update Run " + status)

    val log = logStringBuilder.toString()
    rg.setTestDetails(testGroup, testAssertion, testCase, job, wrappers, log)
    testRun.status = status
    testRun.history.systemOutputLog = log
    testRun.history.save();

    testRun.report = rg.generateReport();
    testRun.errorMessage = error;
    testRun.success = if (status == TestStatus.GOOD) true else false
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

  def startTest(): Unit = {
    this.status = TestStatus.RUNNING
  }

  override def updateWrappers(set: Set[String]): Unit = {
    wrappers = set;
  }

}

class VisualRivet(val rivet: Rivet, val index: Int) {
  /*
  rivet
  --> slot
      --> params
  --> signals
      --> signal
          --> params
   */

  val slot: VisualSS = new VisualSS(rivet.slot.wrapperId, rivet.slot.signature)

  val signals: List[VisualSS] = {

    val lst = collection.mutable.MutableList[VisualSS]()

    rivet.signalPipeMap.foreach {
      pipe =>
        lst += new VisualSS(pipe._1._1, pipe._1._2)
    }

    if (rivet.freeVariablePipes.length > 0) {
      lst += {
        val signature = new StringBuilder
        for (pipe <- rivet.freeVariablePipes) {
          signature.append(",?");
        }
        if (signature.charAt(0) == ',')
          signature.deleteCharAt(0)

        signature.insert(0, '(').append(')')

        new VisualSS("Custom Values", signature.toString)
      }
    }

    lst.toList
  }

  var status: TestStatus = TestStatus.PENDING;

  override def toString() = "Rivet " + index
}

/**
 * A counterpart of a Signal and/or a Slot that aids drawing
 */
class VisualSS(val label: String, val signature: String) {
  println("\t" + label + "." + signature)
  val params: List[VisualParam] = {
    var index = 1
    val sgn = signature.substring(signature.indexOf('(')).replaceAll("\\s|\\(|\\)", "")
    if (sgn.contains(',')) {
      sgn.split(",").map {
        parm =>
          val vp = new VisualParam(label, signature, parm, index)
          index = index + 1
          vp
      }.toList
    } else {
      if (sgn.length > 0)
        List(new VisualParam(label, signature, sgn, index))
      else
        List()
    }
  }

  var status: TestStatus = TestStatus.PENDING;

  override def toString(): String = label + "." + signature
}

/**
 * A visual wrapper for a param (in or out)
 */
class VisualParam(val label: String, val signature: String, val parm: String, val index: Int) {
  var value: Any = null;
  var status: TestStatus = TestStatus.PENDING;
  val asdf = PrescriptionLevel.Mandatory
  println("\t\tparam " + parm + "(index)")

  override def toString() = parm + " @" + index
}

object TestStatusDecorator {
  def cssClassForSlot(slot: VisualSS): String = {
    if (slot.status == TestStatus.PENDING) "pending"
    else if (slot.status == TestStatus.BAD) "slotBad"
    else "slotGood"
  }

  def cssClassForSignal(signal: VisualSS): String = {
    if (signal.status == TestStatus.PENDING) "pending"
    else if (signal.status == TestStatus.BAD) "signalBad"
    else "signalGood"
  }

  def cssClassForParam(param: VisualParam): String = {
    if (param.status == TestStatus.PENDING) "pending"
    else if (param.status == TestStatus.BAD) "childBad"
    else "childGood"
  }

  def cssClassForTest(testRun: TestRun): String = {
    testRun.status match {
      case TestStatus.PENDING => "pending"
      case TestStatus.RUNNING => "testRunning"
      case TestStatus.BAD => "testBad"
      case TestStatus.GOOD => "testGood"
    }
  }

  def cssClassForRivet(rivet: VisualRivet): String = {
    rivet.status match {
      case TestStatus.PENDING => "pending"
      case TestStatus.RUNNING => "testRunning"
      case TestStatus.BAD => "testBad"
      case TestStatus.GOOD => "testGood"
    }
  }

}

