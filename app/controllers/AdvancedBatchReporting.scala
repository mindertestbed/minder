package controllers

import java.util
import javax.inject.Inject

import models._
import play.api.Logger
import play.api.libs.json.{JsArray, Json, Writes}
import play.api.mvc._
import utils.{JavaAction, ReportUtils, Util}
import views.html.advancedReporting._
import scala.collection.JavaConversions._


/**
  * @author yerlibilgin
  */

class AdvancedBatchReporting @Inject()(implicit authentication: Authentication) extends Controller {

  val temporaryReports = new util.HashMap[Long, (String, Array[Byte])]()

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
}
