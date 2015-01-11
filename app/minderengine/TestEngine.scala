package minderengine

import mtdl.{Rivet, MinderTdl, TdlCompiler}
import play.api.Logger
import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 07/12/14.
 */
class TestEngine {


  private def createMinderInstance(minderClass: Class[MinderTdl], seq: Seq[(String, String)]): MinderTdl = {

    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- seq) {
        map2 += e
      }
      map2.toMap
    }
    minderClass.getConstructors()(0).newInstance(map).asInstanceOf[MinderTdl]
  }

  /**
   * Runs the given test case
   * Created by yerlibilgin on 07/12/14.
   *
   * @param userEmail the owner email if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTest(userEmail: String, tdl: String, wrapperMapping: (String, String)*): Unit = {
    val clsMinderTDL = TdlCompiler.compileTdl(userEmail, tdl)

    val minderTDL = createMinderInstance(clsMinderTDL, wrapperMapping);
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
      var i = 1;
      for (rivet <- minderTDL.SlotDefs) {
        Logger.debug("---- " + "RUN RIVET " + i)
        i = i + 1

        //resolve the minder client id. This might as well be resolved to a local built-in wrapper.
        val minderClient = if (BuiltInWrapperRegistry.get().containsWrapper(rivet.slot.wrapperId)) {
          BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
        } else {
          XoolaServer.get().getClient(rivet.slot.wrapperId)
        }
        val args = Array.ofDim[Object](rivet.pipes.length)

        for (tuple@(label, signature) <- rivet.signalPipeMap.keySet) {
          val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
          if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

          println("dequeue " + (label + ":" + signature))

          val signalData = me.dequeueSignal(label, signature)
          for (paramPipe <- rivet.signalPipeMap(tuple)) {
            args(paramPipe.out) = paramPipe.execute(signalData.args(paramPipe.in)).asInstanceOf[AnyRef]

            convertParam(paramPipe.out, paramPipe.execute(signalData.args(paramPipe.in)))
          }
        }

        for (paramPipe <- rivet.freeVariablePipes) {
          convertParam(paramPipe.out, paramPipe.execute(null))
        }

        rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)


        def convertParam(out: Int, arg: Any) {
          if (arg.isInstanceOf[Rivet]) {
            args(out) = arg.asInstanceOf[Rivet].result
          } else {
            args(out) = arg.asInstanceOf[AnyRef]
          }
        }
      }
    } finally {
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
  }
}
