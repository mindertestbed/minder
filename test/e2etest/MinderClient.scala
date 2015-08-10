package e2etest

import java.lang.reflect.Method
import java.util
import java.util.{Properties, HashMap}

import minderengine._
import org.interop.xoola.core.{Xoola, XoolaTierMode, XoolaProperty}

class MinderClient(properties: Properties) extends IMinderClient with ISignalHandler {
  //load application properties
  properties.setProperty(XoolaProperty.MODE, XoolaTierMode.CLIENT)
  val wrapperName = properties.getProperty("WRAPPER_NAME")
  properties.setProperty(XoolaProperty.CLIENTID, wrapperName)
  val client = Xoola.init(properties)
  //create the minder client, providing the wrapper class name
  val clazz: Class[Wrapper] = Class.forName(properties.getProperty("WRAPPER_CLASS")).asInstanceOf[Class[Wrapper]]
  val wrapper = MinderUtils.createWrapper(clazz, this)
  client registerObject("minderClient", this)
  println("Connecting to server")
  client start()
  client waitForConnection()

  var sessionId: String = null


  val serverObject: IMinderServer = client.get(classOf[IMinderServer], "minderServer")
  val nonBlockingServerObject: IMinderServer = client.get(classOf[IMinderServer], "minderServer", true)

  //Map all the methods that are annotated as signals and slots.
  //this will be presented to the server. This is good to perform method resolution only once
  val methodMap: HashMap[String, MethodContainer] = new HashMap[String, MethodContainer]()
  for (
    method <- clazz.getMethods() //
    if method.getAnnotation(classOf[Signal]) != null || method.getAnnotation(classOf[Slot]) != null
  ) {
    val mc = new MethodContainer(method)
    methodMap put(mc.methodKey, mc)
  }

  //send the information to the server
  var keySet = new util.HashSet[MethodContainer]();

  import scala.collection.JavaConversions._

  for (x <-  methodMap.values()) {
    keySet add x
  }
  serverObject hello(wrapperName, keySet)

  /**
   * getCurrentTestUserInfo
   * the server will call this method. The method performs name resolution and calls
   * the appropriate slot
   */
  override def callSlot(sId: String, slotName: String, args: Array[Object]): Object = {
    checkSession(sId)
    methodMap.get(slotName).method.invoke(wrapper, args: _*)
  }

  def checkSession(sId: String) {
    if (sessionId == null || !(sessionId equals (sId))) {
      throw new MinderException(MinderException.E_INVALID_SESSION)
    }
  }

  /**
   * The SUT is emitting a signal, lets invoke the server
   */
  override def handleSignal(obj: Any, signalMethod: Method, args: Array[Object]): Object = {
    checkSession(sessionId);
    if (signalMethod getName() equals ("getCurrentTestUserInfo")) {
      nonBlockingServerObject.getUserInfo(sessionId)
    } else {
      nonBlockingServerObject signalEmitted(sessionId, wrapperName, MethodContainer.generateMethodKey(signalMethod), new SignalCallData(args));
    }
  }

  override def startTest(sessionId: String): Unit = {
    this sessionId = sessionId
    wrapper startTest
  }

  override def finishTest(): Unit = {
    wrapper finishTest;
    this sessionId = null
  }
}
