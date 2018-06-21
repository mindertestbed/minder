package minderengine

import java.io.ByteArrayInputStream
import java.lang.reflect.InvocationTargetException
import java.util
import java.util.Properties
import javax.inject.{Inject, Provider, Singleton}

import com.gitb.core.v1.StepStatus
import controllers.{EPQueueManager, TestQueueController, TestRunContext}
import models._
import mtdl._
import org.slf4j.LoggerFactory
import play.api
import utils.Util

import scala.collection.JavaConversions._
import scala.collection.mutable;

/**
  * Created by yerlibilgin on 07/12/14.
  */
@Singleton
class TestEngine @Inject()(implicit testQueueController: Provider[TestQueueController],
                           epQueueManager: Provider[EPQueueManager],
                           xoolaServer: Provider[XoolaServer]) {


  val LOGGER = LoggerFactory.getLogger(getClass)

  class GitbWatcher() extends GitbRivetWatcher {
    override def notifyProcessingInfo(log: String, rivet: Rivet): Unit = {
      notify(log, StepStatus.PROCESSING, rivet)
    }

    override def notifySkippedInfo(log: String, rivet: Rivet): Unit = {
      notify(log, StepStatus.SKIPPED, rivet)
    }

    override def notifyWaitingInfo(log: String, rivet: Rivet): Unit = {
      notify(log, StepStatus.WAITING, rivet)
    }

    override def notifyErrorInfo(log: String, rivet: Rivet): Unit = {
      notify(log, StepStatus.ERROR, rivet)
    }

    override def notifyCompletedInfo(log: String, rivet: Rivet): Unit = {
      notify(log, StepStatus.COMPLETED, rivet)
    }

    def notify(log: String, stepStatus: StepStatus, rivet: Rivet): Unit = {
      testQueueController.get().gitbLogFeedUpdate(log, stepStatus, rivet.tplStepId)
    }
  }

  /**
    * Runs the provided already compiled tdl class with the given parameter mapping
    *
    * @param userEmail
    * @param adapterMapping
    * @param testRunContext
    */
  def runTest(testRunContext: TestRunContext, params: String,
              adapterMapping: collection.mutable.Map[String, MappedAdapter],
              userEmail: String): Unit = {

    val minderReportAppender = new MinderReportAppender(testRunContext)
    minderReportAppender.start();

    val lgr = LoggerFactory.getLogger(mtdl.Utils.MINDER_REPORT_LOGGER_NAME);

    val gtb = new GitbWatcher();
    try {
      try {

        val currentMtdl = testRunContext.initialize();
        initializeFunctionsAndParameters(params, testRunContext)

        if (testRunContext.isSuspended()) {
          //it is suspended. Resume
          lgr.info("Resume Test")
          testRunContext.resume();
        } else {
          lgr.info(s"Minder version ${Util.getVersionInfo()}")
          lgr.info("Start Test")
          lgr.info("Initialize test case")
          testRunContext.identifierMinderClientMap = mapAllTransitiveAdapters(currentMtdl, adapterMapping)

          val sutNameSet = new util.HashSet[String]

          //first, call the start methods for all registered adapters of this test.

          lgr.info("> Initialize the Adapters");

          val startTestObject = new StartTestObject


          val properties = new Properties()
          properties.load(new ByteArrayInputStream(params.getBytes()))


          startTestObject.setSession(testRunContext.session)
          startTestObject.setProperties(properties)


          //call start test on all adapters and populate the SUT names
          testRunContext.identifierMinderClientMap.values().foreach(pair => {
            lgr.info("Call start test on " + pair.adapterIdentifier.getName)
            pair.minderClient.startTest(startTestObject)

            lgr.info("Obtain SUT Identifiers from " + pair.adapterIdentifier.getName)
            val sutIdentifiers = pair.minderClient.getSUTIdentifiers;

            if (sutIdentifiers != null) {
              for (sutIdentifier <- sutIdentifiers.getIdentifiers) {
                lgr.info("System Under Test: " + sutIdentifier.getSutName)
                sutNameSet.add(sutIdentifier.getSutName)
              }
            }
          })


          //update the SUT names on the watcher
          testRunContext.updateSUTNames(sutNameSet)
        }

        while (currentMtdl.currentRivetIndex < currentMtdl.RivetDefs.size()) {
          val currentRivet = currentMtdl.RivetDefs(currentMtdl.currentRivetIndex)
          //for (currentRivet <- minderTDL.RivetDefs) {
          var msg: String = "--> RUN RIVET " + currentMtdl.currentRivetIndex;
          lgr.info(msg)
          gtb.notifyProcessingInfo(msg, currentRivet)

          /**
            * A very bad method that is used by two different blocks.
            */
          def logRivetFinished = {
            msg = "<-- Rivet finished sucessfully";
            testRunContext.rivetFinished(currentMtdl.currentRivetIndex)
            gtb.notifyCompletedInfo(msg, currentRivet)
            lgr.info(msg)
          }

          if (currentRivet.isInstanceOf[Suspend]) {
            //save testRunContext in a map
            lgr.info("Suspend Test Case");
            suspendTestCase(testRunContext)
            return
          } else if (currentRivet.isInstanceOf[EndpointRivet]) {
            LOGGER.debug("Matched an endpoint rivet. If the endpoint is not already enqueued, suspend the test case");
            //check the signal queue for the matching http endpoint
            //if no signal is enqueued, then suspend the test case
            val endpointRivet = currentRivet.asInstanceOf[EndpointRivet]
            val epAdapterId = AdapterIdentifier.parse(endpointRivet.adapterFunction.adapterId)
            val httpEndpoint = endpointRivet.method + ":" + testRunContext.mtdlInstance.getParameter(endpointRivet.endPointIdentifier)

            val signalData = MinderSignalRegistry.get().dequeueSignalImmediately(testRunContext.session, epAdapterId, httpEndpoint)

            if (signalData == null) {
              lgr.info("Suspend Test Case for HTTP Call " + httpEndpoint);
              suspendForEndpoint(testRunContext)
              return
            } else {
              LOGGER.debug("And http call for " + httpEndpoint + " was discovered. Process it")

              if (!signalData.isInstanceOf[SignalPojoData]) {
                throw new IllegalStateException("An invalid signal type has been enqueued for " + httpEndpoint)
              }

              val signalPojoData = signalData.asInstanceOf[SignalPojoData]
              val ePPacket = signalPojoData.payload.asInstanceOf[EPPacket]

              lgr.info(s"Call handler for $httpEndpoint")
              endpointRivet.targetFunc(ePPacket);
              try {
                ePPacket.httpInputStream.close();
              } catch {
                case _ =>
              }

              lgr.info(s"Handler for $httpEndpoint finished")

              logRivetFinished
            }
          } else {
            val rivetAdapterId: String = currentRivet.adapterFunction.adapterId
            //resolve the minder client id. This might as well be resolved to a local built-in adapter or the null slot.
            val slotPair = findPairOrError(testRunContext.identifierMinderClientMap, AdapterIdentifier.parse(rivetAdapterId))
            val args = Array.ofDim[Object](currentRivet.pipes.length)

            //a boolean flag used to see whether a timeout occurred or not
            var thereIsTimoeut = false;

            var signalIndex = 0

            try {
              for (tuple@(label, signature) <- currentRivet.signalPipeMap.keySet) {

                if (!MinderSignalRegistry.get().hasSession(testRunContext.session)) {
                  msg = "No MinderSignalRegistry object defined for session " + userEmail;
                  gtb.notifyErrorInfo(msg, currentRivet)
                  throw new scala.IllegalArgumentException(msg)
                }

                //obtain the source signal object
                val signalList = currentRivet.signalPipeMap(tuple);
                if (signalList == null || signalList.isEmpty) {
                  msg = "singal list is empty for " + label + "." + signature;
                  gtb.notifyErrorInfo(msg, currentRivet);
                  throw new scala.IllegalArgumentException(msg);
                }

                val signal = signalList(0).inRef.source;

                val signalPair = findPairOrError(testRunContext.identifierMinderClientMap, AdapterIdentifier.parse(label));

                val signalAdapterIdentifier = signalPair.adapterIdentifier
                msg = "> Wait For Signal:" + signalAdapterIdentifier + "." + signature;
                lgr.debug(msg)
                gtb.notifyWaitingInfo(msg, currentRivet)


                val signalData: SignalData = try {
                  MinderSignalRegistry.get().dequeueSignal(testRunContext.session, signalAdapterIdentifier, signature, signal.timeout)
                } catch {
                  case rte: RuntimeException => {
                    signal.handleTimeout(rte)
                    thereIsTimoeut = true
                    gtb.notifyErrorInfo("Break currentRivet", currentRivet)
                    throw new BreakException
                  }
                }

                msg = "< Signal Arrived: " + signalAdapterIdentifier + "." + signature
                lgr.debug(msg)
                gtb.notifyProcessingInfo(msg, currentRivet)

                if (signalData.isInstanceOf[SignalErrorData]) {
                  lgr.debug("This is an error signal");
                  val signalErrorData = signalData.asInstanceOf[SignalErrorData]
                  msg = "Signal [" + signalAdapterIdentifier + "." + signature + "] failed [" + signalErrorData.signalFailedException.getMessage
                  gtb.notifyErrorInfo(msg, currentRivet)
                  throw new scala.RuntimeException(msg, signalErrorData.signalFailedException)
                }
                val signalCallData: SignalCallData = signalData.asInstanceOf[SignalCallData]

                testRunContext.signalEmitted(currentMtdl.currentRivetIndex, signalIndex, signalCallData)

                for (paramPipe <- currentRivet.signalPipeMap(tuple)) {
                  //FIX for BUG-1 : added an if for -1 param
                  if (paramPipe.in != -1) {
                    val arg = Util.readObject(signalCallData.args(paramPipe.in), currentMtdl.getClass.getClassLoader)
                    convertParam(paramPipe.out, paramPipe.execute(arg), args)
                  }
                }
                signalIndex += 1
              }
            } catch {
              case breakException: BreakException => {
              }
            }

            if (thereIsTimoeut) {
              //we hit a timeout, skip to the next currentRivet
              gtb.notifySkippedInfo("< Rivet skipped", currentRivet)
            } else {
              lgr.debug("Assign free vars")

              for (paramPipe <- currentRivet.freeVariablePipes) {
                val any = paramPipe.execute(null)
                convertParam(paramPipe.out, any, args)
              }

              if (currentRivet.freeVariablePipes.size > 0) {
                val freeArgs = Array.ofDim[Object](currentRivet.freeVariablePipes.size);
                var k = 0;
                for (paramPipe <- currentRivet.freeVariablePipes) {
                  freeArgs(k) = args(paramPipe.out)
                  k += 1
                }
                val signalData2 = new SignalCallData(freeArgs);
                testRunContext.signalEmitted(currentMtdl.currentRivetIndex, signalIndex, signalData2)
              }

              msg = "> CALL SLOT " + slotPair.adapterIdentifier + "." + currentRivet.adapterFunction.signature
              lgr.info(msg)
              gtb.notifyProcessingInfo(msg, currentRivet)
              testRunContext.rivetInvoked(currentMtdl.currentRivetIndex);
              currentRivet.result = slotPair.minderClient.callSlot(testRunContext.session, currentRivet.adapterFunction.signature, args)
              msg = "< SLOT CALLED " + slotPair.adapterIdentifier + "." + currentRivet.adapterFunction.signature;
              lgr.info(msg)
              gtb.notifyProcessingInfo(msg, currentRivet)


              logRivetFinished

            }
            if (Thread.currentThread().isInterrupted) throw new scala.InterruptedException("Test interrupted")
          }
          currentMtdl.currentRivetIndex += 1;
        }

        if (currentMtdl.exception != null) {
          throw currentMtdl.exception
        } else {
          lgr.info(s"Test #${testRunContext.testRun.number} Finished")
          testRunContext.finished()
        }
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
          lgr.error(s"Test #${testRunContext.testRun.number} Failed")
          lgr.error(error, t)
          testRunContext.failed(error, t);
        }
      } finally {
        if (testRunContext.isSuspended()) {
          lgr.info("> Test Suspended")
        } else {
          lgr.info("> Send finish message to all adapters")
          //make sure that we call finish test for all
          val finishTestObject: FinishTestObject = new FinishTestObject
          finishTestObject.setSession(testRunContext.session)
          try {
            testRunContext.identifierMinderClientMap.values().foreach(pair => {
              pair.minderClient.finishTest(finishTestObject)
            })
          } catch {
            case th: Throwable =>
          }
        }
      }
    }finally {
      minderReportAppender.stop();
    }
  }

  private def initializeFunctionsAndParameters(params: String, testRunContext: TestRunContext): Unit = {
    //Redirect all the logging functions to the log4j logger.
    //fixme: change the logger with SLF4J.
    val mtdl = testRunContext.mtdlInstance;

    //the report metadata provided by the script is redirected
    //to the test run context which serializes it to the database.
    mtdl.addReportMetadata = (key: String, value: String) => {
      testRunContext.addReportMetadata(key, value)
    }

    mtdl.setParams(params);
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


  private def mapAllTransitiveAdapters(mtdl: MinderTdl, adapterMapping: mutable.Map[String, MappedAdapter]): util.Map[AdapterIdentifier, IdentifierClientPair] = {
    //map all the variable names to actual adapters, and resolve everything
    val identifierMinderClientMap = new util.LinkedHashMap[AdapterIdentifier, IdentifierClientPair];

    //create NULLSLOT client
    val nullIdentifier = AdapterIdentifier.parse(MinderTdl.NULL_ADAPTER_NAME);
    identifierMinderClientMap.put(nullIdentifier, IdentifierClientPair(nullIdentifier, new NullClient))

    for (adapterName <- mtdl.adapterDefs) {
      val adapterIdentifier = AdapterIdentifier.parse(adapterName)

      if (adapterIdentifier.getName.startsWith("$")) {
        if (adapterIdentifier.getVersion != null)
          throw new IllegalArgumentException("A variable adapter name cannot have version [" + adapterIdentifier + "]")
        // this is a variable
        //resolve the variable name from the adapterMapping
        if (!adapterMapping.contains(adapterIdentifier.getName))
          throw new IllegalArgumentException(adapterIdentifier.getName + " has not been mapped to a real adapter")

        val mapping: MappedAdapter = adapterMapping(adapterIdentifier.getName)

        val transitiveAdapterIdentifier = AdapterIdentifier.parse(mapping.adapterVersion.adapter.name + "|" +
          mapping.adapterVersion.version)
        val client = if (BuiltInAdapterRegistry.get().contains(transitiveAdapterIdentifier)) {
          BuiltInAdapterRegistry.get().getAdapter(transitiveAdapterIdentifier)
        } else {
          xoolaServer.get().getClient(transitiveAdapterIdentifier);
        }
        identifierMinderClientMap.put(adapterIdentifier, IdentifierClientPair(transitiveAdapterIdentifier, client))
      } else {
        //this is a regular adapter
        //resolve the version
        val version = if (adapterIdentifier.getVersion() == null) {
          //no version supplied, resolve the latest version
          val tmp = AdapterVersion.latestByAdapterName(adapterIdentifier.getName)
          if (tmp == null) {
            throw new IllegalArgumentException(adapterIdentifier.getName + " has not still declared a version")
          }
          tmp
        } else {
          val tmp = AdapterVersion.findAdapterNameAndVersion(adapterIdentifier.getName, adapterIdentifier.getVersion)
          if (tmp == null) {
            throw new IllegalArgumentException(adapterIdentifier + " was not found")
          }
          tmp
        }

        val transitiveAdapterIdentifier = AdapterIdentifier.parse(version.adapter.name + "|" + version.version)

        val client = if (BuiltInAdapterRegistry.get().contains(transitiveAdapterIdentifier)) {
          BuiltInAdapterRegistry.get().getAdapter(transitiveAdapterIdentifier)
        } else {
          xoolaServer.get().getClient(transitiveAdapterIdentifier);
        }
        identifierMinderClientMap.put(adapterIdentifier, IdentifierClientPair(transitiveAdapterIdentifier, client))
      }

    }
    identifierMinderClientMap
  }

  def findPairOrError(identifierMinderClientMap: util.Map[AdapterIdentifier, IdentifierClientPair], rivetAdapterId: AdapterIdentifier): IdentifierClientPair = {
    if (!identifierMinderClientMap.containsKey(rivetAdapterId))
      throw new RuntimeException("Minder client with " + rivetAdapterId + " was not found")
    identifierMinderClientMap(rivetAdapterId)
  }


  private def suspendTestCase(testRunContext: TestRunContext) = {
    testRunContext.mtdlInstance.currentRivetIndex += 1;
    SuspensionContext.get().addTestContext(testRunContext);
    testRunContext.suspend()
  }

  /**
    * mark the test context as suspended and put it to the HTTP EPQueue
    *
    * @param testRunContext
    */
  def suspendForEndpoint(testRunContext: TestRunContext): Unit = {
    testRunContext.suspend()
    //we are not increasing the rivet index!!! because the endpoint must be called with the HTTP result
    epQueueManager.get().enqueueTextRunContext(testRunContext)
  }
}

