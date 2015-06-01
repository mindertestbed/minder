package minderengine

import java.util

import models.{TestGroup, TestAssertion, TestCase}
import mtdl._
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{Level, AppenderSkeleton, EnhancedPatternLayout}
import play.api.Logger

import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TestEngine {
  def describe(clsMinderTDL: Class[MinderTdl]): util.List[Rivet] = {
    val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(null, java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    minderTDL.SlotDefs
  }

  class MyAppender(testProcessWatcher: TestProcessWatcher) extends AppenderSkeleton {
    override def append(event: LoggingEvent): Unit = {
      if (this.getLayout != null) {
        val formatted = this.getLayout.format(event);

        if (event.getLevel.toInt == Level.INFO.toInt || event.getLevel.toInt == Level.ERROR.toInt){
          testProcessWatcher.addReportLog(formatted);
        } else {
          testProcessWatcher.addLog(formatted);
        }
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
    val lgr: org.apache.log4j.Logger = org.apache.log4j.Logger.getLogger("test");
    val app = new MyAppender(testProcessWatcher);
    app.setLayout(new EnhancedPatternLayout("%d{ISO8601}: %-5p - %m%n%throwable"));
    lgr.addAppender(app);

    lgr.info("Start Test")

    val set = new util.HashSet[String]()
    try {
      lgr.info("Initialize test case")
      val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(map, java.lang.Boolean.TRUE).asInstanceOf[MinderTdl]
      minderTDL.debug = (any: Any) => {lgr.debug(any)}
      minderTDL.debugThrowable = (any: Any, th: Throwable) => {lgr.debug(any, th)}
      minderTDL.info = (any: Any) => {lgr.info(any)}
      minderTDL.infoThrowable = (any: Any, th: Throwable) => {lgr.info(any, th)}
      minderTDL.error = (any: Any) => {lgr.error(any)}
      minderTDL.errorThrowable = (any: Any, th: Throwable) => {lgr.error(any, th)}

      testProcessWatcher.updateWrappers(minderTDL.wrapperDefs.toSet)

      //first, call the start methods for all registered wrappers of this test.

      lgr.info("> CALL START TEST ON Wrappers");
      for (wrapperName <- minderTDL.wrapperDefs) {
        val minderClient = if (BuiltInWrapperRegistry.get().contains(wrapperName)) {
          BuiltInWrapperRegistry.get().getWrapper(wrapperName)
        } else {
          XoolaServer.get().getClient(wrapperName)
        }
        minderClient.startTest(userEmail)
      }

      try {
        var rivetIndex = 0;
        for (rivet <- minderTDL.SlotDefs) {
          lgr.info("> " + "RUN RIVET " + rivetIndex)

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
          var signalIndex = 0
          for (tuple@(label, signature) <- rivet.signalPipeMap.keySet) {
            val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
            if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

            //obtain the source signal object
            val signalList = rivet.signalPipeMap(tuple);
            if (signalList == null || signalList.isEmpty)
              throw new IllegalArgumentException("singal list is empty for " + label + "." + signature);

            val signal = signalList(0).inRef.source;

            lgr.debug("> Wait For Signal:" + label + "." + signature)

            set.add(label)

            val signalData = me.dequeueSignal(label, signature, signal.timeout)

            lgr.debug("< Signal Arrived: " + label + "." + signature)

            testProcessWatcher.signalEmitted(rivetIndex, signalIndex, signalData)

            for (paramPipe <- rivet.signalPipeMap(tuple)) {
              //FIX for BUG-1 : added an if for -1 param
              if (paramPipe.in != -1) {
                convertParam(paramPipe.out, paramPipe.execute(signalData.args(paramPipe.in)))
              }
            }

            signalIndex += 1
          }

          lgr.debug("Assign free vars")

          for (paramPipe <- rivet.freeVariablePipes) {
            val any = paramPipe.execute(null)
            convertParam(paramPipe.out, any)
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

          lgr.info("> CALL SLOT " + rivet.slot.wrapperId + "." + rivet.slot.signature)
          rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)
          lgr.info("< SLOT CALLED " + rivet.slot.wrapperId + "." + rivet.slot.signature)


          testProcessWatcher.rivetFinished(rivetIndex)
          lgr.info("< Rivet finished sucessfully")
          lgr.info("----------\n")

          def convertParam(out: Int, arg: Any) {
            if(arg == null){
              args(out) = null;
            } else if (arg.isInstanceOf[Rivet]) {
              args(out) = arg.asInstanceOf[Rivet].result
            } else if (arg.isInstanceOf[MinderNull]) {
              args(out) = null;
            } else {
              args(out) = arg.asInstanceOf[AnyRef]
            }
          }

          rivetIndex += 1
        }
      } finally {
        lgr.info("> Send finish message to all wrappers")
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
          } finally{
          }
        }
      }
      testProcessWatcher.finished()
    } catch {
      case t: Throwable => {

        val error = {
          var cause = t;
          var err: String = null;
          while (cause.getCause != null) {
            cause = cause.getCause
          }

          err = cause.getMessage

          if (err == null) {
            err = "Unknown error [" + t.getClass.getName + "]";
          }
          err
        }
        lgr.error(error);
        testProcessWatcher.failed(error, t);
      }
    } finally {
      lgr.info("Test Finished")
      lgr.removeAppender(app)
    }

 //   mapMe("A" -> "A".getBytes(), "B" -> "B".getBytes())

  }

  /**
   * Evaluates the whole test case without running it and creates a list of wrappers and their signals-slots.
   *
   * @param testCase
   * @return
   */
  def describeTdl(testCase: TestCase): util.LinkedHashMap[String, util.Set[SignalSlot]] = {
    testCase.testAssertion = TestAssertion.findById(testCase.testAssertion.id)
    testCase.testAssertion.testGroup = TestGroup.findById(testCase.testAssertion.testGroup.id)
    val root = "_" + testCase.testAssertion.testGroup.id;
    val packagePath = root + "/_" + testCase.id;
    val minderClass = TdlCompiler.compileTdl(root, packagePath, testCase.name, source = testCase.tdl)
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

  def addReportLog(s: String) : Unit

  def failed(message: String, t: Throwable): Unit
}
