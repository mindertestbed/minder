package controllers

import java.util
import java.util.Date

import controllers.common.enumeration.OperationType
import minderengine.{SignalData, TestEngine}
import models._
import mtdl.Rivet
import play.Logger

import scala.collection.JavaConversions._

/**
 * This class starts a test in a new actor and
 * provides information to the main actor (status)
 * Created by yerlibilgin on 13/01/15.
 */
class TestRunner(val job: Job, val user: User) {

  //prepare a mapping
  val variableWrapperMapping = collection.mutable.Map[String, String]();
  val mappedWrappers = MappedWrapper.findByJob(job)
  var log: String = ""
  var report: Array[Byte] = null
  var wrappers: scala.collection.mutable.Set[String] = null;
  var error = ""


  Logger.debug("Wrapper mapping")
  for (map: MappedWrapper <- mappedWrappers) {
    val parm = WrapperParam.findById(map.parameter.id)
    val wrp = Wrapper.findById(map.wrapper.id)
    Logger.debug(parm.name + "<--" + wrp.name)
    variableWrapperMapping += parm.name -> wrp.name
  }

  val testCase = TestCase.findById(job.testCase.id)
  job.testCase = testCase
  val cls = TestEngine.compileTest(user.email, testCase.tdl)
  var rivets: List[VisualRivet] = List();

  describe(TestEngine.describe(cls))

  new Thread(new Runnable {
    override def run(): Unit = {
      TestEngine.runTest2(user.email, cls,
        variableWrapperMapping.toMap, TestRunner.this)
    }
  }).start();

  var status: Int = TestStatus.PENDING;

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
    println("Rivet " + rivetIndex + " finished")
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
    createRun()
  }

  def failed(t: Throwable) {
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
    createRun()
  }

  def addLog(log: String, report: Array[Byte]): Unit = {
    Logger.debug("Add Log")
    Logger.debug(log)
    this.log = log
    this.report = report;
  }

  var testRun: TestRun = null;

  def createRun(): Unit = {
    Logger.debug("Create Run")
    Logger.debug(log)
    testRun = new TestRun()
    testRun.date = new Date()
    testRun.job = job;
    val userHistory = new UserHistory
    userHistory.user = user;
    userHistory.operationType = new TOperationType
    userHistory.operationType.name = OperationType.RUN_TEST_CASE
    userHistory.operationType.save()
    userHistory.systemOutputLog = log
    userHistory.save()
    testRun.history = userHistory
    testRun.report = report;
    testRun.runner = user;
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

  var status: Int = TestStatus.PENDING;

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

  var status: Int = TestStatus.PENDING;

  override def toString(): String = label + "." + signature
}

/**
 * A visual wrapper for a param (in or out)
 */
class VisualParam(val label: String, val signature: String, val parm: String, val index: Int) {
  var value: Any = null;
  var status: Int = TestStatus.PENDING;
  println("\t\tparam " + parm + "(index)")

  override def toString() = parm + " @" + index
}

object TestStatus {
  val PENDING = 0
  val RUNNING = 1
  val BAD = 2
  val GOOD = 3

  def valueOf(status: Int): String = {
    status match {
      case PENDING => "Pending"
      case RUNNING => "Running"
      case BAD => "Failed"
      case GOOD => "Successful"
    }
  }
}

object TestStatusDecorator {
  /*
  parPending
parBad
parGood
   */
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

  def cssClassForTest(testRunner: TestRunner): String = {
    testRunner.status match {
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

