package controllers;

/**
 * Created by yerlibilgin
 */

import models.ReportTemplate;
import models.TestGroup;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import scala.io.Source;
import security.AllowedRoles;
import security.Role;
import utils.Util;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class ReportTemplateController extends Controller {

  private final FormFactory formFactory;
  Authentication authentication;
  static String batchTemplate;
  static String singleTemplate;


  public final Form<ReportTemplateEditor> FORM;

  @Inject
  public ReportTemplateController(FormFactory formFactory, Authentication authentication) {
    this.formFactory = formFactory;
    this.authentication = authentication;

    FORM = formFactory.form(ReportTemplateEditor.class);

    if (batchTemplate == null) {
      InputStream defaultTemplateStream = this.getClass().getResourceAsStream("/reportTemplate.html");
      batchTemplate = Source.fromInputStream(defaultTemplateStream, "utf-8").mkString();
      try {
        defaultTemplateStream.close();
      } catch (IOException e) {
      }
      defaultTemplateStream = this.getClass().getResourceAsStream("/singleTestRunTemplate.html");
      singleTemplate = Source.fromInputStream(defaultTemplateStream, "utf-8").mkString();
      try {
        defaultTemplateStream.close();
      } catch (IOException e) {
      }
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result deleteReportTemplate(long reportTemplateId) {
    ReportTemplate byId = ReportTemplate.byId(reportTemplateId);
    if (byId != null) {
      byId.delete();
    }

    return ok();
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doCreateReportTemplate() {
    Form<ReportTemplateEditor> form = FORM.bindFromRequest(request());

    if (form.hasErrors()) {
      return badRequest(views.html.reportTemplates.createReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }
    ReportTemplateEditor rtf = form.get();

    String errorValue = null;

    //form checks
    //1. check if the name group exists
    TestGroup group = TestGroup.findById(rtf.groupId);
    if (group == null) {
      errorValue = "Group with id " + rtf.groupId + " doesn't exist";
      form.reject("groupId", errorValue);
      return badRequest(views.html.reportTemplates.createReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }
    //2. check if the name already exists
    if (ReportTemplate.findByGroup(group, rtf.name) != null) {
      errorValue = "A report template with the same name " + rtf.name + " already exists in this group";
      form.reject("name", errorValue);
      return badRequest(views.html.reportTemplates.createReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }

    if (errorValue == null) {
      //3. try to save the form
      ReportTemplate reportTemplate = new ReportTemplate();
      reportTemplate.name = rtf.name;
      try {
        reportTemplate.html = Util.gzip(rtf.template.getBytes("utf-8"));
      } catch (UnsupportedEncodingException e) {
      }
      reportTemplate.testGroup = group;
      reportTemplate.owner = authentication.getLocalUser();
      reportTemplate.isBatchReport = rtf.isBatchReport;
      reportTemplate.number = reportTemplate.number;
      try {
        reportTemplate.save();
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        errorValue = ex.getMessage();
      }
    }

    if (errorValue != null) {
      form.reject(errorValue);
      Logger.debug("------------------");
      Logger.debug(form.toString());
      Logger.debug("------------------");
      return badRequest(views.html.reportTemplates.createReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }

    return redirect(routes.GroupController.getGroupDetailView(group.id, "reportTemplates"));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doEditReportTemplate() {
    Form<ReportTemplateEditor> form = FORM.bindFromRequest(request());

    if (form.hasErrors()) {
      return badRequest(views.html.reportTemplates.createReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }
    ReportTemplateEditor rtf = form.get();

    String errorValue = null;

    //form checks
    //1. check if the template exists
    ReportTemplate reportTemplate = ReportTemplate.byId(rtf.id);
    if (reportTemplate == null) {
      errorValue = "A report template with id " + rtf.id + " doesn't exist";
      form.reject("id", errorValue);
      return badRequest(views.html.reportTemplates.editReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }
    //2. check if the name already exists
    ReportTemplate found = ReportTemplate.findByGroup(reportTemplate.testGroup, rtf.name);
    if (found != null && found.id != reportTemplate.id) {
      errorValue = "A report template with the same name " + rtf.name + " already exists in this group";
      form.reject("name", errorValue);
      return badRequest(views.html.reportTemplates.editReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }

    if (errorValue == null) {
      //3. try to save the form
      reportTemplate.name = rtf.name;
      try {
        reportTemplate.html = Util.gzip(rtf.template.getBytes("utf-8"));
      } catch (UnsupportedEncodingException e) {
      }
      reportTemplate.isBatchReport = rtf.isBatchReport;
      try {
        reportTemplate.save();
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        errorValue = ex.getMessage();
      }
    }

    if (errorValue != null) {
      form.reject(errorValue);
      Logger.debug("------------------");
      Logger.debug(form.toString());
      Logger.debug("------------------");
      return badRequest(views.html.reportTemplates.editReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
    }

    return redirect(routes.GroupController.getGroupDetailView(reportTemplate.testGroup.id, "reportTemplates"));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result viewReportTemplateView(long id) {
    //FIXME: toooo bad practice using -1 for non registered objects
    if (id == -1) {
      return ok(singleTemplate).as("text/html");
    } else {
      ReportTemplate template = ReportTemplate.byId(id);
      if (template == null) {
        return badRequest("Invalid report template id");
      }
      return ok(views.html.reportTemplates.viewReportTemplateView.render(template, authentication));
    }
  }

  public Result preViewReportTemplateView(long id) {
    //FIXME: toooo bad practice using -1 for non registered objects
    if (id == -1) {
      return ok(singleTemplate).as("text/html");
    } else {
      ReportTemplate template = ReportTemplate.byId(id);
      if (template == null) {
        return badRequest("Invalid report template id");
      }
      return ok(Util.gunzip(template.html)).as("text/html");
    }
  }


  @AllowedRoles({Role.TEST_DESIGNER})
  public Result editReportTemplateView(long reportTemplateId) {
    ReportTemplateEditor rtf = new ReportTemplateEditor();
    ReportTemplate template = ReportTemplate.byId(reportTemplateId);

    if (template == null) {
      return badRequest("No report template with id " + reportTemplateId);
    }

    rtf.id = reportTemplateId;
    rtf.name = template.name;
    rtf.isBatchReport = template.isBatchReport;
    rtf.template = new String(Util.gunzip(template.html));
    rtf.groupId = template.testGroup.id;

    Form<ReportTemplateEditor> form = FORM.fill(rtf);

    return ok(views.html.reportTemplates.editReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result createReportTemplateView(Long testGroupId) {
    ReportTemplateEditor rtf = new ReportTemplateEditor();
    int newNumber = ReportTemplate.getNewNumber(testGroupId);

    rtf.number = newNumber;
    rtf.groupId = testGroupId;
    rtf.name = String.format("ReportTemplate_%1$03d", newNumber);
    rtf.isBatchReport = true;

    Form<ReportTemplateEditor> form = FORM.fill(rtf);

    return ok(views.html.reportTemplates.createReportTemplateView.render(form, batchTemplate, singleTemplate, authentication));
  }
}
