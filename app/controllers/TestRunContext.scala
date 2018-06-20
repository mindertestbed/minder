package controllers

import java.nio.charset.StandardCharsets
import java.util
import java.util.Date
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerFactory}

import com.avaje.ebean.Ebean
import com.yerlibilgin.BinaryUtil
import controllers.common.Utils
import minderengine._
import models.{Adapter, _}
import mtdl._
import play.Logger
import utils.TestRunNumberProvider

import scala.collection.JavaConversions._

/**
  * This class starts a test in a new actor and
  * provides information to the main actor (status)
  * Created by yerlibilgin on 13/01/15.
  */
class TestRunContext(val testRun: TestRun, testRunFeeder: TestRunFeeder, testLogFeeder: TestLogFeeder, testEngine: TestEngine) extends Runnable with TestProcessWatcher {


  testRun.number = TestRunNumberProvider.getNextNumber();

  var identifierMinderClientMap: util.Map[AdapterIdentifier, IdentifierClientPair] = null
  var suspended = false;
  val variableAdapterMapping = collection.mutable.Map[String, MappedAdapter]();
  val mappedAdapters = MappedAdapter.findByJob(testRun.job)
  var sutNames: java.util.Set[String] = null;
  var error = ""
  var job: AbstractJob = AbstractJob.findById(testRun.job.id)
  val user = testRun.runner;
  val tdl = Tdl.findById(job.tdl.id);
  val testCase = TestCase.findById(tdl.testCase.id)
  val testAssertion = TestAssertion.findById(testCase.testAssertion.id)
  val testGroup = TestGroup.findById(testAssertion.testGroup.id)
  job.tdl = tdl;
  tdl.testCase = testCase;

  val packageRoot = "_" + testGroup.id;
  val packagePath = packageRoot + "/_" + testCase.id;
  val cls = TdlCompiler.compileTdl(packageRoot, packagePath, testGroup.dependencyString, testCase.name, source = tdl.tdl, version = tdl.version);
  var mtdlInstance: MinderTdl = null;
  val logStringBuilder = new StringBuilder;

  val reportMetadataMap = new util.LinkedHashMap[String, String]()

  /**
    * Assign this runnable in order to do one last job (set labels ...) before finishing this test run
    */
  var finalRunnable: Runnable = null;

  var sessionID: String = Utils.getCurrentTimeStamp;
  var session = new TestSession(sessionID);

  /**
    * Number of steps that will be calculated at the beginning for percentage
    * calculation.
    */
  var totalSteps = 1 //for divide by zero

  /**
    * Where we are?
    *
    * The progress will be calculated as: progress = currentStep * 100 / totalSteps
    */
  var currentStep = 0;

  /**
    * progress = currentStep * 100 / totalSteps
    */
  var progressPercent = 0;

  var gitbReplyToUrlAddress = "";

  for (mappedAdapter: MappedAdapter <- mappedAdapters) {
    mappedAdapter.parameter = AdapterParam.findById(mappedAdapter.parameter.id);
    mappedAdapter.adapterVersion = AdapterVersion.findById(mappedAdapter.adapterVersion.id);
    mappedAdapter.adapterVersion.adapter = Adapter.findById(mappedAdapter.adapterVersion.adapter.id);
    variableAdapterMapping.put(mappedAdapter.parameter.name, mappedAdapter)
  }

  describe(TestEngine.describe(cls))

  override def run(): Unit = {
    testRun.status = TestRunStatus.IN_PROGRESS
    //testRun.save()
    testEngine.runTest(TestRunContext.this, job.mtdlParameters, variableAdapterMapping, user.email)
  }

  /**
    * This callback comes from the engine so that we can create our status data structure and later update it.
    *
    * @param slotDefs
    */
  def describe(slotDefs: util.List[Rivet]): Unit = {
    totalSteps = slotDefs.size() * 2; //one rivet call, one rivet finished
  }

  def stepUp(): Unit = {
    currentStep += 1
    updateProgress
  }

  def updateProgress = {
    progressPercent = currentStep * 100 / totalSteps
    if (progressPercent > 100)
      progressPercent = 100

    testRunFeeder.testProgressUpdate(progressPercent)
  }

  override def rivetFinished(rivetIndex: Int): Unit = {
    stepUp()
    Logger.info("Rivet " + rivetIndex + " finished")
  }

  override def rivetInvoked(rivetIndex: Int): Unit = {
    stepUp()
    Logger.info("Rivet " + rivetIndex + " finished")
  }

  override def signalEmitted(rivetIndex: Int, signalIndex: Int, signalData: SignalData): Unit = {
  }

  override def finished() {
    testRun.status = TestRunStatus.SUCCESS
    finalizeTestRun()
  }

  override def failed(err: String, t: Throwable) {
    error = err
    testRun.status = TestRunStatus.FAILED
    finalizeTestRun()
  }

  override def addLog(log: String): Unit = {
    logStringBuilder.append(log)
    testLogFeeder.log(LogRecord(testRun, log))
  }

  override def addReportLog(log: String): Unit = {
    logStringBuilder.append(log + "\n")
    testLogFeeder.log(LogRecord(testRun, log))
  }

  def finalizeTestRun(): Unit = {
    testRun.finishDate = new Date();
    testRun.history.setSystemOutputLog(logStringBuilder.toString());

    testRun.errorMessage = error.getBytes("utf-8");
    if (sutNames != null) {
      val sb = new StringBuilder()
      var i: Int = 1
      for (sut <- sutNames) {
        sb.append(i).append("- ").append(sut).append('\n')
        i += 1
      }
      testRun.sutNames = sb.toString();
    }

    //if there is anything to be done before the last save action, do it
    if (finalRunnable != null)
      finalRunnable.run()

    testRun.reportMetadata = utils.Util.gzip(generateXMLMetadata());
    testRun.save()
  }

  override def updateSUTNames(set: scala.collection.Set[String]): Unit = {
    sutNames = set;
  }

  def init() = {
    testRun.status = TestRunStatus.IN_PROGRESS;
    testRun.date = new Date();
  }

  def initialize(): MinderTdl = {
    if (this.mtdlInstance == null) {
      this.mtdlInstance = cls.getConstructors()(0).newInstance(java.lang.Boolean.FALSE).asInstanceOf[MinderTdl];
    }
    mtdlInstance;
  }


  def suspend() = {
    updateProgress
    this.suspended = true
  }

  def resume() = {
    updateProgress
    this.suspended = false
  }

  def isSuspended(): Boolean = suspended


  def addReportMetadata(key: String, value: String): Unit = {
    reportMetadataMap.put(key, value);
  }

  /**
    * Convert the data in <code>metadataMap</code> to xml
    */
  def generateXMLMetadata(): Array[Byte] = {

    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

    val root = document.createElement("reportMetadata")
    document.appendChild(root);


    reportMetadataMap.foreach(kvp => {
      val element = document.createElement("metadata")
      element.setAttribute("name", kvp._1);
      element.setTextContent(BinaryUtil.b2base64(kvp._2.getBytes(StandardCharsets.UTF_8)))
      root.appendChild(element)
    })

    import java.io.StringWriter
    val domSource = new DOMSource(document)
    val transformer = TransformerFactory.newInstance.newTransformer
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
    transformer.setOutputProperty(OutputKeys.METHOD, "xml")
    transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    val sw = new StringWriter
    val sr = new StreamResult(sw)
    transformer.transform(domSource, sr)

    println("Report Metadata:")
    println(sw.toString)

    sw.toString.getBytes(StandardCharsets.UTF_8)
  }

}
