package minderengine

import java.io.{PipedInputStream, PipedOutputStream}

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.core.status.{Status, StatusListener}
import org.slf4j.LoggerFactory

/**
  * A logback implementation of the report logger
  *
  * @author yerlibilgin
  */
class MinderReportAppender(testProcessWatcher: TestProcessWatcher) extends OutputStreamAppender[ILoggingEvent] {

  val LOGGER = LoggerFactory.getLogger(this.getClass)

  LOGGER.debug("Initialize MinderReportAppender")


  val lc: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  val ple = new PatternLayoutEncoder()

  ple.setPattern("%date %level %msg%n")
  ple.setContext(lc)
  ple.start();
  this.setEncoder(ple)
  this.setContext(lc)

  val pipeOutputStream = new PipedOutputStream();
  val pipeInputStream = new PipedInputStream(pipeOutputStream);
  this.setOutputStream(pipeOutputStream)
  this.setName(mtdl.Utils.MINDER_REPORT_LOGGER_NAME)

  var targetLogger = LoggerFactory.getLogger(mtdl.Utils.MINDER_REPORT_LOGGER_NAME).asInstanceOf[Logger];

  private val appenderThread = new Thread() {

    import java.io._

    override def run(): Unit = {
      try {
        LOGGER.debug("Start reading the report pipe")
        val reader = new BufferedReader(new InputStreamReader(pipeInputStream))
        while (true) {
          val line = reader.readLine();
          if (line == null) {
            return;
          }
          testProcessWatcher.addReportLog(line)
        }
      } catch {
        case _ =>
      } finally {
        try {
          pipeInputStream.close();
        } catch {
          case _ =>
        }
        LOGGER.debug("Report thread exit")
      }
    }
  }

  override def stop(): Unit = {
    targetLogger.detachAppender(this.getName)
    super.stop()
  }

  getStatusManager.add(new StatusListener {
    def addStatusEvent(status: Status): Unit = {
      println(status.getMessage)
      if (status.getThrowable != null)
        status.getThrowable.printStackTrace()
    }
  })

  appenderThread.start();
  targetLogger.setLevel(Level.DEBUG)
  targetLogger.setAdditive(true)
  targetLogger.addAppender(this)
}