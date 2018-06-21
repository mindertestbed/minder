package minderengine

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, Logger, LoggerContext, PatternLayout}
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.status.{Status, StatusListener}
import org.slf4j.LoggerFactory

/**
  * A logback implementation of the report logger
  *
  * @author yerlibilgin
  */
class MinderReportAppender(testProcessWatcher: TestProcessWatcher) extends AppenderBase[ILoggingEvent] {

  val LOGGER = LoggerFactory.getLogger(this.getClass)
  LOGGER.debug("Initialize MinderReportAppender")

  val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  val patternLayout = new PatternLayout()
  var targetLogger = LoggerFactory.getLogger(mtdl.Utils.MINDER_REPORT_LOGGER_NAME).asInstanceOf[Logger];

  this.setContext(loggerContext)
  this.setName(mtdl.Utils.MINDER_REPORT_LOGGER_NAME)

  patternLayout.setPattern("%date %level %msg%n")
  patternLayout.setContext(loggerContext);
  targetLogger.setLevel(Level.DEBUG)
  targetLogger.setAdditive(true)


  getStatusManager.add(new StatusListener {
    def addStatusEvent(status: Status): Unit = {
      if (status.getThrowable != null) {
        LOGGER.error(status.getThrowable.getMessage, status.getThrowable)
      }
    }
  })

  override def start(): Unit = {
    super.start()
    patternLayout.start()
    targetLogger.addAppender(this)
  }

  override def stop(): Unit = {
    super.stop()
    patternLayout.stop()
    targetLogger.detachAppender(this.getName)
  }

  def append(eventObject: ILoggingEvent): Unit = {
    val str = patternLayout.doLayout(eventObject)
    testProcessWatcher.addReportLog(str)
  }
}