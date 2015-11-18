package minderengine

/**
  * Author: yerlibilgin
  * Date:   17/11/15.
  */
class NullClient extends IMinderClient {
  override def callSlot(testSession: TestSession, s: String, objects: Array[AnyRef]): AnyRef = {
    null
  }

  override def finishTest(finishTestObject: FinishTestObject): Unit = {}

  override def getSUTIdentifier: SUTIdentifier = {
    val identifier: SUTIdentifier = new SUTIdentifier()
    identifier.setSutName("")
    identifier
  }

  override def startTest(startTestObject: StartTestObject): Unit = {

  }
}

