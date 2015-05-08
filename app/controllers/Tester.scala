package controllers

import controllers.common.enumeration.TestStatus
import minderengine.{MinderSignalRegistry, MinderWrapperRegistry, SessionMap}
import models.{Job, TestCase, User}
import mtdl.SignalSlotInfoProvider
import play.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc._
import views.html._

import scala.collection.mutable

/**
 * This is the play controller that serves the http side for jobs
 */
object Tester extends Controller {
  def test() = Action { implicit request =>
    val tdl = request.body.asText.mkString
    val mail = request.session.get("userEmail").mkString

    SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
    try {
      SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())

      //TestEngine.runTest(mail, "XMLTEST", tdl, "$xmlValueInitiator" -> "xmlValueInitiator", "$xmlGenerator" -> "xmlGenerator");
      Ok
    } catch {
      case t: Throwable => {
        t.printStackTrace()
        BadRequest(t.getMessage);
      }
    }
  }

  val testMap: collection.mutable.Map[String, TestRunContext] = collection.mutable.Map()

  def getTestRunner(user: User, job: Job): TestRunContext = {
    val rc2 = Job.findById(job.id)
    val testCase = TestCase.findById(job.testCase.id)
    var testRunner = testMap(user.email);

    if (testRunner == null) {
      //dummy
      //testRunner = new TestRunner(rc2, user)
    }

    return testRunner;
  }

  /**
   * This method starts a test in its first call,
   * then provides information about if (in successive calls)
   * @param id
   * @return
   */
  def runTest(id: Long, userId: Long) = Action { implicit request =>
    val user = User.find.byId(userId);
    val mail = user.email;

    val rc = Job.findById(id);

    println("--------------")
    println("TEST CASE TDL " + rc.testCase.id)
    println("TEST CASE TDL " + rc.testCase.tdl)
    println("--------------")
    //
    try {

      if (testMap.contains(mail)) {
        val testRunner = testMap(mail);
        //if (testRunner.status == TestStatus.BAD || testRunner.status == TestStatus.GOOD) {
        //  SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
        //  SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())
          //val testRunner = new TestRunner(rc, user);
         // testMap(mail) = testRunner
        //a}
      } else {
        SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
        SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())
        //val testRunner = new TestRunner(rc, user);
       // testMap(mail) = testRunner
      }
      Ok;//testRunStatus.render(rc, user))
    }

    catch {
      case t: Throwable => {
        Logger.error(t.getMessage, t);
        if (t.getMessage != null)
          BadRequest(t.getMessage)
        else
          BadRequest("Unknown error. Check logs")
      }
    }
  }

  /**
   * This method starts a test in its first call,
   * then provides information about if (in successive calls)
   * @param id
   * @return
   */
  def syncTest(id: Long, userId: Long) = Action { implicit request =>
    val user = User.find.byId(userId);
    val rc = Job.findById(id);
    Logger.debug("SYNC")


    Ok;//(testRunStatus.render(rc, user))
  }


  /**
   * SSE tunnel for listing the active jobs running now.
   */
  val (jobListOut, jobListChannel) = Concurrent.broadcast[JsValue];


  val testQueue = mutable.Queue[Job]();
}
