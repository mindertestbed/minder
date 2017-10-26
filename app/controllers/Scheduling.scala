package controllers

import javax.inject.Inject

import models.{Job, JobSchedule, TestGroup}
import play.api.mvc.Controller
import utils.JavaAction

/**
  * @author Yerlibilgin
  */
class Scheduling @Inject()(implicit authentication: Authentication) extends Controller {
  def listScheduledJobs(groupId: Long) = JavaAction {
    val group = TestGroup.findById(groupId)
    if (group == null) {
      BadRequest(s"No such group with id $group")
    } else {
      Ok(views.html.group.childViews.schedules(group))
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
    Ok
  }

  def doAddScheduledJob() = JavaAction {
    Ok
  }

  def editScheduledJob(scheduledJobId: Long) = JavaAction {
    Ok
  }

  def doEditScheduledJob() = JavaAction {
    Ok
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

}
