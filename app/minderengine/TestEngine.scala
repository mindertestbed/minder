package minderengine

import java.lang.reflect.InvocationTargetException
import java.util

import models._
import mtdl._
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{AppenderSkeleton, EnhancedPatternLayout, Level}

import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TestEngine {
  def describe(clsMinderTDL: Class[MinderTdl]): util.List[Rivet] = {
    val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(null, java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    minderTDL.RivetDefs
  }

  var minderTDL: MinderTdl = null

  /**
   * When Xoola server is initialized, it needs a class loader to deserialize the objects read from the network.
   * We will provide xoola the minderTDL.getClass.getClassLoader so that it successfully resolves its stuff.
   * This function will delegate the minderTDL.getClass.getClassLoader to xoola.
   */

  def getCurrentMTDLClassLoader(): ClassLoader = {
    if (minderTDL == null)
      Thread.currentThread().getContextClassLoader;
    else
      minderTDL.getClass.getClassLoader
  }

  class MyAppender(testProcessWatcher: TestProcessWatcher) extends AppenderSkeleton {
    override def append(event: LoggingEvent): Unit = {
      if (this.getLayout != null) {
        val formatted = this.getLayout.format(event);

        if (event.getLevel.toInt == Level.INFO.toInt || event.getLevel.toInt == Level.ERROR.toInt) {
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
   * @param wrapperMapping
   * @param testProcessWatcher
   */
  def runTest(userEmail: String, clsMinderTDL: Class[MinderTdl],
              wrapperMapping: collection.mutable.Map[String, MappedWrapper],
              testProcessWatcher: TestProcessWatcher, params: String): Unit = {
    val lgr: org.apache.log4j.Logger = org.apache.log4j.Logger.getLogger("test");
    val app = new MyAppender(testProcessWatcher);
    app.setLayout(new EnhancedPatternLayout("%d{ISO8601}: %-5p - %m%n%throwable"));
    lgr.addAppender(app);

    lgr.info("Start Test")

    try {
      lgr.info("Initialize test case")
      //mtdl does not know the minder model, it uses String->String
      val newMapping = wrapperMapping.map(entry => (entry._1,
        entry._2.wrapperVersion.wrapper.name + "|" + entry._2.wrapperVersion.version))
      //we need to map wrappers to versions as well
      //because the tdl calls the wrapper name and does not know the version
      //but we know what versio  of which wrapper is used here, and Xoola maps the remote id
      //to wrapper|version not only wrapper.

      val wrapperToVersionMap = wrapperMapping.map(entry => (entry._2.wrapperVersion.wrapper.name,
        entry._2.wrapperVersion.version))
      val const = clsMinderTDL.getConstructors()(0)
      minderTDL = const.newInstance(newMapping, java.lang.Boolean.TRUE).asInstanceOf[MinderTdl]
      minderTDL.debug = (any: Any) => {
        lgr.debug(any)
      }
      minderTDL.debugThrowable = (any: Any, th: Throwable) => {
        lgr.debug(any, th)
      }
      minderTDL.info = (any: Any) => {
        lgr.info(any)
      }
      minderTDL.infoThrowable = (any: Any, th: Throwable) => {
        lgr.info(any, th)
      }
      minderTDL.error = (any: Any) => {
        lgr.error(any)
      }
      minderTDL.errorThrowable = (any: Any, th: Throwable) => {
        lgr.error(any, th)
      }

      minderTDL.setParams(params);


      val mutableSutSet = new java.util.HashSet[String]

      //first, call the start methods for all registered wrappers of this test.

      lgr.info("> CALL START TEST ON Wrappers");
      for (wrapperName <- minderTDL.wrapperDefs) {
        val identifier = wrapperName + "|" + wrapperToVersionMap(wrapperName)
        val minderClient = if (BuiltInWrapperRegistry.get().contains(identifier)) {
          BuiltInWrapperRegistry.get().getWrapper(identifier)
        } else {
          val mC = XoolaServer.get().getClient(identifier);
          mutableSutSet.add(mC.getSUTName)
          mC
        }
        minderClient.startTest(userEmail)
      }

      testProcessWatcher.updateSUTNames(mutableSutSet)

      try {
        var rivetIndex = 0;
        for (rivet <- minderTDL.RivetDefs) {
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

                override def getSUTName(): String = ""
              }

            } else {
              val identifier = rivet.slot.wrapperId + "|" + wrapperToVersionMap(rivet.slot.wrapperId)
              if (BuiltInWrapperRegistry.get().containsWrapper(identifier)) {
                BuiltInWrapperRegistry.get().getWrapper(identifier)
              } else {
                XoolaServer.get().getClient(identifier)
              }
            }

          val args = Array.ofDim[Object](rivet.pipes.length)

          //a boolean flag used to see whether a timeout occurred or not
          var thereIsTimoeut = false;

          var signalIndex = 0

          try {
            for (tuple@(label, signature) <- rivet.signalPipeMap.keySet) {
              val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
              if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

              //obtain the source signal object
              val signalList = rivet.signalPipeMap(tuple);
              if (signalList == null || signalList.isEmpty)
                throw new IllegalArgumentException("singal list is empty for " + label + "." + signature);

              val signal = signalList(0).inRef.source;

              val identifier = label + "|" + wrapperToVersionMap(label)
              lgr.debug("> Wait For Signal:" + identifier + "." + signature)

              val signalData: SignalData = try {
                me.dequeueSignal(identifier, signature, signal.timeout).asInstanceOf[SignalData]
              } catch {
                case rte: RuntimeException => {
                  signal.handleTimeout(rte)
                  thereIsTimoeut = true
                  throw new BreakException
                }
              }

              lgr.debug("< Signal Arrived: " + identifier + "." + signature)

              if (signalData.isInstanceOf[SignalErrorData]) {
                lgr.debug("This is an error signal");
                val signalErrorData = signalData.asInstanceOf[SignalErrorData]
                throw new RuntimeException("Signal [" + identifier + "." + signature + "] failed [" + signalErrorData.signalFailedException.getMessage,
                  signalErrorData.signalFailedException)
              }
              val signalCallData: SignalCallData = signalData.asInstanceOf[SignalCallData]

              testProcessWatcher.signalEmitted(rivetIndex, signalIndex, signalCallData)

              for (paramPipe <- rivet.signalPipeMap(tuple)) {
                //FIX for BUG-1 : added an if for -1 param
                if (paramPipe.in != -1) {
                  convertParam(paramPipe.out, paramPipe.execute(signalCallData.args(paramPipe.in)), args)
                }
              }

              signalIndex += 1
            }
          } catch {
            case breakException: BreakException => {
            }
          }

          if (thereIsTimoeut) {
            //we hit a timeout, skip to the next rivet
          } else {
            lgr.debug("Assign free vars")

            for (paramPipe <- rivet.freeVariablePipes) {
              val any = paramPipe.execute(null)
              convertParam(paramPipe.out, any, args)
            }

            if (rivet.freeVariablePipes.size > 0) {
              val freeArgs = Array.ofDim[Object](rivet.freeVariablePipes.size);
              var k = 0;
              for (paramPipe <- rivet.freeVariablePipes) {
                freeArgs(k) = args(paramPipe.out)
                k += 1
              }
              val signalData2 = new SignalCallData(freeArgs);
              testProcessWatcher.signalEmitted(rivetIndex, signalIndex, signalData2)
            }

            lgr.info("> CALL SLOT " + rivet.slot.wrapperId + "." + rivet.slot.signature)
            rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)
            lgr.info("< SLOT CALLED " + rivet.slot.wrapperId + "." + rivet.slot.signature)


            testProcessWatcher.rivetFinished(rivetIndex)
            lgr.info("< Rivet finished sucessfully")
            lgr.info("----------\n")

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
          } finally {
          }
        }

        if (minderTDL.exception != null) {
          //we have a late error
          throw minderTDL.exception;
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

      minderTDL = null;
    }

    //   mapMe("A" -> "A".getBytes(), "B" -> "B".getBytes())

  }

  def convertParam(out: Int, arg: Any, args: Array[Object]) {
    if (arg == null) {
      args(out) = null;
    } else if (arg.isInstanceOf[Rivet]) {
      args(out) = arg.asInstanceOf[Rivet].result
    } else if (arg.isInstanceOf[MinderNull]) {
      args(out) = null;
    } else {
      args(out) = arg.asInstanceOf[AnyRef]
    }
  }

  /**
   * Evaluates the whole test case without running it and creates a list of wrappers and their signals-slots.
   *
   * @param tdl
   * @return
   */
  def describeTdl(tdl: Tdl): util.LinkedHashMap[String, util.Set[SignalSlot]] = {
    val testCase = tdl.testCase;
    testCase.testAssertion = TestAssertion.findById(testCase.testAssertion.id)
    testCase.testAssertion.testGroup = TestGroup.findById(testCase.testAssertion.testGroup.id)
    val root = "_" + testCase.testAssertion.testGroup.id;
    val packagePath = root + "/_" + testCase.id;
    val minderClass = TdlCompiler.compileTdl(root, packagePath, testCase.testAssertion.testGroup.dependencyString, testCase.name, source = tdl.tdl, version = tdl.version);

    val minderTdl = try {
      minderClass.getConstructors()(0).newInstance(null, java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    } catch {
      case ex: InvocationTargetException => {
        var cause: Throwable = ex
        while (cause.getCause != null) cause = cause.getCause
        throw cause
      }
    }

    val slotDefs = minderTdl.RivetDefs

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

  def updateSUTNames(set: scala.collection.Set[String]): Unit

  def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit

  def rivetFinished(rivetIndex: Int): Unit

  def finished(): Unit

  def addLog(log: String): Unit

  def addReportLog(s: String): Unit

  def failed(message: String, t: Throwable): Unit
}
