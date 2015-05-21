package controllers;

import com.avaje.ebean.Ebean;
import editormodels.JobEditorModel;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.jobDetailView;
import views.html.jobEditor;
import views.html.jobLister;
import views.html.testRunViewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class JobController  extends Controller {
  public static final Form<JobEditorModel> JOB_FORM = form(JobEditorModel.class);

  public static Result getCreateJobEditorView(Long testCaseId) {
    TestCase testCase = TestCase.findById(testCaseId);

    if (testCase == null)
      return badRequest("Test case with id " + testCaseId
          + " couldn't be found");

    int max = 0;

    List<Job> list = Job.findByTestCase(testCase);
    if (list != null) {
      for (Job job : list) {
        if (job.name
            .matches(testCase.name + "\\(\\d+\\)$")) {
          int val = Integer.parseInt(job.name.substring(
              job.name.lastIndexOf('(') + 1,
              job.name.lastIndexOf(')')));

          if (max < val)
            max = val;
        }
      }
    }

    JobEditorModel model = new JobEditorModel();
    model.testCaseId = testCaseId;
    model.name = testCase.name + "(" + (max + 1) + ")";

    //
    initWrapperListForModel(testCase, model);

    return ok(jobEditor.render(JOB_FORM.fill(model), null));
  }

  private static void initWrapperListForModel(TestCase testCase, JobEditorModel model) {
    model.mappedWrappers = new ArrayList<>();

    for (WrapperParam parameter : testCase.parameters) {
      model.mappedWrappers.add(new MappedWrapperModel(null, parameter.id,
          parameter.name, ""));
    }

    Collections.sort(model.mappedWrappers, new Comparator<MappedWrapperModel>() {
      @Override
      public int compare(MappedWrapperModel o1, MappedWrapperModel o2) {
        return o1.name.compareTo(o2.name);
      }
    });
  }

  public static Result doCreateJob() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    Form<JobEditorModel> form = JOB_FORM
        .bindFromRequest();

    if (form.hasErrors()) {
      Util.printFormErrors(form);
      return badRequest(jobEditor.render(form, null));
    }

    JobEditorModel model = form.get();

    // check if we have a repetition
    TestCase testCase = TestCase.findById(model.testCaseId);
    Job existing = Job.findByTestCaseAndName(
        testCase, model.name);

    if (existing != null) {
      form.reject("A Job with the name [" + model.name
          + "] already exists");
      return badRequest(jobEditor.render(form, null));
    }

    // check the parameters.
    if (model.mappedWrappers != null) {
      for (MappedWrapperModel mappedWrapper : model.mappedWrappers) {
        if (mappedWrapper.value == null || mappedWrapper.value.equals("")) {
          form.reject("You have to fill all parameters");
          return badRequest(jobEditor.render(form, null));
        }
      }
    } else {
      System.out.println("Mapped wrappers null");
      if(testCase.parameters.size() > 0){
        initWrapperListForModel(testCase, model);
        form.reject("You have to fill all parameters");
        return badRequest(jobEditor.render(form, null));
      }
    }

    // everything is tip-top. So save
    Job rc = new Job();
    rc.name = model.name;
    rc.testCase = testCase;
    rc.obsolete = false;
    rc.tdl = testCase.tdl;
    rc.owner = Application.getLocalUser(session());

    try {
      Ebean.beginTransaction();
      List<MappedWrapper> mappedWrappers = new ArrayList<>();
      if (model.mappedWrappers != null) {
        for (MappedWrapperModel mappedWrapper : model.mappedWrappers) {
          MappedWrapper mw = new MappedWrapper();
          mw.parameter = WrapperParam.findByTestCaseAndName(testCase,
              mappedWrapper.name);
          mw.job = rc;
          mw.wrapper = Wrapper.findByName(mappedWrapper.value);
          mw.save();
        }
      }

      rc.mappedWrappers = mappedWrappers;
      rc.save();
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();

      form.reject(ex.getMessage());
      return badRequest(jobEditor.render(form, null));
    }

    return redirect(routes.TestCaseController.viewTestCase( testCase.id, true));
  }

  public static Result doDeleteJob(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    Job rc = Job.findById(id);
    if (rc == null) {
      // it does not exist. error
      return badRequest("Test assertion with id " + id
          + " does not exist.");
    }


    if (!Util.canAccess(Application.getLocalUser(session()), rc.owner))
      return badRequest("You don't have permission to modify this resource");


    try {
      rc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    TestCase tc = TestCase.findById(rc.testCase.id);
    return redirect(routes.TestCaseController.viewTestCase(rc.testCase.id, true));
  }

  public static Result getEditJobEditorView(Long id) {
    Job rc = Job.findById(id);
    if (rc == null) {
      // it does not exist. error
      return badRequest("Job with id " + id
          + " does not exist.");
    }


    if (!Util.canAccess(Application.getLocalUser(session()), rc.owner))
      return badRequest("You don't have permission to modify this resource");


    JobEditorModel jobEditorModel = new JobEditorModel();
    jobEditorModel.id = rc.id;
    jobEditorModel.name = rc.name;
    jobEditorModel.tdl = rc.tdl;
    jobEditorModel.testCaseId = rc.testCase.id;
    jobEditorModel.obsolete = rc.obsolete;
    jobEditorModel.mappedWrappers = new ArrayList<>();

    for (MappedWrapper mappedWrapper : rc.mappedWrappers) {
      jobEditorModel.mappedWrappers.add(new MappedWrapperModel(mappedWrapper.id,
          mappedWrapper.parameter.id, mappedWrapper.parameter.name,
          mappedWrapper.wrapper.name));
    }

    Form<?> fill = JOB_FORM.fill(jobEditorModel);

    return ok(jobEditor.render(fill, null));
  }

  public static Result doEditJob() {
    Form<JobEditorModel> form = JOB_FORM
        .bindFromRequest();

    if (form.hasErrors()) {
      return badRequest(jobEditor.render(form, null));
    }

    JobEditorModel model = form.get();

    Job rc = Job.findById(model.id);
    if (rc == null) {
      return badRequest("The Job " + rc.id
          + " is not found.");
    }


    if (!Util.canAccess(Application.getLocalUser(session()), rc.owner))
      return badRequest("You don't have permission to modify this resource");

    rc.name = model.name;
    rc.obsolete = false;
    rc.tdl = rc.testCase.tdl;

    try {
      Ebean.beginTransaction();
      MappedWrapper.deleteByJob(rc);
      List<MappedWrapper> mappedWrappers = new ArrayList<>();
      for (MappedWrapperModel mappedWrapper : model.mappedWrappers) {
        MappedWrapper mw = new MappedWrapper();
        mw.parameter = WrapperParam.findByTestCaseAndName(rc.testCase,
            mappedWrapper.name);
        mw.job = rc;
        mw.wrapper = Wrapper.findByName(mappedWrapper.value);
        mw.save();
      }

      rc.mappedWrappers = mappedWrappers;
      rc.save();
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();

      form.reject(ex.getMessage());
      return badRequest(jobEditor.render(form, null));
    }
    return ok(jobLister.render(rc.testCase, null));
  }

  public static Result displayJob(Long id, boolean showHistory) {
    Job rc = Job.findById(id);

    final User localUser = Application.getLocalUser(session());

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
  public static List<String> listOptions(MappedWrapperModel mappedWrapperModel) {
    // get MappedParam
    // get the wrapperparam
    WrapperParam wp = WrapperParam
        .findById(mappedWrapperModel.wrapperParamId);
    // get signatures supported by this wp.
    List<ParamSignature> psList = ParamSignature.getByWrapperParam(wp);

    // create the return list.
    List<String> listOptions = new ArrayList<>();
    // List<SignalSlot> TdlCompiler.getSignatures(testCase.tdl,
    // mappedWrapperModel.name);
    // we have to list the wrappers that cover all these signatures (might
    // be more but we don't care)
    // not an optiomal solution for a huuuuge database. But there won't be
    // more than 100 wrappers :-)
    List<Wrapper> all = Wrapper.getAll();

    out:
    for (Wrapper wrapper : all) {
      System.out.println("Wrapper " + wrapper.name);
      // check if all the signatures are covered by the signals or slots
      // of this wrapper.
      for (ParamSignature ps : psList) {
        System.out.print("\t" + ps.signature);
        boolean included = false;
        for (TSignal signal : wrapper.signals) {
          if (ps.signature.equals(signal.signature.replaceAll("\\s",
              ""))) {
            included = true;
            break;
          }
        }

        if (!included) {
          for (TSlot slot : wrapper.slots) {
            if (ps.signature.equals(slot.signature.replaceAll(
                "\\s", ""))) {
              included = true;
              break;
            }
          }
        }

        if (included)
          System.out.println(" included");
        else
          System.out.println("NOT included");
        if (!included)
          continue out;
      }

      // if we are here, then this wrapper contains all.
      // so add it to the list.
      listOptions.add(wrapper.name);
    }

    return listOptions;
  }


  public static Result viewTestRunHistory(Long testRunId) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null) {
      return badRequest("Test Run with id [" + testRunId + "] not found!");
    } else {
      return ok(testRunViewer.render(tr, null));
    }
  }


}
