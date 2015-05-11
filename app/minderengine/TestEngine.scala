package minderengine

import java.util

import models.TestCase
import mtdl._
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{AppenderSkeleton, EnhancedPatternLayout}
import play.api.Logger

import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TestEngine {

  def compileTest(userEmail: String, name: String, tdl: String): Class[MinderTdl] = {
    TdlCompiler.compileTdl(userEmail, name, tdl)
  }

  def describe(clsMinderTDL: Class[MinderTdl]): util.List[Rivet] = {
    val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(null, java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    minderTDL.SlotDefs
  }

  class MyAppender(testProcessWatcher: TestProcessWatcher) extends AppenderSkeleton {
    override def append(event: LoggingEvent): Unit = {
      if (this.getLayout != null) {
        val formatted = this.getLayout.format(event);
        testProcessWatcher.addLog(formatted);
        Logger.debug(formatted)
      }
    }

    def close() {}

    def requiresLayout(): Boolean = {
      false;
    }
  }

  /**
   * Runs the provided already compiled tdl class with the given parameter mapping
   * @param userEmail
   * @param clsMinderTDL
   * @param map
   * @param testProcessWatcher
   */
  def runTest(userEmail: String, clsMinderTDL: Class[MinderTdl], map: Map[String, String], testProcessWatcher: TestProcessWatcher): Unit = {
    val logBuilder = new StringBuilder
    val lgr: org.apache.log4j.Logger = org.apache.log4j.Logger.getLogger("test");
    val app = new MyAppender(testProcessWatcher);
    app.setLayout(new EnhancedPatternLayout("|TEST ENGINE| %d{ISO8601}: %-5p - %m%n%throwable"));
    lgr.addAppender(app);

    lgr.info("Start Test")

    val set = new util.HashSet[String]()
    try {
      lgr.debug("Initialize test case")
      val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(map, java.lang.Boolean.TRUE).asInstanceOf[MinderTdl]
      minderTDL.debug = (any: Any) => {lgr.debug(any)}
      minderTDL.debugThrowable = (any: Any, th: Throwable) => {lgr.debug(any, th)}
      minderTDL.info = (any: Any) => {lgr.info(any)}
      minderTDL.infoThrowable = (any: Any, th: Throwable) => {lgr.info(any, th)}
      minderTDL.error = (any: Any) => {lgr.error(any)}
      minderTDL.errorThrowable = (any: Any, th: Throwable) => {lgr.error(any, th)}

      testProcessWatcher.updateWrappers(minderTDL.wrapperDefs.toSet)

      //first, call the start methods for all registered wrappers of this test.

      for (wrapperName <- minderTDL.wrapperDefs) {
        val minderClient = if (BuiltInWrapperRegistry.get().contains(wrapperName)) {
          BuiltInWrapperRegistry.get().getWrapper(wrapperName)
        } else {
          XoolaServer.get().getClient(wrapperName)
        }

        lgr.debug(">>>> CALL START TEST ON [" + wrapperName + "]");
        minderClient.startTest(userEmail)
        lgr.debug("<<<< START TEST ON [" + wrapperName + "] FINISHED");
      }

      try {
        var rivetIndex = 0;
        for (rivet <- minderTDL.SlotDefs) {
          lgr.debug(">>>> " + "RUN RIVET " + rivetIndex)

          //resolve the minder client id. This might as well be resolved to a local built-in wrapper or the null slot.
          val minderClient =
            if (rivet.slot.wrapperId == "NULLWRAPPER") {
              new IMinderClient {
                override def callSlot(s: String, s1: String, objects: Array[AnyRef]): AnyRef = {
                  null
                }

                override def finishTest(): Unit = {}

                override def startTest(s: String): Unit = {}
              }

            } else if (BuiltInWrapperRegistry.get().containsWrapper(rivet.slot.wrapperId)) {
              BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
            } else {
              XoolaServer.get().getClient(rivet.slot.wrapperId)
            }

          val args = Array.ofDim[Object](rivet.pipes.length)

          set.add(rivet.slot.wrapperId)
          lgr.debug("ARG LEN:" + rivet.pipes.length)

          var signalIndex = 0
          for (tuple@(label, signature) <- rivet.signalPipeMap.keySet) {
            val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
            if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

            lgr.debug(">>>>>>>> Dequeue Signal:" + label + "." + signature)

            set.add(label)

            val signalData = me.dequeueSignal(label, signature)

            lgr.debug("<<<<<<<< Signal Obtained Signal: " + label + "." + signature)

            testProcessWatcher.signalEmitted(rivetIndex, signalIndex, signalData)

            for (paramPipe <- rivet.signalPipeMap(tuple)) {
              //FIX for BUG-1 : added an if for -1 param
              if (paramPipe.in != -1) {
                convertParam(paramPipe.out, paramPipe.execute(signalData.args(paramPipe.in)))
              }
            }

            signalIndex += 1
          }

          lgr.debug(">>>>>>>> Assign free vars")

          for (paramPipe <- rivet.freeVariablePipes) {
            convertParam(paramPipe.out, paramPipe.execute(null))
          }

          if (rivet.freeVariablePipes.size > 0) {
            val freeArgs = Array.ofDim[Object](rivet.freeVariablePipes.size);
            var k = 0;
            for (paramPipe <- rivet.freeVariablePipes) {
              freeArgs(k) = args(paramPipe.out)
              k += 1
            }
            val signalData2 = new SignalData(freeArgs);
            testProcessWatcher.signalEmitted(rivetIndex, signalIndex, signalData2)
          }

          lgr.debug("<<<<<<<< Free vars assigned")

          lgr.debug(">>>>>>>> CALL SLOT " + rivet.slot.wrapperId + "." + rivet.slot.signature)

          rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)
          lgr.debug("<<<<<<<< SLOT CALL FINISHED " + rivet.slot.wrapperId + "." + rivet.slot.signature)


          testProcessWatcher.rivetFinished(rivetIndex)
          lgr.debug("<<<< Rivet finished sucessfully")
          lgr.debug("----------\n")

          def convertParam(out: Int, arg: Any) {
            if (arg.isInstanceOf[Rivet]) {
              args(out) = arg.asInstanceOf[Rivet].result
            } else {
              args(out) = arg.asInstanceOf[AnyRef]
            }
          }

          rivetIndex += 1
        }
      } finally {
        testProcessWatcher.addLog(">>>> Send finish message to all wrappers")
        //make sure that we call finish test for all
        for (wrapperName <- minderTDL.wrapperDefs) {
          val minderClient = if (BuiltInWrapperRegistry.get().contains(wrapperName)) {
            BuiltInWrapperRegistry.get().getWrapper(wrapperName)
          } else {
            XoolaServer.get().getClient(wrapperName)
          }

          try {
            testProcessWatcher.addLog(">>>> Send finish message [" + wrapperName + "]")
            minderClient.finishTest()
          } catch {
            case _: Throwable => {}
          } finally{
            testProcessWatcher.addLog("<<<< Finish message sent [" + wrapperName + "]")
          }
        }
      }

      testProcessWatcher.addLog(logBuilder.toString())
      testProcessWatcher.finished()
    } catch {
      case t: Throwable => {
        lgr.error(t.getMessage, t)
        testProcessWatcher.failed(t)
      }
    } finally {
      lgr.removeAppender(app)
    }

  }

  /**
   * Runs the given test case as a tdl
   * Created by yerlibilgin on 07/12/14.
   *
   * @param userEmail the owner email if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTdl(userEmail: String, name: String, tdl: String, wrapperMapping: (String, String)*): Unit = {
    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- wrapperMapping) {
        map2 += e
      }
      map2.toMap
    }

    runTest(userEmail, compileTest(userEmail, name, tdl), map, null)
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
    val minderClass = TdlCompiler.compileTdl(email, testCase.name, tdlStr = testCase.tdl)
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


trait TestProcessWatcher {
  def updateWrappers(set: Set[String]): Unit

  def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit

  def rivetFinished(rivetIndex: Int): Unit

  def finished(): Unit

  def addLog(log: String): Unit

  def failed(t: Throwable): Unit
}
