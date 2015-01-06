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
   * @param userEmail the owner email if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTest(userEmail: String, tdl: String): Unit = {

    val clsMinderTDL = TdlCompiler.compileTdl(userEmail, tdl)

    val minderTDL = clsMinderTDL.newInstance()

    //first start all tests
    for (rivet <- minderTDL.SlotDefs) {
      val minderClient = if (BuiltInWrapperRegistry.get().contains(rivet.slot.wrapperId)) {
        BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
      } else {
        val label = MinderWrapperRegistry.get().getUidForLabel(rivet.slot.wrapperId)
        XoolaServer.get().getClient(label)
      }

      minderClient.startTest(userEmail)
    }

    try {
      var i = 1;
      for (rivet <- minderTDL.SlotDefs) {
        println("---- " + "RUN RIVET " + i)
        i = i + 1

        //resolve the minder client id. This might as well be resolved to a local built-in wrapper.
        val minderClient = if (BuiltInWrapperRegistry.get().containsWrapper(rivet.slot.wrapperId)) {
          BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
        } else {
          val label = MinderWrapperRegistry.get().getUidForLabel(rivet.slot.wrapperId)
          XoolaServer.get().getClient(label)
        }
        val args = Array.ofDim[Object](rivet.pipes.length)

        for (tuple <- rivet.signalPipeMap.keySet) {
          val me: MinderSignalRegistry = SessionMap.getObject(userEmail, "signalRegistry")
          if (me == null) throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + userEmail)

          println("dequeue " + ((tuple _1) + ":" + (tuple _2)))
          println("dequeue " + ((MinderWrapperRegistry.get().getUidForLabel(tuple _1)) + ":" + (tuple _2)))


          val signalData = me.dequeueSignal(MinderWrapperRegistry.get().getUidForLabel(tuple _1), tuple _2)
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
    }finally{
      //make sure that we call finish test for all
      for (rivet <- minderTDL.SlotDefs) {
        val minderClient = if (BuiltInWrapperRegistry.get().contains(rivet.slot.wrapperId)) {
          BuiltInWrapperRegistry.get().getWrapper(rivet.slot.wrapperId)
        } else {
          val label = MinderWrapperRegistry.get().getUidForLabel(rivet.slot.wrapperId)
          XoolaServer.get().getClient(label)
        }

        try{ minderClient.finishTest()} catch{case _: Throwable => {}}
      }
    }
  }
}
