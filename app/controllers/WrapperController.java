package controllers;

import editormodels.WrapperEditorModel;
import global.Util;
import models.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class WrapperController extends Controller {
  public static final Form<WrapperEditorModel> WRAPPER_FORM = form(WrapperEditorModel.class);

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


  public static Result listTestRuns(Long configurationId) {
    Job rc = Job.findById(configurationId);
    if (rc == null) {
      return badRequest("Job with id [" + configurationId
          + "] not found!");
    } else {

      return ok(testRunLister.render(configurationId, null));
    }

  }

  public static Result doCreateWrapper() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<WrapperEditorModel> filledForm = WRAPPER_FORM.bindFromRequest();
    final User localUser = Application.getLocalUser(session());
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(wrapperEditor.render(filledForm, null));
    } else {
      WrapperEditorModel model = filledForm.get();

      Wrapper wrapper = Wrapper.findByName(model.name);
      if (wrapper != null) {
        filledForm.reject("The wrapper with name [" + wrapper.name
            + "] already exists");
        return badRequest(wrapperEditor.render(filledForm, null));
      }

      wrapper = new Wrapper();

      wrapper.user = localUser;
      wrapper.name = model.name;
      wrapper.shortDescription = model.shortDescription;

      wrapper.save();

      return ok(wrapperLister.render(Application.getLocalUser(session())));
    }
  }

  public static Result createNewWrapperForm() {
    return ok(wrapperEditor.render(WRAPPER_FORM, null));
  }

  public static Result doDeleteWrapper(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    System.out.println("Wrapper id:" + id);
    Wrapper wr = Wrapper.findById(id);
    if (wr == null) {
      // it does not exist. error
      return badRequest("Wrapper with id " + id + " does not exist.");
    }

    try {
      System.out.println("Wrapper delete");
      wr.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    return ok(wrapperLister.render(Application.getLocalUser(session())));
  }

  public static Result editWrapperForm(Long id) {
    Wrapper wr = Wrapper.find.byId(id);
    if (wr == null) {
      return badRequest("Wrapper with id [" + id + "] not found!");
    } else {
      WrapperEditorModel wrModel = new WrapperEditorModel();
      wrModel.id = id;
      wrModel.name = wr.name;
      wrModel.shortDescription = wr.shortDescription;

      Form<WrapperEditorModel> bind = WRAPPER_FORM
          .fill(wrModel);
      return ok(wrapperEditor2.render(bind, null));
    }
  }

  public static Result doEditWrapper() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    final Form<WrapperEditorModel> filledForm = WRAPPER_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest("");//wrapperEditor2.render(filledForm, null));
    } else {
      WrapperEditorModel model = filledForm.get();
      Wrapper wr = Wrapper.find.byId(model.id);
      wr.shortDescription = model.shortDescription;
      wr.update();

      Logger.info("Done updating wrapper " + model.name);
      return ok(wrapperLister.render(Application
          .getLocalUser(session())));
    }
  }


  public static Result viewReport(Long testRunId, String type) {
    TestRun tr = TestRun.findById(testRunId);
    if (tr == null)
      return badRequest("A test run with id " + testRunId + " was not found");
    response().setContentType("application/x-download");
    String fileName = tr.id + ".report";
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
  }

  private static byte[] toPdf(byte[] data, TestRun tr) {
    try {
      JasperReport report = JasperCompileManager.compileReport(WrapperController.class.getResourceAsStream("/taReport.jrxml"));
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("user", tr.history.email);
      values.put("email", tr.history.email);
      values.put("result", tr.success);
      values.put("date", tr.date);
      values.put("errorMessage", tr.errorMessage);

      Logger.debug("Error message " + tr.errorMessage);
      Job rc = Job.findById(tr.job.id);
      TestCase tc = TestCase.findById(rc.testCase.id);
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

      if (tr.wrappers != null && tr.wrappers.length() > 0) {
        JasperReport wrapperReport = JasperCompileManager.compileReport(WrapperController.class.getResourceAsStream("/wrappersReport.jrxml"));
        values = new HashMap<String, Object>();
        values.put("wrappers", tr.wrappers);
        source = new JREmptyDataSource();
        JasperPrint print2 = JasperFillManager.fillReport(wrapperReport, values, source);
        jasperPrintList.add(print2);
      }

      if (tr.history.systemOutputLog != null && tr.history.systemOutputLog.length() > 0) {
        JasperReport logReport = JasperCompileManager.compileReport(WrapperController.class.getResourceAsStream("/logReport.jrxml"));
        values = new HashMap<String, Object>();
        values.put("log", tr.history.systemOutputLog);
        source = new JREmptyDataSource();
        JasperPrint print3 = JasperFillManager.fillReport(logReport, values, source);
        jasperPrintList.add(print3);
      }

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
}
