package controllers;

/**
 * Created by edonafasllija on 17/02/16.
 */

import editormodels.JobTemplateEditorModel;
import models.JobTemplate;
import models.TestGroup;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import utils.Util;

import javax.inject.Inject;


public class JobTemplateController extends Controller {
  Authentication authentication;
  public final Form<JobTemplateEditorModel> JOB_TEMPLATE_FORM;

  @Inject
  public JobTemplateController(FormFactory formFactory, Authentication authentication) {
    this.authentication = authentication;
    JOB_TEMPLATE_FORM = formFactory.form(JobTemplateEditorModel.class);
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result createJobTemplateForm(Long testGroupId) {
    return ok(views.html.group.JobTemplateEditor.render(JOB_TEMPLATE_FORM, TestGroup.findById(testGroupId), authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doCreateJobTemplate(Long testGroupId) {
    //return ok(views.html.group.JobTemplateEditor.render(JOB_TEMPLATE_FORM, null));

    Form<JobTemplateEditorModel> form = JOB_TEMPLATE_FORM.bindFromRequest();
    TestGroup group = TestGroup.findById(testGroupId);

    if (group == null) {
      return badRequest("Test Group with [" + testGroupId + "] not found");
    }


    if (form.hasErrors()) {
      Util.printFormErrors(form);
      return badRequest(views.html.group.JobTemplateEditor.render(form, group, authentication));
    }


    JobTemplateEditorModel model = form.get();


    JobTemplate jtl = JobTemplate.findByGroup(group, model.name);
    if (jtl != null) {
      form.reject("A Job Template with name [" + jtl.name + "] already exists");
      return badRequest(views.html.group.JobTemplateEditor.render(form, group, authentication));
    }

    jtl = new JobTemplate();
    jtl.testGroup = group;
    jtl.name = model.name;
    jtl.owner = authentication.getLocalUser();
    jtl.mtdlParameters = model.mtdlParameters;

    jtl.save();


    return redirect(routes.GroupController.getGroupDetailView(group.id, "jobTemplates"));
  }
}

