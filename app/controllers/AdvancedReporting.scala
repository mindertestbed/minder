package controllers

import javax.inject.Inject

import models.TestGroup
import play.api.mvc._
import utils.JavaAction

/**
  * @author yerlibilgin
  */

class AdvancedReporting @Inject()(implicit authentication: Authentication) extends Controller {

  def renderMain(groupId: Long) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else
        Ok(views.html.advancedReporting.mainView(testGroup))
    }
  }

    def createBatchReport(groupId: Long) = JavaAction {
      implicit request => {
        val testGroup = TestGroup.findById(groupId)
        if (testGroup == null)
          BadRequest(s"No such test group with id $groupId")
        else
          Ok(views.html.advancedReporting.createBatchReport(testGroup))
      }
    }

  def createSingleReport(groupId: Long) = JavaAction {
    implicit request => {
      val testGroup = TestGroup.findById(groupId)
      if (testGroup == null)
        BadRequest(s"No such test group with id $groupId")
      else
        Ok(views.html.advancedReporting.createSingleReport(testGroup))
    }
  }
}
