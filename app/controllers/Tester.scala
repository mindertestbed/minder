package controllers

import minderengine.{MinderWrapperRegistry, TestEngine, MinderSignalRegistry, SessionMap}
import models.{TestCase, User, RunConfiguration}
import mtdl.SignalSlotInfoProvider
import play.Logger
import play.api.mvc._
import play.mvc.Http
import views.html._

object Tester extends Controller {
  def test() = Action { implicit request =>
    val tdl = request.body.asText.mkString
    val mail = request.session.get("userEmail").mkString

    SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
    try {
      SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())

      TestEngine.runTest(mail, tdl, "$xmlValueInitiator" -> "xmlValueInitiator", "$xmlGenerator" -> "xmlGenerator");
      Ok
    } catch {
      case t: Throwable => {
        t.printStackTrace()
        BadRequest(t.getMessage);
      }
    }
  }

  val testMap: collection.mutable.Map[String, TestRunner] = collection.mutable.Map()

  def getTestRunner(user: User, runConfiguration: RunConfiguration): TestRunner = {
    val rc2 = RunConfiguration.findById(runConfiguration.id)
    val testCase = TestCase.findById(runConfiguration.testCase.id)
    var testRunner = testMap(user.email);

    if (testRunner == null) {
      //dummy
      testRunner = new TestRunner(rc2, user)
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

    val rc = RunConfiguration.findById(id);

    println("--------------")
    println("TEST CASE TDL " + rc.testCase.id)
    println("TEST CASE TDL " + rc.testCase.tdl)
    println("--------------")
    //
    try {

      if (testMap.contains(mail)) {
        val testRunner = testMap(mail);
        if (testRunner.status == TestStatus.BAD || testRunner.status == TestStatus.GOOD){
          SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
          SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())
          val testRunner = new TestRunner(rc, user);
          testMap(mail) = testRunner
        }
      } else {
        SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
        SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())
        val testRunner = new TestRunner(rc, user);
        testMap(mail) = testRunner
      }
      Ok(testStatusViewer.render(rc, user))
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
    val rc = RunConfiguration.findById(id);
    Logger.debug("SYNC")
    Ok(testStatusViewer.render(rc, user))
  }

}

