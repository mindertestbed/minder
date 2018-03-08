package controllers

import java.util
import java.util.regex.Pattern
import javax.inject.Inject

import models._
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import utils.{JavaAction, Util}

import scala.collection.JavaConversions._




object ModelJSONLister {
  val LOGGER = Logger.of(classOf[ModelJSONLister])

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
      "name" -> o.name,
      "owner" -> o.owner.email
    )
  }

  implicit val testCaseWrites = new Writes[TestCase] {
    def writes(o: TestCase) = Json.obj(
      "id" -> o.id.longValue(),
      "name" -> o.name
    )
  }

  implicit val jsWrites = new Writes[JobSchedule] {
    def writes(o: JobSchedule) = Json.obj(
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

  implicit val jsListWrites = new Writes[java.util.List[JobSchedule]] {
    def writes(list: util.List[models.JobSchedule]) = JsArray(list.map(elem => Json.toJson(elem)))
  }
}


/**
  * @author yerlibilgin
  */

class ModelJSONLister @Inject()(implicit authentication: Authentication) extends Controller {

  import ModelJSONLister._

  val temporaryReports = new util.HashMap[Long, (String, Array[Byte])]()

  val pattern = Pattern.compile("\\$\\{(\\w|_)+(\\w|\\d|_)*}")

  def listTestSuites(groupId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else {
        val countSuiteByGroup = TestSuite.countByGroup(testGroup)

        LOGGER.debug(s"Number of test suites for test group: $groupId is $countSuiteByGroup")

        Ok(Json.obj(
          "count" -> countSuiteByGroup,
          "content" -> Json.toJson(TestSuite.findByGroup(testGroup, pageIndex, pageSize))))
      }
    }
  }

  def listSuiteJobs(testSuiteId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {
      val suite = TestSuite.findById(testSuiteId)
      if (suite == null)
        BadRequest(s"No such testsuite with id $testSuiteId")
      else
        Ok(Json.obj("count" -> Job.countBySuite(suite),
          "content" -> Json.toJson(Job.findBySuite(suite, pageIndex, pageSize))))
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


  def listSchedulesJSON(groupId: Long, pageIndex: Int, pageSize: Int) = JavaAction {
    implicit request => {

      var testGroup = TestGroup.findById(groupId)

      if (testGroup == null)
        BadRequest(s"No such test group with id $testGroup")
      else
        Ok(Json.obj("count" -> JobSchedule.countByTestGroup(testGroup),
          "content" -> Json.toJson(JobSchedule.findByTestGroup(testGroup, pageIndex, pageSize))))
    }
  }

  def listReportParameters(batchReportId: Long, reportId: Long) = JavaAction {
    implicit request => {

      val params = new util.HashSet[String]()

      if (batchReportId != -1)
        params.addAll(listReportParameters_(ReportTemplate.byId(batchReportId)))

      if (reportId != -1)
        params.addAll(listReportParameters_(ReportTemplate.byId(reportId)))


      Ok(JsArray(params.map(k => Json.obj("name" -> k)).toSeq))
    }
  }

  def listReportParameters_(reportTemplate: ReportTemplate): util.Set[String] = {
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
