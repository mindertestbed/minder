package controllers

import java.util

import minderengine.{TestEngine, SignalData}
import models._
import mtdl.Rivet
import play.Logger

import scala.collection.JavaConversions._

/**
 * This class starts a test in a new actor and
 * provides information to the main actor (status)
 * Created by yerlibilgin on 13/01/15.
 */
class TestRunner(val runConfiguration: RunConfiguration, val user: User) {

  //prepare a mapping
  val variableWrapperMapping = collection.mutable.Map[String, String]();
  val mappedWrappers = MappedWrapper.findByRunConfiguration(runConfiguration)

  Logger.debug("Wrapper mapping")
  for (map: MappedWrapper <- mappedWrappers) {
    val parm = WrapperParam.findById(map.parameter.id)
    val wrp = Wrapper.findById(map.wrapper.id)
    Logger.debug(parm.name + "<--" + wrp.name)
    variableWrapperMapping += parm.name -> wrp.name
  }

  val testCase = TestCase.findById(runConfiguration.testCase.id)

  new Thread(new Runnable {
    override def run(): Unit = {
      println("USER.email " + user.email)
      println("runConfiguration " + runConfiguration.name)
      TestEngine.runTest2(user.email, testCase.tdl,
        variableWrapperMapping.toMap, describe, signalEmitted, slotParamSet,
        finished, failed, log)
    }
  }).start();

  var rivets: List[VisualRivet] = List();
  var status = TestStatus.PENDING;

  /**
   * This callback comes from the engine so that we can create our status data structure and later update it.
   * @param slotDefs
   */
  def describe(slotDefs: util.List[Rivet]): Unit = {
    rivets = slotDefs.map { rivet =>
      new VisualRivet(rivet)
    }.toList
  }

  def signalEmitted(label: String, signature: String, signalData: SignalData): Unit = {

  }

  def slotParamSet() {}

  def finished() {
    status = TestStatus.GOOD
  }


  var error = ""

  def failed(t: Throwable) {
    status = TestStatus.BAD
    error = {
      if (t.getMessage != null) t.getMessage
      else {
        "Unknown error";
      }
    }
  }

  def log() {}
}

class VisualRivet(rivet: Rivet) {
  /*
  rivet
  --> slot
      --> params
  --> signals
      --> signal
          --> params
   */

  println("RIVET")
  val slot: VisualSS = new VisualSS(rivet.slot.wrapperId, rivet.slot.signature)

  val signals: List[VisualSS] = {
    rivet.signalPipeMap.map {
      pipe =>
        new VisualSS(pipe._1._1, pipe._1._2)
    }.toList
  }
}

/**
 * A counterpart of a Signal and/or a Slot that aids drawing
 */
class VisualSS(val label: String, val signature: String) {
  println("\t" + label + "." + signature)
  val params: List[VisualParam] = {
    var index = 0
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

  var status: Int = 0;
}

/**
 * A visual wrapper for a param (in or out)
 */
class VisualParam(val label: String, val signature: String, val parm: String, val index: Int) {
  var value: Any = null;
  var status: Int = 0;
  println("\t\tparam " + parm + "(index)")

  override def toString() = label + "::" + signature + "::" + parm + "::" + index
}

object TestStatus {
  val PENDING = 0;
  val BAD = 1;
  val GOOD = 2
}

object TestStatusDecorator {
  /*
  parPending
parBad
parGood
   */
  def cssClassForSlot(slot: VisualSS): String = {
    if (slot.status == TestStatus.PENDING) "slotPending"
    else if (slot.status == TestStatus.BAD) "slotBad"
    else "slotGood"
  }

  def cssClassForSignal(signal: VisualSS): String = {
    if (signal.status == TestStatus.PENDING) "signalPending"
    else if (signal.status == TestStatus.BAD) "signalBad"
    else "signalGood"
  }

  def cssClassForParam(param: VisualParam): String = {
    if (param.status == TestStatus.PENDING) "childPending"
    else if (param.status == TestStatus.BAD) "childBad"
    else "childGood"
  }

}

