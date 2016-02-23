package controllers;

/**
 * Created by edonafasllija on 17/02/16.
 */

import com.avaje.ebean.Ebean;
import editormodels.*;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.AllowedRoles;
import security.Role;
import views.html.job.*;

import static play.data.Form.form;

import views.html.group.childViews.jobTemplatesList;


public class JobTemplateController extends Controller {


  //public static final Form<JobEditorModel> JOB_TEMPLATE_FORM = form(JobEditorModel.class);
  public static final Form<JobTemplateEditorModel> JOB_TEMPLATE_FORM = form(JobTemplateEditorModel.class);

  @AllowedRoles({Role.TEST_DESIGNER})
  public static Result createJobTemplateForm() {
    return ok(views.html.group.JobTemplateEditor.render(JOB_TEMPLATE_FORM, null));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public static Result doCreateJobTemplate(Long testGroupId) {
    //return ok(views.html.group.JobTemplateEditor.render(JOB_TEMPLATE_FORM, null));

    final Form<JobTemplateEditorModel> filledForm = JOB_TEMPLATE_FORM.bindFromRequest();

    TestGroup group = TestGroup.findById(testGroupId);

    if (group == null) {
      return badRequest("Test Group with [" + testGroupId + "] not found");
    }

    User localUser = Authentication.getLocalUser();
    if (localUser.email.equals("root@minder") || group.owner.email.equals(localUser.email)) {

      if (filledForm.hasErrors()) {
        Util.printFormErrors(filledForm);
        return badRequest(views.html.group.JobTemplateEditor.render(filledForm, null));
      } else {
        JobTemplateEditorModel model = filledForm.get();

        JobTemplate jtl = JobTemplate.findByGroup(group, model.name);
        if (jtl != null) {
          filledForm.reject("A Job Template with name [" + jtl.name + "] already exists");
          return badRequest(views.html.group.JobTemplateEditor.render(filledForm, null));
        }

        jtl = new JobTemplate();
        jtl.testGroup = group;
        jtl.name = model.name;
        jtl.owner = localUser;
        jtl.mtdlParameters = model.mtdlParameters;

        jtl.save();

        return ok(jobTemplatesList.render(group));
      }
    } else {
      return badRequest("You can't use this resource");
    }


  }
}

