package controllers;

import com.avaje.ebean.Ebean;
import editormodels.JobEditorModel;
import global.Global;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.jobDetailView;
import views.html.jobEditor;
import views.html.testRunViewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class JobController extends Controller {
  public static final Form<JobEditorModel> JOB_FORM = form(JobEditorModel.class);

  @Security.Authenticated(Secured.class)
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

    //
    initWrapperListForModel(tdl, model);

    return ok(jobEditor.render(JOB_FORM.fill(model), null));
  }

  @Security.Authenticated(Secured.class)
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

  @Security.Authenticated(Secured.class)
  public static Result doCreateJob() {
    Form<JobEditorModel> form = JOB_FORM.bindFromRequest();

    if (form.hasErrors()) {
      Util.printFormErrors(form);
      return badRequest(jobEditor.render(form, null));
    }

    JobEditorModel model = form.get();

    // check if we have a repetition
    Tdl tdl = Tdl.findById(model.tdlID);
    Job existing = Job.findByTdlAndName(tdl, model.name);

    if (existing != null) {
      form.reject("A Job with the name [" + model.name + "] already exists");
      return badRequest(jobEditor.render(form, null));
    }

    // check the parameters.
    if (model.wrapperMappingList != null) {
      for (MappedWrapperModel mappedWrapper : model.wrapperMappingList) {
        if (mappedWrapper.wrapperVersion == null) {
          form.reject("You have to fill all parameters");
          return badRequest(jobEditor.render(form, null));
        }
      }
    } else {
      if (tdl.parameters.size() > 0) {
        initWrapperListForModel(tdl, model);
        form.reject("You have to fill all parameters");
        return badRequest(jobEditor.render(form, null));
      }
    }

    // everything is tip-top. So save
    Job job = new Job();
    job.name = model.name;
    job.tdl = tdl;
    job.owner = Authentication.getLocalUser();

    try {
      Ebean.beginTransaction();
      List<MappedWrapper> mappedWrappers = new ArrayList<>();
      if (model.wrapperMappingList != null) {
        for (MappedWrapperModel mappedWrapperModel : model.wrapperMappingList) {
          MappedWrapper mw = new MappedWrapper();
          mw.parameter = mappedWrapperModel.wrapperParam;
          mw.wrapperVersion = mappedWrapperModel.wrapperVersion;
          mw.job = job;
          mw.save();
        }
      }
      job.mappedWrappers = mappedWrappers;
      job.mtdlParameters = model.mtdlParameters;
      job.save();
      SuiteJob sj = new SuiteJob();
      sj.name = "Ali";
      sj.tdl = tdl;
      sj.owner = job.owner;
      sj.mappedWrappers = mappedWrappers;
      sj.mtdlParameters = model.mtdlParameters;
      sj.testSuite = TestSuite.findById(1L);
      sj.save();
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();

      form.reject(ex.getMessage());
      return badRequest(jobEditor.render(form, null));
    }

    return redirect(routes.TestCaseController.viewTestCase(tdl.testCase.id, "jobs"));
  }


  @Security.Authenticated(Secured.class)
  public static Result doDeleteJob(Long id) {
    Job rc = Job.findById(id);
    if (rc == null) {
      // it does not exist. error
      return badRequest("Test assertion with id " + id
          + " does not exist.");
    }


    if (!Util.canAccess(Authentication.getLocalUser(), rc.owner))
      return badRequest("You don't have permission to modify this resource");


    try {
      rc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    return redirect(routes.TestCaseController.viewTestCase(rc.tdl.testCase.id, "jobs"));
  }

  @Security.Authenticated(Secured.class)
  public static Result getEditJobEditorView(Long id) {
    Job job = Job.findById(id);
    if (job == null) {
      // it does not exist. error
      return badRequest("Job with id " + id + " does not exist.");
    }

    if (!Util.canAccess(Authentication.getLocalUser(), job.owner))
      return badRequest("You don't have permission to modify this resource");


    JobEditorModel jobEditorModel = new JobEditorModel();
    jobEditorModel.id = job.id;
    jobEditorModel.name = job.name;
    jobEditorModel.wrapperMappingList = new ArrayList<>();
    jobEditorModel.mtdlParameters = job.mtdlParameters;

    for (MappedWrapper mappedWrapper : job.mappedWrappers) {
      jobEditorModel.wrapperMappingList.add(new MappedWrapperModel(mappedWrapper, mappedWrapper.parameter, mappedWrapper.wrapperVersion));
    }

    Form<?> fill = JOB_FORM.fill(jobEditorModel);

    return ok(jobEditor.render(fill, null));
  }

  @Security.Authenticated(Secured.class)
  public static Result displayJob(Long id, boolean showHistory) {
    Job rc = Job.findById(id);

    final User localUser = Authentication.getLocalUser();

    if (rc == null) {
      return badRequest("A Job with id [" + id
          + "] was not found");
    }

    return ok(jobDetailView.render(rc, showHistory, localUser));
  }

  /**
   * List the actual registered wrappers that provide the same
   * signal and slots with the parametric wrappers provided in the model
   *
   * @param mappedWrapperModel
   * @return
   */
  @Security.Authenticated(Secured.class)
  public static List<WrapperVersion> listFittingWrappers(MappedWrapperModel mappedWrapperModel) {
    // get signatures supported by this wp.
    List<ParamSignature> psList = ParamSignature.getByWrapperParam(mappedWrapperModel.wrapperParam);

    // create the return list.
    List<WrapperVersion> listOptions = new ArrayList<>();
    // List<SignalSlot> TdlCompiler.getSignatures(testCase.tdl,
    // mappedWrapperModel.name);
    // we have to list the wrappers that cover all these signatures (might
    // be more but we don't care)
    // not an optiomal solution for a huuuuge database. But there won't be
    // more than 100 wrappers :-)
    List<Wrapper> all = Wrapper.getAll();

    Logger.debug("List Fitting Wrappers");
    Logger.info("List Fitting Wrappers");

    out:
    for (Wrapper wrapper : all) {
      // check if all the signatures are covered by the signals or slots
      // of this wrapper.
      List<WrapperVersion> wrapperVersions = WrapperVersion.getAllByWrapper(wrapper);
      for (WrapperVersion wrapperVersion : wrapperVersions) {
        Logger.debug("Check " + wrapper.name + "|" + wrapperVersion.version);
        for (ParamSignature ps : psList) {
          boolean included = false;


          Logger.debug("\tLook for " + ps.signature);
          for (TSignal signal : wrapperVersion.signals) {
            if (ps.signature.equals(signal.signature.replaceAll("\\s",
                ""))) {
              included = true;
              Logger.debug("\t\t" + signal.signature + " HIT");
              break;
            }
          }

          if (!included) {
            for (TSlot slot : wrapperVersion.slots) {
              if (ps.signature.equals(slot.signature.replaceAll(
                  "\\s", ""))) {
                included = true;
                Logger.debug("\t\t" + slot.signature + " HIT");
                break;
              }
            }
          }

          if (!included)
            continue out;
        }

        // if we are here, then this wrapper contains all.
        // so add it to the list.
        Logger.debug(wrapper.name + "|" + wrapperVersion.version + " FITS");
        listOptions.add(wrapperVersion);
      }
    }


    return listOptions;
  }

  @Security.Authenticated(Secured.class)
  public static Result viewTestRunHistory(Long testRunId) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null) {
      return badRequest("Test Run with id [" + testRunId + "] not found!");
    } else {
      return ok(testRunViewer.render(tr, null));
    }
  }


}
