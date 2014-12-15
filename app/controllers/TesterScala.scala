package controllers

import minderengine._
import mtdl.SignalSlotInfoProvider
import play.mvc.Controller
import play.mvc.Result
import play.mvc.Http._
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by yerlibilgin on 15/12/14.
 */
object TesterScala extends Controller {
  def test(tdl: String): Result = {
    //get
    //register a signal registry for me

    SessionMap.registerObject("myildiz83@gmail.com", "signalRegistry", new MinderSignalRegistry());
    val te = new TestEngine();
    try {

      println("Will run tests")

      SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())

      val labels = MinderWrapperRegistry.get().getAllLabels

      for(label <- labels){
        println(label)
      }

      te.runTest("myildiz83@gmail.com", tdl);
      play.mvc.Results.ok();
      //play.mvc.Results.internalServerError();
    } catch {
      case t: Throwable => {
        t.printStackTrace()
        play.mvc.Results.internalServerError();
      }
    }
  }
}
