package controllers

import java.io.{File, FileInputStream}
import java.util
import java.util.Collections
import javax.inject.{Inject, Provider, Singleton}

import minderengine._
import models.{AbstractJob, User}
import mtdl.{EPPacket, EndpointRivet}
import org.slf4j.LoggerFactory
import play.api.mvc._
import utils.UserPasswordUtil

import scala.tools.nsc.interpreter.InputStream

/**
  * Manages a collection of endpoints registered to custom URLs under the /ep/ parent URL
  */
@Singleton
class EPQueueManager @Inject()(testQueueController: Provider[TestQueueController]) extends Controller {


  val LOGGER = LoggerFactory.getLogger(classOf[EPQueueManager])

  val endpointQueueMap = new util.LinkedHashMap[String, util.Queue[TestRunContext]]()

  //this map is used for finding the test run contexts through their session
  val crossSessionToURLMap = new util.HashMap[TestSession, String]()

  var activeRunContext: TestRunContext = null;

  //a flag indicating the life of the thread.
  var goon = true;

  val MAX_QUE_SIZE = 100

  /**
    * The main test thread
    */
  def getEP(url: String) = Action(parse.temporaryFile) {
    implicit request => {
      processRequest(request.body.file, request.headers, "GET", url)
    }
  }

  def postEP(url: String) = Action(parse.temporaryFile) {
    implicit request => {
      processRequest(request.body.file, request.headers, "POST", url)
    }
  }

  def putEP(url: String) = Action(parse.temporaryFile) {
    implicit request => {
      processRequest(request.body.file, request.headers, "PUT", url)
    }
  }

  def deleteEP(url: String) = Action(parse.temporaryFile) {
    implicit request => {
      processRequest(request.body.file, request.headers, "DELETE", url)
    }
  }


  def processRequest(file: File, headers: Headers, method: String, url: String): Result = {
    LOGGER.debug(s"Received end point call: $method:$url")
    val decoratedURL = s"$method:/$url"

    if (endpointQueueMap.containsKey(decoratedURL)) {
      LOGGER.debug("A suspended job was found. Enqueue it")
      val queue = endpointQueueMap.get(decoratedURL);
      if (!queue.isEmpty) {
        val context = queue.poll();
        epPacketArrived(decoratedURL, context, headers, new FileInputStream(file))
        Ok
      } else {
        BadRequest("No registered endpoint for " + url)
      }
    } else {
      //check if the endpoint is a registered job

      LOGGER.debug("Try to find the endpoint job for " + url)
      val job = AbstractJob.findByEndpoint(decoratedURL)
      if (job != null) {
        LOGGER.debug("An endpoint job was found. Enqueue it")

        val username = headers.get("USERNAME").getOrElse("tester@minder")
        val password = headers.get("PASSWORD").getOrElse("12345")

        val user = User.findByEmail(username)

        if (user != null) {
          if (util.Arrays.equals(user.password, UserPasswordUtil.generateHA1(user.email, password))) {
            val context = testQueueController.get().createTestRunContext(job, user, Visibility.PUBLIC);
            epPacketArrived(decoratedURL, context, headers, new FileInputStream(file))
            Ok
          } else {
            BadRequest("User login failed")
          }
        } else {
          BadRequest("User not found")
        }
      } else {
        LOGGER.debug("No job was found for " + url + ". Reject the event")
        BadRequest("No registered endpoint for " + url)
      }
    }
  }

  private def epPacketArrived(decoratedURL: String, context: TestRunContext, headers: Headers, httpInputStream: InputStream) = {
    val identifier = AdapterIdentifier.parse(EndpointRivet.ADAPTER_NAME);

    val map = new util.HashMap[String, String]()

    headers.headers.foreach(key => {
      map.put(key._1, key._2)
    })

    val epPacket = new EPPacket(decoratedURL, map, httpInputStream)
    MinderSignalRegistry.get().enqueueSignal(context.session, identifier, decoratedURL, new SignalPojoData(epPacket))
    testQueueController.get().enqueueTestRunContext(context)
  }


  /**
    * Enqueue a test run context that is to suspend
    *
    * @param testRunContext
    */
  def enqueueTextRunContext(testRunContext: TestRunContext): Unit = {

    this.synchronized {
      var rivet = testRunContext.mtdlInstance.RivetDefs.get(testRunContext.mtdlInstance.currentRivetIndex)
      val endpointRivet = rivet.asInstanceOf[EndpointRivet]
      var endPointIdentifier = endpointRivet.endPointIdentifier
      //get the actually assigned http absoluteURL value
      //an absolute URL is a sub url prefixed with /
      var absoluteURL = testRunContext.mtdlInstance.getParameter(endPointIdentifier)
      val decoratedURL = endpointRivet.method + ":" + absoluteURL
      LOGGER.debug("test run context for job " + testRunContext.job.id + " was suspended for " +
        endPointIdentifier + " with value " + decoratedURL)

      val queue =
        if (endpointQueueMap.containsKey(decoratedURL)) {
          endpointQueueMap.get(decoratedURL)
        } else {
          val newQueue = new util.LinkedList[TestRunContext]()
          endpointQueueMap.put(decoratedURL, newQueue)
          newQueue
        }
      queue.offer(testRunContext)

      //make sure that we hold a reference to the queue
      LOGGER.debug("A new session was enqueued " + testRunContext.session + " " + testRunContext.testRun.number)
      crossSessionToURLMap.put(testRunContext.session, decoratedURL);
    }
  }


  def removeTestContext(testRunContext: TestRunContext) = {
    //find the test run context and remove it from the queue

    this.synchronized {
      val session = testRunContext.session
      LOGGER.debug("Remove session " + testRunContext.session + " " + testRunContext.testRun.number + " from the queue")
      if (crossSessionToURLMap.containsKey(session)) {
        LOGGER.debug("Session found")

        val url = crossSessionToURLMap.get(session);
        LOGGER.debug("URL " + url)
        if (endpointQueueMap.containsKey(url)) {
          LOGGER.debug("URL found")
          val queue = endpointQueueMap.get(url);
          LOGGER.debug("Remove " + queue.remove(testRunContext));
        }

        crossSessionToURLMap.remove(session)
      }
    }
  }

}

