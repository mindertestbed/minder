package minderengine

/**
  * Author: yerlibilgin
  * Date:   17/11/15.
  */
class NullClient extends IMinderClient {
  override def callSlot(testSession: TestSession, s: String, objects: Array[AnyRef]) = null;

  override def finishTest(finishTestObject: FinishTestObject) {}

  override def getSUTIdentifiers: SUTIdentifiers = null

  override def startTest(startTestObject: StartTestObject) {}
}

