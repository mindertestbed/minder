package controllers

import java.util
import java.util.regex.Pattern
import javax.inject.Inject

import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import utils.{JavaAction, ReportUtils, Util}
import views.html.advancedReporting._

/**
  * @author yerlibilgin
  */

class AdvancedBatchReporting @Inject()(implicit authentication: Authentication) extends Controller {

  import scala.collection.JavaConversions._


  val temporaryReports = new util.HashMap[Long, (String, Array[Byte])]()


  implicit val testSuiteWrites = new Writes[models.TestSuite] {
    def writes(o: models.TestSuite) = Json.obj(
      "id" -> o.id,
      "name" -> o.name
    )
  }

  implicit val suiteRunWrites = new Writes[SuiteRun] {
    def writes(o: SuiteRun) = Json.obj(
      "id" -> o.id.longValue(),
      "date" -> Util.formatDate(o.date)
    )
  }

  implicit val runWrites = new Writes[TestRun] {
    def writes(o: TestRun) = Json.obj(
      "id" -> o.id.longValue(),
      "date" -> Util.formatDate(o.date),
      "runner" -> o.runner.email,
      "number" -> o.number,
      "result" -> (o.status == TestRunStatus.SUCCESS)
    )
  }

  implicit val jobWrites = new Writes[Job] {
    def writes(o: Job) = Json.obj(
      "id" -> o.id.longValue(),
      "name" -> o.name
    )
  }

  implicit val testCaseWrites = new Writes[TestCase] {
    def writes(o: TestCase) = Json.obj(
      "id" -> o.id.longValue(),
      "name" -> o.name
    )
  }

  implicit val listJobWrites = new Writes[java.util.List[Job]] {
    def writes(list: util.List[models.Job]) = JsArray(list.map(elem => Json.toJson(elem)))
  }

  implicit val listTCWrites = new Writes[java.util.List[TestCase]] {
    def writes(list: util.List[models.TestCase]) = JsArray(list.map(elem => Json.toJson(elem)))
  }

  implicit val listWrites = new Writes[java.util.List[TestSuite]] {
    def writes(list: util.List[models.TestSuite]) = JsArray(list.map(elem => Json.toJson(elem)))
  }

  implicit val listRnWrites = new Writes[java.util.List[TestRun]] {
    def writes(list: util.List[models.TestRun]) = JsArray(list.map(elem => Json.toJson(elem)))
  }

  implicit val listSuiteRunWrites = new Writes[java.util.List[SuiteRun]] {
    def writes(list: util.List[models.SuiteRun]) = JsArray(list.map(elem => Json.toJson(elem)))
  }


