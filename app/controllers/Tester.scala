package controllers;

/*
public class Tester extends Controller {
  public static Result test() {
    Http.RequestBody body = request().body();
    try {
      String tdlStr = body.asText();
      System.out.println("start");
      System.out.println(tdlStr);
      System.out.println("end");
      return TesterScala.test(tdlStr);
    } catch (Exception e) {
      e.printStackTrace();
      return internalServerError();
    }
  }
}
*/
object TesterScala extends Controller{
    def test(tdl:String):Result={
    //get
    //register a signal registry for me

    SessionMap.registerObject("myildiz83@gmail.com","signalRegistry",new MinderSignalRegistry());
    val te=new TestEngine();
    try{
    SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get())

    te.runTest("myildiz83@gmail.com",tdl);
    play.mvc.Results.ok();
    }catch{
    case t:Throwable=>{
    t.printStackTrace()
    play.mvc.Results.internalServerError();
    }
    }
    }
    }
