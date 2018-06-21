package controllers

import javax.inject.Inject

import controllers.common.FieldUpdateDto
import editormodels.ScheduleEditorModel
import models.{Job, JobSchedule, TestGroup}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc.Controller
import utils.JavaAction

/**
  * @author Yerlibilgin
  */
class Scheduling @Inject()(implicit authentication: Authentication) extends Controller {

  val scheduleForm = Form(
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText(minLength = 3),
      "groupId" -> longNumber,
      "cronExpression" -> nonEmptyText
    )(ScheduleEditorModel.apply)(ScheduleEditorModel.unapply)
  )

  def listScheduledJobs(groupId: Long) = JavaAction {
    val group = TestGroup.findById(groupId)
    if (group == null) {
      BadRequest(s"No such group with id $group")
    } else {
      Ok(views.html.jobSchedules.schedulesFragment(group))
    }
  }

  def viewSchedule(scheduledJobId: Long) = JavaAction {
    val schedule = JobSchedule.findById(scheduledJobId)

    if (schedule == null) {
      BadRequest(s"No such job schedule with id $scheduledJobId")
    } else {
      Ok(views.html.jobSchedules.viewSchedule(schedule))
    }
  }

  def addScheduledJob(groupId: Long) = JavaAction {
    Ok(views.html.jobSchedules.createSchedule(scheduleForm.fill(ScheduleEditorModel(0, "NAME", groupId, "0 * * * *"))))
  }

  def doAddScheduledJob() = JavaAction { implicit request =>

    val form = scheduleForm.bindFromRequest()

    if (form.hasErrors || form.hasGlobalErrors) {
      BadRequest(views.html.jobSchedules.createSchedule(form))
    } else {
      val value = form.get
      try {
        val jobSchedule = new JobSchedule
        jobSchedule.testGroup = TestGroup.findById(value.groupId)
        jobSchedule.name = value.name
        jobSchedule.cronExpression = value.cronExpression
        jobSchedule.save();
        Redirect(controllers.routes.GroupController.getGroupDetailView(value.groupId, "sched"))
      } catch {
        case th: Throwable => {
          BadRequest(views.html.jobSchedules.createSchedule(form.withError("Persistence Error", s"Cannot save job schedule ${th.getMessage}")))
        }
      }
    }
  }

  def deleteScheduledJob(scheduledJobId: Long) = JavaAction {
    val schedule = JobSchedule.findById(scheduledJobId);

    if (schedule.owner.id == authentication.getLocalUser.id) {
      if (schedule != null) {
        schedule.delete()
      }
      Ok
    } else {
      BadRequest(s"You are not the owner of the schedule ${schedule.id}")
    }
  }

  def removeJobFromSchedule(scheduledJobId: Long, jobId: Long) = JavaAction {
    val schedule = JobSchedule.findById(scheduledJobId)
    if (schedule == null) {
      BadRequest(s"No such job schedule with id $scheduledJobId")
    } else {
      val job = Job.findById(jobId)
      schedule.jobs.remove(job)
      schedule.save()
      Ok
    }
  }

  def deleteNextJob(scheduleId: Long) = JavaAction {
    val schedule = JobSchedule.findById(scheduleId)
    if (schedule == null) {
      BadRequest(s"No such job schedule with id $scheduleId")
    } else {
      schedule.nextJob = null
      schedule.save()
      Ok(views.html.jobSchedules.util.nextJobSchedule(schedule))
    }
  }

  def setNextJob(scheduleId: Long, nextId: Long) = JavaAction {
    if (scheduleId == nextId) {
      BadRequest(s"Please do not set next schedule to itself")
    } else {
      val schedule = JobSchedule.findById(scheduleId)

      if (schedule == null) {
        BadRequest(s"No such job schedule with id $scheduleId")
      } else {
        var nextJob = JobSchedule.findById(nextId)

        if (nextJob == null) {
          BadRequest(s"No such job schedule with id $scheduleId")
        } else {
          schedule.nextJob = nextJob
          schedule.save()
          Ok(views.html.jobSchedules.util.nextJobSchedule(schedule))
        }
      }
    }
  }

  //@AllowedRoles(Array(Role.TEST_DESIGNER))
  def doEditScheduleField = JavaAction(parse.json) { implicit request =>
    val jsonNode = request.body
    val dto = new FieldUpdateDto
    dto.id = (jsonNode \ "id").as[Long]
    dto.field = (jsonNode \\ "field").mkString
    dto.newValue = (jsonNode \ "newValue").as[String]
    dto.converter = (jsonNode \\ "converter").mkString

    val jobSchedule = JobSchedule.findById(dto.id)

    if (jobSchedule == null) {
      BadRequest(s"A job schedule with id ${dto.id} not found")
    } else {
      jobSchedule.name = dto.newValue;
      jobSchedule.save()
      Ok(Json.obj("value" -> dto.newValue)).as("application/json")
    }
  }

  def renderCandidateJobList(scheduleId: Long) = JavaAction { implicit request =>
    Ok(views.html.jobSchedules.viewScheduleFragments.candidates.candidateJobList(JobSchedule.findById(scheduleId)))
  }

  def renderCandidateSuiteJobList(scheduleId: Long) = JavaAction { implicit request =>
    Ok(views.html.jobSchedules.viewScheduleFragments.candidates.candidateSuiteJobList(JobSchedule.findById(scheduleId)))
  }

  def renderCandidateSuiteList(scheduleId: Long) = JavaAction { implicit request =>
    Ok(views.html.jobSchedules.viewScheduleFragments.candidates.candidateSuiteList(JobSchedule.findById(scheduleId)))
  }

  def addTestSuiteToSchedule(scheduleId: Long, testSuiteId: Long) = JavaAction { implicit request =>
    Ok
  }

  def addJobToSchedule(scheduleId: Long, jobId: Long) = JavaAction { implicit request =>
    Ok
  }

}
