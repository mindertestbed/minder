package controllers

import controllers.common.Utils
import minderengine.{MinderSignalRegistry, HttpSession}
import models.{Job, Tdl, TestCase, User}
import play.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.collection.mutable

/**
 * This is the play controller that serves the http side for jobs
 */
object Tester extends Controller {
  def test() = Action { implicit request =>
    val tdl = request.body.asText.mkString
    val mail = request.session.get("userEmail").mkString

    testMap(mail).sessionID = Utils.getCurrentTimeStamp;
    HttpSession.registerObject(testMap(mail).sessionID, "signalRegistry", new MinderSignalRegistry());
    try {
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
    val tdl = Tdl.findById(rc2.tdl.id);
    val testCase = TestCase.findById(rc2.tdl.id)
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
    *
    * @param id
   * @return
   */
  def runTest(id: Long, userId: Long) = Action { implicit request =>
    val user = User.findById(userId);
    val mail = user.email;

    val rc = Job.findById(id);
    //
    try {

      if (testMap.contains(mail)) {
        val testRunner = testMap(mail);
        //if (testRunner.status == TestStatus.BAD || testRunner.status == TestStatus.GOOD) {
        //  GlobalSignalRegistry.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
        //  SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())
        //val testRunner = new TestRunner(rc, user);
        // testMap(mail) = testRunner
        //a}
      } else {
        testMap(mail).sessionID = Utils.getCurrentTimeStamp;
        HttpSession.registerObject(testMap(mail).sessionID, "signalRegistry", new MinderSignalRegistry());
        //val testRunner = new TestRunner(rc, user);
        // testMap(mail) = testRunner
      }
      Ok; //testRunStatus.render(rc, user))
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
    *
    * @param id
   * @return
   */
  def syncTest(id: Long, userId: Long) = Action { implicit request =>
    val user = User.findById(userId);
    val rc = Job.findById(id);
    Logger.debug("SYNC")


    Ok; //(testRunStatus.render(rc, user))
  }


  /**
   * SSE tunnel for listing the active jobs running now.
   */
  val (jobListOut, jobListChannel) = Concurrent.broadcast[JsValue];


  val testQueue = mutable.Queue[Job]();
}
