package controllers

import minderengine.{MinderWrapperRegistry, TestEngine, MinderSignalRegistry, SessionMap}
import mtdl.SignalSlotInfoProvider
import play.api.mvc._
import play.mvc.Http
import views.html

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

  def doRunRunconfiguration(id: Long) = Action { implicit request =>
    //val tdl = request.body.asText.mkString

    val user = Application.getLocalUser(request.session.asInstanceOf[Http.Session])
    val mail = user.email;

    BadRequest
  }

}

