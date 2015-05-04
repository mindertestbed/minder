package minderengine

import java.util

import builtin.ReportGenerator
import controllers.TestRunner
import models.{TestAssertion, TestCase, TestGroup, User}
import mtdl._
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{EnhancedPatternLayout, AppenderSkeleton, Appender, PatternLayout}
import play.api.Logger

import scala.collection.JavaConversions._
import scala.io.Source

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

  class MyAppender(var sb: StringBuilder) extends AppenderSkeleton {
    override def append(event: LoggingEvent): Unit = {
      if (this.getLayout != null) {
        val formatted = this.getLayout.format(event);
        sb.append(formatted);
        Logger.debug(formatted)
      }
    }

    def close() {}

    def requiresLayout(): Boolean = {
      false;
    }
  }

  def runTest2(userEmail: String, clsMinderTDL: Class[MinderTdl], map: Map[String, String], testRunner: TestRunner): Unit = {
    val logBuilder = new StringBuilder
    val lgr: org.apache.log4j.Logger = org.apache.log4j.Logger.getLogger("test");
    val app = new MyAppender(logBuilder);
    app.setLayout(new EnhancedPatternLayout("%d{ISO8601}: %-5p - %m%n%throwable"));
    lgr.addAppender(app);

    lgr.info("Start Test")
    val report = ""
    val rg = new ReportGenerator {
      override def getCurrentTestUserInfo: UserDTO = {
        null
      }
    }
    rg.startTest()
    if (testRunner != null)
      testRunner.startTest()
    lgr.info("Initialize report params")
    rg.setReportTemplate(Source.fromInputStream(this.getClass.getResourceAsStream("/taReport.xml")).mkString.getBytes())
    val user = User.findByEmail(userEmail)
    rg.setReportAuthor(user.name, userEmail);
    val testCase: TestCase = testRunner.job.testCase
    val testAssertion = TestAssertion.findById(testCase.testAssertion.id)
    val testGroup = TestGroup.findById(testAssertion.testGroup.id)

    val set = new util.HashSet[String]()
    try {

      lgr.debug("Initialize test case")
      val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(map, java.lang.Boolean.TRUE).asInstanceOf[MinderTdl]


      if (testRunner != null) {
        testRunner.wrappers = minderTDL.wrapperDefs
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
        var rivetIndex = 0;
        for (rivet <- minderTDL.SlotDefs) {
          lgr.debug("---- " + "RUN RIVET " + rivetIndex)

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

            lgr.debug("Dequeue Signal:" + label + "." + signature)

            set.add(label)

            val signalData = me.dequeueSignal(label, signature)

            lgr.debug("Signal Obtained Signal: " + label + "." + signature)

            if (testRunner != null) {
              testRunner.signalEmitted(rivetIndex, signalIndex, signalData)
            }

            for (paramPipe <- rivet.signalPipeMap(tuple)) {
              //FIX for BUG-1 : added an if for -1 param
              if (paramPipe.in != -1) {
                convertParam(paramPipe.out, paramPipe.execute(signalData.args(paramPipe.in)))
                testRunner.slotParamSet(rivetIndex, paramPipe.out, args(paramPipe.out))
              }
            }

            signalIndex += 1
          }

          lgr.debug("Assign free vars")

          for (paramPipe <- rivet.freeVariablePipes) {
            convertParam(paramPipe.out, paramPipe.execute(null))
            testRunner.slotParamSet(rivetIndex, paramPipe.out, args(paramPipe.out))
          }

          if (testRunner != null && rivet.freeVariablePipes.size > 0) {
            val freeArgs = Array.ofDim[Object](rivet.freeVariablePipes.size);
            var k = 0;
            for (paramPipe <- rivet.freeVariablePipes) {
              freeArgs(k) = args(paramPipe.out)
              k += 1
            }
            val signalData2 = new SignalData(freeArgs);
            testRunner.signalEmitted(rivetIndex, signalIndex, signalData2)
          }


          lgr.debug("Slot ready for call " + rivet.slot.wrapperId + "." + rivet.slot.signature)

          if (testRunner != null) testRunner.slotSet(rivetIndex);
          rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)
          lgr.debug("Slot call finished sucessfully")

          if (testRunner != null) testRunner.rivetFinished(rivetIndex)
          lgr.debug("Rivet finished sucessfully")
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
        lgr.debug("Send finish message to all wrappers")
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

      if (testRunner != null) {
        rg.setTestDetails(testGroup.name, testAssertion, testCase.name, testRunner.job,
          set, logBuilder.toString())
        testRunner.addLog(logBuilder.toString(), rg.generateReport())
        testRunner.finished()
      }
    } catch {
      case t: Throwable => {
        Logger.error(t.getMessage, t);
        lgr.error(t.getMessage, t)
        if (testRunner != null) {
          rg.setTestDetails(testGroup.name, testAssertion, testCase.name, testRunner.job,
            set, logBuilder.toString())
          testRunner.addLog(logBuilder.toString(), rg.generateReport())

          testRunner.failed(t)
        } else {
          throw new RuntimeException(t)
        }
      }
    } finally {

    }

  }

  /**
   * Runs the given test case
   * Created by yerlibilgin on 07/12/14.
   *
   * @param userEmail the owner email if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTest(userEmail: String, name: String, tdl: String, wrapperMapping: (String, String)*): Unit = {
    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- wrapperMapping) {
        map2 += e
      }
      map2.toMap
    }

    runTest2(userEmail, compileTest(userEmail, name, tdl), map, null)
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
