package minderengine

import java.util

import controllers.Application
import models.TestCase
import mtdl._
import play.Play
import play.api.Logger
import play.mvc.Http
import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TestEngine {
  def runTest2(userEmail: String, tdl: String, map: Map[String, String],
               describe: (util.List[Rivet]) => Unit, signalEmitted: (String, String, SignalData) => Unit, slotParamSet: () => Unit,
               finished: () => Unit, failed: (Throwable) => Unit, log: () => Unit): Unit = {

    try {
      println("-===================")
      println(tdl)
      val clsMinderTDL = TdlCompiler.compileTdl(userEmail, tdl)

      val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(map, java.lang.Boolean.TRUE).asInstanceOf[MinderTdl]

      if (describe != null) {
        describe(minderTDL.SlotDefs)
      }
      //first, call the start methods for all registered wrappers of this test.

      for (wrapperName <- minderTDL.wrapperDefs) {
        val minderClient = if (BuiltInWrapperRegistry.get().contains(wrapperName)) {
          BuiltInWrapperRegistry.get().getWrapper(wrapperName)
        } else {
          XoolaServer.get().getClient(wrapperName)
        }
        minderClient.startTest(userEmail)
      }


      try {
        var i = 1;
        for (rivet <- minderTDL.SlotDefs) {
          Logger.debug("---- " + "RUN RIVET " + i)
          i = i + 1

          //resolve the minder client id. This might as well be resolved to a local built-in wrapper.
          val minderClient = if (BuiltInWrapperRegistry.get().containsWrapper(rivet.slot.wrapperId)) {
            BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
          } else {
            XoolaServer.get().getClient(rivet.slot.wrapperId)
          }
          val args = Array.ofDim[Object](rivet.pipes.length)

          for (tuple@(label, signature) <- rivet.signalPipeMap.keySet) {
            val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
            if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

            println("dequeue " + (label + ":" + signature))

            val signalData = me.dequeueSignal(label, signature)
            for (paramPipe <- rivet.signalPipeMap(tuple)) {
              //FIX for BUG-1 : added an if for -1 param
              if (paramPipe.in != -1) {
                convertParam(paramPipe.out, paramPipe.execute(signalData.args(paramPipe.in)))
              }
            }
          }

          for (paramPipe <- rivet.freeVariablePipes) {
            convertParam(paramPipe.out, paramPipe.execute(null))
          }

          rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)

          def convertParam(out: Int, arg: Any) {
            if (arg.isInstanceOf[Rivet]) {
              args(out) = arg.asInstanceOf[Rivet].result
            } else {
              args(out) = arg.asInstanceOf[AnyRef]
            }
          }
        }
      } finally {
        //make sure that we call finish test for all
        for (wrapperName <- minderTDL.wrapperDefs) {
          val minderClient = if (BuiltInWrapperRegistry.get().contains(wrapperName)) {
            BuiltInWrapperRegistry.get().getWrapper(wrapperName)
          } else {
            XoolaServer.get().getClient(wrapperName)
          }

          try {
            minderClient.finishTest()
          } catch {
            case _: Throwable => {}
          }
        }
      }
      if (finished != null) {
        finished()
      }
    } catch {
      case t: Throwable => {
        if (failed != null) {
          failed(t)
        } else {
          throw new RuntimeException(t)
        }
      }
    }

  }

  /**
   * Runs the given test case
   * Created by yerlibilgin on 07/12/14.
   *
   * @param userEmail the owner email if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTest(userEmail: String, tdl: String, wrapperMapping: (String, String)*): Unit = {
    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- wrapperMapping) {
        map2 += e
      }
      map2.toMap
    }

    runTest2(userEmail, tdl, map, null, null, null, null, null, null)
  }

  /**
   * Evaluates the whole test case without running it and creates a list of wrappers and their signals-slots.
   *
   * @param testCase
   * @param email the email of the user used for package declaration
   * @return
   */
  def describeTdl(testCase: TestCase, email: String): util.LinkedHashMap[String, util.Set[SignalSlot]] = {
    Logger.debug("Describing: " + testCase.name + " for user " + email)
    Logger.debug(testCase.tdl)
    val minderClass = TdlCompiler.compileTdl(email, tdlStr = testCase.tdl)
    val minderTdl = minderClass.getConstructors()(0).newInstance(null, java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]

    val slotDefs = minderTdl.SlotDefs

    val hm: util.LinkedHashMap[String, util.Set[SignalSlot]] = new util.LinkedHashMap()
    val map: collection.mutable.LinkedHashMap[String, util.Set[SignalSlot]] = collection.mutable.LinkedHashMap()

    for (r <- slotDefs) {
      //check the slot
      val wrapperName = r.slot.wrapperId
      val slotSignature = r.slot.signature

      var set = map.getOrElseUpdate(wrapperName, new util.HashSet[SignalSlot]())
      set.add(new SlotImpl(wrapperId = wrapperName, signature = slotSignature))
      println(wrapperName + "::" + slotSignature)

      for (e@(k@(wn, ws), v) <- r.signalPipeMap) {
        println(wn + "::" + ws)
        set = map.getOrElseUpdate(wn, new util.HashSet[SignalSlot]())
        set.add(new SignalImpl(wn, ws))
      }
    }

    for ((k, v) <- map) {
      println("---------")
      println(k)
      for (ss <- v) {
        println("\t" + ss.signature)
      }
      hm.put(k, v)
    }

    hm
  }
}
