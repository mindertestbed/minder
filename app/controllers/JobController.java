package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.Utils;
import editormodels.JobEditorModel;
import utils.ReportUtils;
import utils.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import views.html.job.*;

import static play.data.Form.form;

import minderengine.Visibility;

import javax.inject.Inject;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class JobController extends Controller {
  Authentication authentication;
  public final Form<JobEditorModel> JOB_FORM;

  @Inject
  public JobController(FormFactory formFactory, Authentication authentication) {
    this.authentication = authentication;
    JOB_FORM = formFactory.form(JobEditorModel.class);
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public Result listTestRuns(Long configurationId) {
    Job rc = Job.findById(configurationId);
    if (rc == null) {
      return badRequest("Job with id [" + configurationId
          + "] not found!");
    } else {

      return ok(testRunLister.render(configurationId, authentication));
    }

  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result getCreateJobEditorView(Long tdlId) {
    Tdl tdl = Tdl.findById(tdlId);
    tdl.testCase = TestCase.findById(tdl.testCase.id);

    if (tdl == null)
      return badRequest("No TDL definition found with id " + tdl);

    int max = 0;

    List<Job> list = Job.findByTdl(tdl);
    if (list != null) {
      for (Job job : list) {
        if (job.name.matches(tdl.testCase.name + "\\(\\d+\\)$")) {
          int val = Integer.parseInt(job.name.substring(
              job.name.lastIndexOf('(') + 1,
              job.name.lastIndexOf(')')));

          if (max < val)
            max = val;
        }
      }
    }

    JobEditorModel model = new JobEditorModel();
    model.tdlID = tdlId;
    model.name = tdl.testCase.name + "(" + (max + 1) + ")";
    model.mtdlParameters = "";
    model.visibility = Visibility.PROTECTED;

    //
    initWrapperListForModel(tdl, model);

    return ok(jobEditor.render(JOB_FORM.fill(model), authentication));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  private void initWrapperListForModel(Tdl tdl, JobEditorModel model) {
    model.wrapperMappingList = new ArrayList<>();

    for (WrapperParam parameter : tdl.parameters) {
      model.wrapperMappingList.add(new MappedWrapperModel(null, parameter, null));
    }

    Collections.sort(model.wrapperMappingList, new Comparator<MappedWrapperModel>() {
      @Override
      public int compare(MappedWrapperModel o1, MappedWrapperModel o2) {
        return o1.wrapperParam.name.compareTo(o2.wrapperParam.name);
      }
    });
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doCreateJob() {
    Form<JobEditorModel> form = JOB_FORM.bindFromRequest();

    if (form.hasErrors()) {
      Util.printFormErrors(form);
      return badRequest(jobEditor.render(form, authentication));
    }

    JobEditorModel model = form.get();

    // check if we have a repetition
    Tdl tdl = Tdl.findById(model.tdlID);
    Job existing = Job.findByTdlAndName(tdl, model.name);

    if (existing != null) {
      form.reject("A Job with the name [" + model.name + "] already exists");
      return badRequest(jobEditor.render(form, authentication));
    }

    // check the parameters.
    if (model.wrapperMappingList != null) {
      for (MappedWrapperModel mappedWrapper : model.wrapperMappingList) {
        if (mappedWrapper.wrapperVersion == null) {
          form.reject("You have to fill all parameters");
          return badRequest(jobEditor.render(form, authentication));
        }
      }
    } else {
      if (tdl.parameters.size() > 0) {
        initWrapperListForModel(tdl, model);
        form.reject("You have to fill all parameters");
        return badRequest(jobEditor.render(form, authentication));
      }
    }

    // everything is tip-top. So save
    Job job = new Job();
    job.name = model.name;
    job.tdl = tdl;
    job.visibility = model.visibility;
    job.owner = authentication.getLocalUser();

    try {
      Ebean.beginTransaction();
      if (model.wrapperMappingList != null) {
        for (MappedWrapperModel mappedWrapperModel : model.wrapperMappingList) {
          MappedWrapper mw = new MappedWrapper();
          mw.parameter = mappedWrapperModel.wrapperParam;
          mw.wrapperVersion = mappedWrapperModel.wrapperVersion;
          mw.job = job;
          mw.save();
        }
      }
      job.mtdlParameters = model.mtdlParameters;
      job.save();

      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();

      form.reject(ex.getMessage());
      return badRequest(jobEditor.render(form, authentication));
    }

    return redirect(routes.TestCaseController.viewTestCase(tdl.testCase.id, "jobs"));
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doDeleteJob(Long id) {
    Job rc = Job.findById(id);
    if (rc == null) {
      // it does not exist. error
      return badRequest("Test assertion with id " + id
          + " does not exist.");
    }


    if (!Util.canAccess(authentication.getLocalUser(), rc.owner))
      return unauthorized("You don't have permission to modify this resource");


    try {
      rc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    return redirect(routes.TestCaseController.viewTestCase(rc.tdl.testCase.id, "jobs"));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public Result displayJob(Long id, boolean showHistory) {
    Job rc = Job.findById(id);

    final User localUser = authentication.getLocalUser();

    if (rc == null) {
      return badRequest("A Job with id [" + id + "] was not found");
    }

    if (Util.canAccess(localUser, rc.owner, rc.visibility))
      return ok(views.html.job.jobDetailView.render(rc, showHistory, localUser, authentication));
    else
      return unauthorized("You can't see this resource");
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public Result viewTestRunHistory(Long testRunId) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null) {
      return badRequest("Test Run with id [" + testRunId + "] not found!");
    } else {
      final User localUser = authentication.getLocalUser();
      if (Util.canAccess(localUser, tr.runner, tr.visibility)) {
        return ok(testRunDetailView.render(tr, null, authentication));
      } else {
        return unauthorized("You don't have permission to modify this resource");
      }
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public Result viewReport(Long testRunId, String type) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null)
      return badRequest("A test run with id " + testRunId + " was not found");

    final User localUser = authentication.getLocalUser();
    if (Util.canAccess(localUser, tr.runner, tr.visibility)) {
      String fileName = tr.job.name + "." + tr.number + ".report";
      //
      fileName += ".pdf";
      byte[] data = ReportUtils.toPdf(tr);

      response().setHeader("Content-disposition", "attachment; filename=" + fileName);
      return ok(data).as("application/x-download");
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result changeTestRunVisibility(long id, String visibility) {
    TestRun tr = TestRun.findById(id);
    if (tr == null)
      return badRequest("A test run with id " + id + " was not found");

    final User localUser = authentication.getLocalUser();
    if (Util.canAccess(localUser, tr.runner, tr.visibility)) {
      tr.visibility = Visibility.valueOf(visibility);
      tr.save();
      return ok(visibilityTagFragment.render(tr.visibility, tr.runner, true, true, authentication));
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result changeJobVisibility(long id, String visibility) {
    Job job = Job.findById(id);
    if (job == null)
      return badRequest("A job with id " + id + " was not found");

    System.out.println(visibility);
    final User localUser = authentication.getLocalUser();
    if (Util.canAccess(localUser, job.owner, job.visibility)) {
      job.visibility = Visibility.valueOf(visibility);
      job.save();
      return ok(visibilityTagFragment.render(job.visibility, job.owner, true, true, authentication));
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doEditJobField() {
    JsonNode jsonNode = request().body().asJson();

    return Utils.doEditField(JobEditorModel.class, Job.class, jsonNode, authentication.getLocalUser());
  }

}
