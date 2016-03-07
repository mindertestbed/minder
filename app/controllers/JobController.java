package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.Utils;
import editormodels.JobEditorModel;
import global.Util;
import models.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
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
      /*SuiteJob sj = new SuiteJob();
      sj.name = "Ali";
      sj.tdl = tdl;
      sj.owner = job.owner;
      sj.mappedWrappers = mappedWrappers;
      sj.mtdlParameters = model.mtdlParameters;
      sj.testSuite = TestSuite.findById(1L);
      sj.save();*/
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

  @AllowedRoles(Role.TEST_DESIGNER)
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
    jobEditorModel.visibility = job.visibility;
    jobEditorModel.wrapperMappingList = new ArrayList<>();
    jobEditorModel.mtdlParameters = job.mtdlParameters;

    for (MappedWrapper mappedWrapper : job.mappedWrappers) {
      jobEditorModel.wrapperMappingList.add(new MappedWrapperModel(mappedWrapper, mappedWrapper.parameter, mappedWrapper.wrapperVersion));
    }

    Form<?> fill = JOB_FORM.fill(jobEditorModel);

    return ok(views.html.job.jobEditor.render(fill));
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

  /**
   * List the actual registered wrappers that provide the same
   * signal and slots with the parametric wrappers provided in the model
   *
   * @param mappedWrapperModel
   * @return
   */
  @AllowedRoles(Role.TEST_DESIGNER)
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
      byte[] data = tr.report;
      if ("pdf".equals(type)) {
        //
        fileName += ".pdf";
        data = toPdf(data, tr);
      } else {
        fileName += ".xml";
      }
      response().setHeader("Content-disposition", "attachment; filename=" + fileName);
      return ok(data);
    } else {
      return unauthorized("You don't have permission to modify this resource");
    }
  }


  @SuppressWarnings({"DEPRECATION"})
  private static byte[] toPdf(byte[] data, TestRun tr) {
    try {
      JasperReport report = JasperCompileManager.compileReport(WrapperController.class.getResourceAsStream("/taReport.jrxml"));
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("user", tr.history.email);
      values.put("email", tr.history.email);
      values.put("result", tr.success);
      values.put("date", tr.date);
      values.put("errorMessage", new String(tr.errorMessage, "utf-8"));

      Job rc = Job.findById(tr.job.id);
      TestCase tc = TestCase.findById(rc.tdl.testCase.id);
      TestAssertion ta = TestAssertion.findById(tc.testAssertion.id);
      TestGroup tg = TestGroup.findById(ta.testGroup.id);

      values.put("testGroup", tg.name);
      values.put("testCase", tc.name);
      values.put("job", rc.name);

      values.put("taId", ta.taId);
      values.put("taNormativeSource", ta.normativeSource);
      values.put("taDescription", ta.shortDescription);
      values.put("taTarget", ta.target);
      values.put("taPredicate", ta.predicate);
      values.put("taPrerequisite", ta.prerequisites);
      values.put("taPrescription", ta.prescriptionLevel.toString());
      values.put("taVariable", ta.variables);
      values.put("taTag", ta.tag);

      JRDataSource source = new JREmptyDataSource();
      JasperPrint print1 = JasperFillManager.fillReport(report, values, source);
      List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
      jasperPrintList.add(print1);

      if (tr.sutNames != null && tr.sutNames.length() > 0) {
        JasperReport wrapperReport = JasperCompileManager.compileReport(WrapperController.class.getResourceAsStream("/sutNamesReport.jrxml"));
        values = new HashMap<String, Object>();
        values.put("sutNames", tr.sutNames);
        source = new JREmptyDataSource();
        JasperPrint print2 = JasperFillManager.fillReport(wrapperReport, values, source);
        jasperPrintList.add(print2);
      }

      JasperReport logReport = JasperCompileManager.compileReport(WrapperController.class.getResourceAsStream("/logReport.jrxml"));
      values = new HashMap<String, Object>();
      values.put("log", tr.history.extractSystemOutputLog());
      source = new JREmptyDataSource();
      JasperPrint print3 = JasperFillManager.fillReport(logReport, values, source);
      jasperPrintList.add(print3);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JRPdfExporter exporter = new JRPdfExporter();
      //Add the list as a Parameter
      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrintList);
      //this will make a bookmark in the exported PDF for each of the reports
      exporter.setParameter(JRPdfExporterParameter.IS_CREATING_BATCH_MODE_BOOKMARKS, Boolean.TRUE);
      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
      exporter.exportReport();
      return baos.toByteArray();
    } catch (Exception ex) {
      ex.printStackTrace();
      return "Invalid".getBytes();
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