  def step1(groupId: Long) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else
        Ok(batchReporting.step1(testGroup))
    }
  }

  def step2(groupId: Long) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else
        Ok(batchReporting.step2(testGroup))
    }
  }

  def step3(groupId: Long) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else
        Ok(batchReporting.step3(testGroup))
    }
  }

  def step4(groupId: Long) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else
        Ok(batchReporting.step4(testGroup))
    }
  }

  def previewReport(reportId: Long) = JavaAction {
    implicit request => {
      val report = ReportTemplate.byId(reportId)
      if (report == null)
        BadRequest(s"No such group with id $reportId")
      else
        Ok(new String(utils.Util.gunzip(report.html))).as("text/html")
    }
  }

  def listTestSuites(groupId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else {
        Ok(Json.obj(
          "count" -> TestSuite.countByGroup(testGroup),
          "content" -> Json.toJson(TestSuite.findByGroup(testGroup, pageIndex, pageSize))))
      }
    }
  }

  def listSuiteRuns(testSuiteId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      val suite = TestSuite.findById(testSuiteId)
      if (suite == null)
        BadRequest(s"No such testsuite with id $testSuiteId")
      else
        Ok(Json.obj("count" -> SuiteRun.countBySuite(suite),
          "content" -> Json.toJson(SuiteRun.findBySuite(suite, pageIndex, pageSize))))
    }
  }

  def listTestRuns(suiteRunId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      val suiteRun = SuiteRun.findById(suiteRunId)
      if (suiteRun == null)
        BadRequest(s"No such suiteRun with id $suiteRunId")
      else
        Ok(Json.obj("count" -> TestRun.countBySuiteRun(suiteRun),
          "content" -> Json.toJson(TestRun.findBySuiteRun(suiteRun, pageIndex, pageSize))))
    }
  }


  def listTestCases(testGroupId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(testGroupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $testGroupId")
      else
        Ok(Json.obj("count" -> TestCase.countByGroup(testGroup),
          "content" -> Json.toJson(TestCase.findByGroup(testGroup, pageIndex, pageSize))))
    }
  }


  def listJobs(testCaseId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {

      var testCase = TestCase.findById(testCaseId)

      if (testCase == null)
        BadRequest(s"No such test case with id $testCaseId")
      else
        Ok(Json.obj("count" -> Job.countByTestCase(testCase),
          "content" -> Json.toJson(Job.findByTestCase(testCase, pageIndex, pageSize))))
    }
  }


  def listJobTestRuns(jobId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      var job = Job.findById(jobId)
      if (job == null)
        BadRequest(s"No job with id $jobId found")
      else
        Ok(Json.obj("count" -> TestRun.countByJob(job),
          "content" -> Json.toJson(TestRun.findByJob(job, pageIndex, pageSize))))
    }
  }

  def getReportParameters1(batchReportId: Long, reportId: Long) = JavaAction {
    implicit request => {

      val params = new util.HashSet[String]()

      if (batchReportId != -1)
        params.addAll(getParameters(ReportTemplate.byId(batchReportId)))

      if (reportId != -1)
        params.addAll(getParameters(ReportTemplate.byId(reportId)))


      Ok(JsArray(params.map(k => Json.obj("name" -> k)).toSeq))
    }
  }

  def getReportParameters2(reportId: Long) = JavaAction {
    implicit request => {
      val params = getParameters(ReportTemplate.byId(reportId));
      Ok(JsArray(params.map(k => Json.obj("name" -> k)).toSeq))
    }
  }


  def generateReport() = JavaAction(parse.json) {
    implicit request => {
      val json = request.body

      Logger debug (json.toString())

      val selectedBatchReportId = (json \ "selectedBatchReportId").as[Long]
      val selectedSingleReportId = (json \ "selectedSingleReportId").as[Long]
      val groupId = (json \ "groupId").as[Long]

      var fileName = s"report_$groupId.pdf"


      //flatten all the test runs

      //we have either SuiteRuns, or TestRuns.
      var testRunSet = new util.HashSet[TestRun]()

      val stsss = (json \ "customParameterList")
      val customParameterListJson = stsss.get.as[Map[String, String]]

      try {
        val srs = ((json \ "reportItems") \ "SuiteRun")
        val asopt = srs.asOpt[Map[String, String]]
        asopt.map(suiteRun => {
          suiteRun.map(kvp =>
            testRunSet.addAll(TestRun.findBySuiteRunId(kvp._1.toLong))
          )
        })

      }
      catch {
        case th: Throwable => {
          th.printStackTrace()
        }
      }

      try {
        val srs = (json \ "reportItems" \ "TestRun")
        val asopt = srs.asOpt[Map[String, String]]

        asopt.map(testRunMap => {
          testRunMap.map(kvp =>
            testRunSet.add(TestRun.findById(kvp._1.toLong))
          )
        })

      }
      catch {
        case th: Throwable => {
          th.printStackTrace()
        }
      }


      val reportId = System.currentTimeMillis()

      temporaryReports.put(reportId, fileName -> ReportUtils.toPdf(groupId, selectedBatchReportId, selectedSingleReportId, testRunSet, customParameterListJson))

      Ok(java.lang.Long.toString(reportId))
    }
  }

  def downloadReport(temporaryReportId: Long) = JavaAction {
    implicit request => {

      if (temporaryReports.containsKey(temporaryReportId)) {
        val report = temporaryReports.remove(temporaryReportId)
        Ok(report._2).withHeaders("Content-disposition" -> s"attachment; filename=${
          report._1
        }").as("application/x-download")
      } else {
        BadRequest("No report found")
      }
    }
  }


  val pattern = Pattern.compile("\\$\\{(\\w|_)+(\\w|\\d|_)*}")

  def getParameters(reportTemplate: ReportTemplate): util.Set[String] = {
    val html = new String(Util.gunzip(reportTemplate.html))
    var list = new util.HashSet[String]()

    val matcher = pattern.matcher(html)

    while (matcher.find()) {
      val group = matcher.group()
      if (!isReserved(group))
        list.add(matcher.group().replaceAll("\\$|\\{|\\}", ""))
    }

    list
  }

  def isReserved(keyword: String) = Util.keywords.contains(keyword)


}
