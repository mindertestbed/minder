package minderengine

import java.io.{PrintWriter, ByteArrayOutputStream, OutputStreamWriter, StringWriter}
import java.lang.reflect.InvocationTargetException
import java.util

import builtin.ReportGenerator
import controllers.{TestRunner, Application}
import models.{TestGroup, TestAssertion, User, TestCase}
import mtdl._
import play.Play
import play.api.Logger
import play.mvc.Http
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TestEngine {

  def compileTest(userEmail: String, tdl: String): Class[MinderTdl] = {
    TdlCompiler.compileTdl(userEmail, tdl)
  }

  def describe(clsMinderTDL: Class[MinderTdl]): util.List[Rivet] = {
    println("Describe")
    val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(null, java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    minderTDL.SlotDefs
  }

  def runTest2(userEmail: String, clsMinderTDL: Class[MinderTdl], map: Map[String, String], testRunner: TestRunner): Unit = {
    println("Run Test 2")
    val log = new StringBuilder
    val report = ""
    val rg = new ReportGenerator {
      override def getCurrentTestUserInfo: UserDTO = {
        null
      }
    }
    rg.startTest()
    if(testRunner!=null)
      testRunner.startTest()
    log.append("\nArrange report params")
    rg.setReportTemplate(Source.fromInputStream(this.getClass.getResourceAsStream("/taReport.xml")).mkString.getBytes())
    val user = User.findByEmail(userEmail)
    rg.setReportAuthor(user.name, userEmail);
    val testCase: TestCase = testRunner.runConfiguration.testCase
    val testAssertion = TestAssertion.findById(testCase.testAssertion.id)
    val testGroup = TestGroup.findById(testAssertion.testGroup.id)

    val set = new util.HashSet[String]()
    try {

      log.append("\nInitialize test case")
      val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(map, java.lang.Boolean.TRUE).asInstanceOf[MinderTdl]


      if(testRunner != null){
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
          Logger.debug("---- " + "RUN RIVET " + rivetIndex)
          log.append("\n---- " + "RUN RIVET " + rivetIndex)

          //resolve the minder client id. This might as well be resolved to a local built-in wrapper.
          val minderClient = if (BuiltInWrapperRegistry.get().containsWrapper(rivet.slot.wrapperId)) {
            BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
          } else {
            XoolaServer.get().getClient(rivet.slot.wrapperId)
          }
          val args = Array.ofDim[Object](rivet.pipes.length)

          set.add(rivet.slot.wrapperId)
          log.append("\nARG LEN:").append(rivet.pipes.length)

          var signalIndex = 0
          for (tuple@(label, signature) <- rivet.signalPipeMap.keySet) {
            val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
            if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

            println("\ndequeue " + (label + ":" + signature))
            log.append("\nDequeue Signal:").append(label).append(signature)

            set.add(label)

            val signalData = me.dequeueSignal(label, signature)

            log.append("\nSignal Obtained Signal:").append(label).append(signature)

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

          log.append("\nAssing free vars")

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


          log.append("\nSlot ready for call " + rivet.slot.wrapperId + "." + rivet.slot.signature)

          if (testRunner != null) testRunner.slotSet(rivetIndex);
          rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)
          log.append("\nSlot call finished sucessfully")

          if (testRunner != null) testRunner.rivetFinished(rivetIndex)
          log.append("\nRivet finished sucessfully")
          log.append("\n---------------------\n")

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
        log.append("\nSend finish message to all wrappers\n")
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
        rg.setTestDetails(testGroup.name, testAssertion, testCase.name, testRunner.runConfiguration,
          set, log.toString())
        testRunner.addLog(log.toString(), rg.generateReport())
        testRunner.finished()
      }
    } catch {
      case t: InvocationTargetException => {
        t.printStackTrace()
        val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
        val sw = new PrintWriter(new OutputStreamWriter(stream));
        t.printStackTrace(sw)
        sw.flush();
        sw.close();
        log.append("\n" + new String(stream.toByteArray))
        if (t.getCause != null)
          if (testRunner != null) {
            rg.setTestDetails(testGroup.name, testAssertion, testCase.name, testRunner.runConfiguration,
              set, log.toString())
            testRunner.addLog(log.toString(), rg.generateReport())

            testRunner.failed(t.getCause)
          } else {
            throw new RuntimeException(t)
          }
      }
      case t: Throwable => {
        val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
        val sw = new PrintWriter(new OutputStreamWriter(stream));
        t.printStackTrace(sw)
        sw.flush();
        sw.close();
        log.append("\n" + new String(stream.toByteArray))

        if (testRunner != null) {
          rg.setTestDetails(testGroup.name, testAssertion, testCase.name, testRunner.runConfiguration,
            set, log.toString())
          testRunner.addLog(log.toString(), rg.generateReport())

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
  def runTest(userEmail: String, tdl: String, wrapperMapping: (String, String)*): Unit = {
    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- wrapperMapping) {
        map2 += e
      }
      map2.toMap
    }

    runTest2(userEmail, compileTest(userEmail, tdl), map, null)
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
