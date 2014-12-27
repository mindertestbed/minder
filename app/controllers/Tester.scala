package controllers

import minderengine.{MinderWrapperRegistry, TestEngine, MinderSignalRegistry, SessionMap}
import mtdl.SignalSlotInfoProvider
import play.api.mvc._
import views.html

object Tester extends Controller {
  def test() = Action { implicit request =>
    val tdl = request.body.asText.mkString
    val mail = request.session.get("userEmail").mkString

    SessionMap.registerObject(mail, "signalRegistry", new MinderSignalRegistry());
    val te = new TestEngine();
    try {
      SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())

      te.runTest(mail, tdl);
      play.mvc.Results.ok();
    } catch {
      case t: Throwable => {
        t.printStackTrace()
        play.mvc.Results.internalServerError();
      }
    }
    Ok("It works!")
  }
}

