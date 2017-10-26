package controllers

import javax.inject.Inject

import models.{JobSchedule, TestGroup}
import play.api.mvc.Controller
import utils.JavaAction

/**
  * @author Yerlibilgin
  */
class Scheduling @Inject()(implicit authentication: Authentication) extends Controller {
  def renderMain(groupId: Long) = JavaAction {
    val group = TestGroup.findById(groupId)
    if (group == null) {
      BadRequest(s"No such group with id $group")
    } else {
      Ok(views.html.group.childViews.schedules(group))
    }
  }

  def listScheduledJobs(groupId: Long) = JavaAction {
    Ok(views.html.group.childViews.schedules(TestGroup.findById(groupId)))
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

    if(schedule.owner.id == authentication.getLocalUser.id) {
      if (schedule != null) {
        schedule.delete()
      }
      Ok
    } else {
      BadRequest(s"You are not the owner of the schedule ${schedule.id}")
    }
  }

  def viewSchedule(scheduledJobId: Long) = JavaAction {
    Ok
  }
}
