package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.Utils;
import editormodels.JobEditorModel;
import global.ReportUtils;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import views.html.job.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class JobController extends Controller {
  public static final Form<JobEditorModel> JOB_FORM = form(JobEditorModel.class);


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result listTestRuns(Long configurationId) {
    Job rc = Job.findById(configurationId);
    if (rc == null) {
      return badRequest("Job with id [" + configurationId
          + "] not found!");
    } else {

      return ok(testRunLister.render(configurationId));
    }

  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result getCreateJobEditorView(Long tdlId) {
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

    return ok(jobEditor.render(JOB_FORM.fill(model)));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  private static void initWrapperListForModel(Tdl tdl, JobEditorModel model) {
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
  public static Result doCreateJob() {
    Form<JobEditorModel> form = JOB_FORM.bindFromRequest();

    if (form.hasErrors()) {
      Util.printFormErrors(form);
      return badRequest(jobEditor.render(form));
    }

    JobEditorModel model = form.get();

    // check if we have a repetition
    Tdl tdl = Tdl.findById(model.tdlID);
    Job existing = Job.findByTdlAndName(tdl, model.name);

    if (existing != null) {
      form.reject("A Job with the name [" + model.name + "] already exists");
      return badRequest(jobEditor.render(form));
    }

    // check the parameters.
    if (model.wrapperMappingList != null) {
      for (MappedWrapperModel mappedWrapper : model.wrapperMappingList) {
        if (mappedWrapper.wrapperVersion == null) {
          form.reject("You have to fill all parameters");
          return badRequest(jobEditor.render(form));
        }
      }
    } else {
      if (tdl.parameters.size() > 0) {
        initWrapperListForModel(tdl, model);
        form.reject("You have to fill all parameters");
        return badRequest(jobEditor.render(form));
      }
    }

    // everything is tip-top. So save
    Job job = new Job();
    job.name = model.name;
    job.tdl = tdl;
    job.visibility = model.visibility;
    job.owner = Authentication.getLocalUser();

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
      return badRequest(jobEditor.render(form));
    }

    return redirect(routes.TestCaseController.viewTestCase(tdl.testCase.id, "jobs"));
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doDeleteJob(Long id) {
    Job rc = Job.findById(id);
    if (rc == null) {
      // it does not exist. error
      return badRequest("Test assertion with id " + id
          + " does not exist.");
    }


    if (!Util.canAccess(Authentication.getLocalUser(), rc.owner))
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
  public static Result displayJob(Long id, boolean showHistory) {
    Job rc = Job.findById(id);

    final User localUser = Authentication.getLocalUser();

    if (rc == null) {
      return badRequest("A Job with id [" + id + "] was not found");
    }

    if (Util.canAccess(localUser, rc.owner, rc.visibility))
      return ok(views.html.job.jobDetailView.render(rc, showHistory, localUser));
    else
      return unauthorized("You can't see this resource");
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result viewTestRunHistory(Long testRunId) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null) {
      return badRequest("Test Run with id [" + testRunId + "] not found!");
    } else {
      final User localUser = Authentication.getLocalUser();
      if (Util.canAccess(localUser, tr.runner, tr.visibility)) {
        return ok(testRunDetailView.render(tr, null));
      } else {
        return unauthorized("You don't have permission to modify this resource");
      }
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_DEVELOPER, Role.TEST_OBSERVER})
  public static Result viewReport(Long testRunId, String type) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null)
      return badRequest("A test run with id " + testRunId + " was not found");


    System.out.println("Type: " + type);
    final User localUser = Authentication.getLocalUser();
    if (Util.canAccess(localUser, tr.runner, tr.visibility)) {
      response().setContentType("application/x-download");
      String fileName = tr.job.name + "." + tr.number + ".report";
      byte[] data = {};
      if ("pdf".equals(type)) {
        //
        fileName += ".pdf";
        data = ReportUtils.toPdf(tr);
      } else {
        fileName += ".xml";
      }
      response().setHeader("Content-disposition", "attachment; filename=" + fileName);
      return ok(data);
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result changeTestRunVisibility(long id, String visibility) {
    TestRun tr = TestRun.findById(id);
    if (tr == null)
      return badRequest("A test run with id " + id + " was not found");

    System.out.println(visibility);
    final User localUser = Authentication.getLocalUser();
    if (Util.canAccess(localUser, tr.runner, tr.visibility)) {
      tr.visibility = Visibility.valueOf(visibility);
      tr.save();
      return ok(visibilityTagFragment.render(tr.visibility, tr.runner, true, true));
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result changeJobVisibility(long id, String visibility) {
    Job job = Job.findById(id);
    if (job == null)
      return badRequest("A job with id " + id + " was not found");

    System.out.println(visibility);
    final User localUser = Authentication.getLocalUser();
    if (Util.canAccess(localUser, job.owner, job.visibility)) {
      job.visibility = Visibility.valueOf(visibility);
      job.save();
      return ok(visibilityTagFragment.render(job.visibility, job.owner, true, true));
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditJobField() {
    JsonNode jsonNode = request().body().asJson();

    return Utils.doEditField(JobEditorModel.class, Job.class, jsonNode, Authentication.getLocalUser());
  }

}
