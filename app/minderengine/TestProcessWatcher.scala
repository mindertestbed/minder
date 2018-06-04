package minderengine

/**
  * Author: yerlibilgin
  * Date:   17/11/15.
  */
trait TestProcessWatcher {

  def updateSUTNames(set: scala.collection.Set[String]): Unit

  def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit

  def rivetFinished(rivetIndex: Int): Unit

  def rivetInvoked(rivetIndex: Int): Unit

  def finished(): Unit

  def addLog(log: String): Unit

  def addReportLog(s: String): Unit

  def failed(message: String, t: Throwable): Unit

  def addReportMetadata(key: String, value: String) : Unit
}
