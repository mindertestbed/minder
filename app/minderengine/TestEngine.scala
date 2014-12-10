package minderengine

import mtdl.{Rivet, MinderTdl, TdlCompiler}
import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 07/12/14.
 */
class TestEngine {

  /**
   * Runs the given test case
   * Created by yerlibilgin on 07/12/14.
   *
   * @param userEmail the user email if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTest(userEmail: String, tdl: String): Unit = {
		  
    val clsMinderTDL = TdlCompiler.compileTdl(userEmail, tdl)

    val minderTDL = clsMinderTDL.newInstance()
    for (rivet <- minderTDL.SlotDefs) {
      var minderClient = XoolaServer.get().getClient(rivet.slot.wrapperId)
      val args = Array.ofDim[Object](rivet.pipes.length)

      for (tuple <- rivet.signalPipeMap.keySet) {
        val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "minderSignalRegistry")
        if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

        var signalData = me.dequeueSignal(tuple _1, tuple _2)
        for (paramPipe <- rivet.signalPipeMap(tuple)) {
          args(paramPipe.out) = paramPipe.converter(signalData.args(paramPipe.in))

          convertParam(paramPipe.out, paramPipe.converter(signalData.args(paramPipe.in)))
        }
      }

      for (paramPipe <- rivet.freeVariablePipes) {
        convertParam(paramPipe.out, paramPipe.converter(null))
      }

      
      rivet.result = minderClient.callSlot(userEmail, rivet.slot.signature, args)


      def convertParam(out: Int, arg: Object) {
        if (arg.isInstanceOf[Rivet]) {
          args(out) = arg.asInstanceOf[Rivet].result
        } else {
          args(out) = arg
        }
      }
    }
  }
}
