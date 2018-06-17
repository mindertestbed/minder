package minderengine

import mtdl.Rivet

trait GitbRivetWatcher {

  def notifyProcessingInfo(log: String, rivet: Rivet): Unit

  def notifySkippedInfo(log: String, rivet: Rivet): Unit

  def notifyWaitingInfo(log: String, rivet: Rivet): Unit

  def notifyErrorInfo(log: String, rivet: Rivet): Unit

  def notifyCompletedInfo(log: String, rivet: Rivet): Unit
}