object TestEngine {
  def describe(clsMinderTDL: Class[MinderTdl]): util.List[Rivet] = {
    val minderTDL = clsMinderTDL.getConstructors()(0).newInstance(java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    minderTDL.RivetDefs
  }

  /**
    * Evaluates the whole test case without running it and creates a list of adapters and their signals-slots.
    *
    * @param tdl
    * @return
    */
  def describeTdl(tdl: Tdl): util.LinkedHashMap[String, util.Set[AdapterFunction]] = {
    val testCase = tdl.testCase;
    testCase.testAssertion = TestAssertion.findById(testCase.testAssertion.id)
    testCase.testAssertion.testGroup = TestGroup.findById(testCase.testAssertion.testGroup.id)
    val root = "_" + testCase.testAssertion.testGroup.id;
    val packagePath = root + "/_" + testCase.id;
    val minderClass = TdlCompiler.compileTdl(root, packagePath, testCase.testAssertion.testGroup.dependencyString, testCase.name, source = tdl.tdl, version = tdl.version);

    val minderTdl = try {
      minderClass.getConstructors()(0).newInstance(java.lang.Boolean.FALSE).asInstanceOf[MinderTdl]
    } catch {
      case ex: InvocationTargetException => {
        var cause: Throwable = ex
        while (cause.getCause != null) cause = cause.getCause
        throw cause
      }
    }

    val slotDefs = minderTdl.RivetDefs

    val hm: util.LinkedHashMap[String, util.Set[AdapterFunction]] = new util.LinkedHashMap()
    val map: collection.mutable.LinkedHashMap[String, util.Set[AdapterFunction]] = collection.mutable.LinkedHashMap()

    for (r <- slotDefs) {
      //check the slot
      val adapterName = r.adapterFunction.adapterId
      val slotSignature = r.adapterFunction.signature

      var set = map.getOrElseUpdate(adapterName, new util.HashSet[AdapterFunction]())
      set.add(new AdapterFunction(adapterId = adapterName, signature = slotSignature))

      for (e@(k@(wn, ws), v) <- r.signalPipeMap) {
        set = map.getOrElseUpdate(wn, new util.HashSet[AdapterFunction]())
        set.add(new AdapterFunction(wn, ws))
      }
    }

    for ((k, v) <- map) {
      hm.put(k, v)
    }

    hm
  }

}