package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.yerlibilgin.ValueChecker;
import controllers.common.Utils;
import editormodels.JobEditorModel;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import minderengine.Visibility;
import models.AbstractJob;
import models.AdapterParam;
import models.EndPointIdentifier;
import models.Job;
import models.MappedAdapter;
import models.ReportTemplate;
import models.Tdl;
import models.TestCase;
import models.TestRun;
import models.User;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import utils.ReportUtils;
import utils.Util;
import views.html.job.jobEditor;
import views.html.job.testRunDetailView;
import views.html.job.testRunLister;
import views.html.job.visibilityTagFragment;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class JobController extends Controller {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JobController.class);

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
    LOGGER.debug("Create job editor view for tdl " + tdlId);
    Tdl tdl = Tdl.findById(tdlId);

    if (tdl == null) {
      LOGGER.warn("No such tdl " + tdlId);
      return badRequest("No TDL definition found with id " + tdl);
    }

    tdl.testCase = TestCase.findById(tdl.testCase.id);

    LOGGER.debug("IS tdl an http endpoint " + tdl.isHttpEndpoint);
    int max = 0;

    List<Job> list = Job.findByTdl(tdl);
    if (list != null) {
      for (Job job : list) {
        if (job.name.matches(tdl.testCase.name + "\\(\\d+\\)$")) {
          int val = Integer.parseInt(job.name.substring(
              job.name.lastIndexOf('(') + 1,
              job.name.lastIndexOf(')')));

          if (max < val) {
            max = val;
          }
        }
      }
    }

    JobEditorModel model = new JobEditorModel();
    model.tdlID = tdlId;
    model.name = tdl.testCase.name + "(" + (max + 1) + ")";
    model.visibility = Visibility.PROTECTED;
    model.mtdlParameters = "";

    //if the tdl is an http endpoint, then we need to force the
    //user to enter the names of identifiers
    if (tdl.isHttpEndpoint) {
      List<EndPointIdentifier> identifiers = EndPointIdentifier.listByTdl(tdl);

      for (EndPointIdentifier identifier : identifiers) {
        model.mtdlParameters += identifier.identifier + "=/sample/path\n";
      }
    }

    //
    initAdapterListForModel(tdl, model);

    return ok(jobEditor.render(JOB_FORM.fill(model), authentication));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  private void initAdapterListForModel(Tdl tdl, JobEditorModel model) {
    model.adapterMappingList = new ArrayList<>();

    for (AdapterParam parameter : tdl.parameters) {
      model.adapterMappingList.add(new MappedAdapterModel(null, parameter, null));
    }

    Collections.sort(model.adapterMappingList, new Comparator<MappedAdapterModel>() {
      @Override
      public int compare(MappedAdapterModel o1, MappedAdapterModel o2) {
        return o1.adapterParam.name.compareTo(o2.adapterParam.name);
      }
    });
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doCreateJob() {
    LOGGER.debug("Do create job");
    Form<JobEditorModel> form = JOB_FORM.bindFromRequest();

    if (form.hasErrors()) {
      Util.printFormErrors(form);
      return badRequest(jobEditor.render(form, authentication));
    }

    try {
      JobEditorModel model = form.get();

      // check if we have a repetition
      Tdl tdl = Tdl.findById(model.tdlID);
      Job existing = Job.findByTdlAndName(tdl, model.name);

      if (existing != null) {
        form.reject("A Job with the name [" + model.name + "] already exists");
        return badRequest(jobEditor.render(form, authentication));
      }

      // check the parameters.
      if (model.adapterMappingList != null) {
        for (MappedAdapterModel mappedAdapter : model.adapterMappingList) {
          if (mappedAdapter.adapterVersion == null) {
            form.reject("You have to fill all parameters");
            return badRequest(jobEditor.render(form, authentication));
          }
        }
      } else {
        if (tdl.parameters.size() > 0) {
          initAdapterListForModel(tdl, model);
          form.reject("You have to fill all parameters");
          return badRequest(jobEditor.render(form, authentication));
        }
      }

      //if the http endpoint identifiers for this tdl exist, they have to be mapped
      //in the mtdl parameter list
      String firstHttpEndpoint = null;
      List<EndPointIdentifier> tdlEndPointIdentifiers = EndPointIdentifier.listByTdl(tdl);

      if (!tdlEndPointIdentifiers.isEmpty()) {
        LOGGER.debug("The tdl has endpoints. Validate the values from mtdl");
        Properties properties = convertMtdlParametersToProperties(model.mtdlParameters);

        //make sure that all endpoint identifiers have been assigned realy http endpoints
        //TODO: validate URLs
        LOGGER.debug("Verify that all the endpoints are assigned");
        if (!mtdlParameterListValid(properties, tdlEndPointIdentifiers)) {
          LOGGER.error("Some endpoints have not been mapped");
          form.reject("mtdlParameters", "All endpoint identifiers have to be mapped.");
          Util.printFormErrors(form);
          return badRequest(jobEditor.render(form, authentication));
        }

        //if this tdl is an httpendpoint, then its first endpoint should be free to be mapped to a new job
        //i.e. another job must not have occupied the http endpoint previously
        if (tdl.isHttpEndpoint) {
          ValueChecker.notEmpty(tdlEndPointIdentifiers, "tdlEndPointIdentifiers");
          firstHttpEndpoint = tdlEndPointIdentifiers.get(0).method + ":" + properties.getProperty(tdlEndPointIdentifiers.get(0).identifier);
          LOGGER.debug("This is an http endpoint tdl. Make sure that " + firstHttpEndpoint + " hasn't already been used");
          //check if this is an http endpoint and if the first identifier doesn't have a duplicate mapping
          AbstractJob alreadyMappedJob = AbstractJob.findByEndpoint(firstHttpEndpoint);
          if (alreadyMappedJob != null) {
            LOGGER.error("The job " + alreadyMappedJob.id + " has already the mapping " + firstHttpEndpoint);
            form.reject("mtdlParameters", "The first identifier is already mapped to job [" +  //
                alreadyMappedJob.tdl.testCase.testAssertion.testGroup.name + "/" + //
                alreadyMappedJob.tdl.testCase.testAssertion.taId + "/" +  //
                alreadyMappedJob.tdl.testCase.name + ":" + alreadyMappedJob.tdl.version + "/" +
                alreadyMappedJob.name + ":" + alreadyMappedJob.id + "]");

            Util.printFormErrors(form);
            return badRequest(jobEditor.render(form, authentication));
          }
        }
      }

      LOGGER.debug("All good save job");
      // everything is tip-top. So save
      Job job = new Job();
      job.name = model.name;
      job.httpEndpoint = firstHttpEndpoint;
      job.tdl = tdl;
      job.mtdlParameters = model.mtdlParameters;
      job.visibility = model.visibility;
      job.owner = authentication.getLocalUser();
      job.reportTemplate = ReportTemplate.byId(model.reportTemplate);

      try {
        Ebean.beginTransaction();
        if (model.adapterMappingList != null) {
          for (MappedAdapterModel mappedAdapterModel : model.adapterMappingList) {
            MappedAdapter mw = new MappedAdapter();
            mw.parameter = mappedAdapterModel.adapterParam;
            mw.adapterVersion = mappedAdapterModel.adapterVersion;
            mw.job = job;
            mw.save();
          }
        }

        job.save();

        Ebean.commitTransaction();
      } catch (Exception ex) {
        ex.printStackTrace();
        Ebean.endTransaction();

        form.reject(ex.getMessage());
        return internalServerError(jobEditor.render(form, authentication));
      }

      return redirect(routes.TestCaseController.viewTestCase(tdl.testCase.id, "jobs"));
    } catch (IllegalArgumentException ex) {
      form.reject("Invalid request " + ex.getMessage());
      Util.printFormErrors(form);
      return badRequest(jobEditor.render(form, authentication));
    } catch (Exception ex) {
      form.reject("Server error " + ex.getMessage());
      Util.printFormErrors(form);
      return internalServerError(jobEditor.render(form, authentication));
    }
  }

  /**
   * Check whether all the endpoint identifiers of this tdl are listed
   * in the paremeter list and are valid
   */
  private boolean mtdlParameterListValid(Properties mtdlParameters, List<EndPointIdentifier> endPointIdentifiers) {
    for (EndPointIdentifier endPointIdentifier : endPointIdentifiers) {
      final String value = mtdlParameters.getProperty(endPointIdentifier.identifier);
      if (Strings.isNullOrEmpty(value)) {
        return false;
      } else if (!value.startsWith("/")) {
        LOGGER.error("endpoint must start with /");
        throw new IllegalArgumentException("Endpoints must start with /");
      }
    }

    return true;
  }

  private Properties convertMtdlParametersToProperties(String mtdlParameters) {
    Properties properties = new Properties();
    try {
      properties.load(new StringReader(mtdlParameters));
    } catch (IOException e) {
      throw new IllegalArgumentException("Couldn't parce mtdl parameters");
    }
    return properties;
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doDeleteJob(Long id) {
    Job rc = Job.findById(id);
    if (rc == null) {
      // it does not exist. error
      return badRequest("Test assertion with id " + id
          + " does not exist.");
    }

    if (!Util.canAccess(authentication.getLocalUser(), rc.owner)) {
      return unauthorized("You don't have permission to modify this resource");
    }

    try {
      rc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.error(ex.getMessage(), ex);
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

    if (Util.canAccess(localUser, rc.owner, rc.visibility)) {
      return ok(views.html.job.jobDetailView.render(rc, showHistory, localUser, authentication));
    } else {
      return unauthorized("You can't see this resource");
    }
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
    if (tr == null) {
      return badRequest("A test run with id " + testRunId + " was not found");
    }

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
    if (tr == null) {
      return badRequest("A test run with id " + id + " was not found");
    }

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
    if (job == null) {
      return badRequest("A job with id " + id + " was not found");
    }

    LOGGER.debug(visibility);
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

    LOGGER.debug("Edit node " + jsonNode);

    String fieldName = jsonNode.findPath("field").asText();

    if (fieldName.equals("mtdlParameters")) {

      Long jobId = jsonNode.findPath("id").asLong();
      String newValue = jsonNode.findPath("newValue").asText();

      Tdl tdl = AbstractJob.findById(jobId).tdl;
      //if the http endpoint identifiers for this tdl exist, they have to be mapped
      //in the mtdl parameter list
      String decoratedHttpEndpoint = null;
      List<EndPointIdentifier> tdlEndpointIdentifiers = EndPointIdentifier.listByTdl(tdl);
      if (tdlEndpointIdentifiers.size() > 0) {
        LOGGER.debug("The tdl has endpoints. Validate the values from mtdl");
        Properties properties = convertMtdlParametersToProperties(newValue);

        LOGGER.debug("Verify that all the endpoints are assigned");
        try {
          if (!mtdlParameterListValid(properties, tdlEndpointIdentifiers)) {
            LOGGER.error("Some endpoints have not been mapped");
            return badRequest("Some endpoints have not been mapped");
          }
        } catch (Exception ex) {
          LOGGER.error(ex.getMessage(), ex);
          return badRequest(ex.getMessage());
        }

        //if this tdl is an httpendpoint, then its first endpoint should be free to be mapped to a new job
        //i.e. another job must not have occupied the http endpoint previously
        if (tdl.isHttpEndpoint) {
          LOGGER.debug("This is an http endpoint tdl. Make sure that " + decoratedHttpEndpoint + " hasn't already been used");
          ValueChecker.notEmpty(tdlEndpointIdentifiers, "tdlEndpointIdentifiers");
          decoratedHttpEndpoint =
              tdlEndpointIdentifiers.get(0).method + ":/" + properties.getProperty(tdlEndpointIdentifiers.get(0).identifier);
          //check if this is an http endpoint and if the first identifier doesn't have a duplicate mapping
          AbstractJob alreadyMappedJob = AbstractJob.findByEndpoint(decoratedHttpEndpoint);
          if (alreadyMappedJob != null && alreadyMappedJob.id != jobId) {
            final String errorText = "The first http endpoint " + decoratedHttpEndpoint + " is already mapped to job [" +  //
                alreadyMappedJob.tdl.testCase.testAssertion.testGroup.name + "/" + //
                alreadyMappedJob.tdl.testCase.testAssertion.taId + "/" +  //
                alreadyMappedJob.tdl.testCase.name + ":" + alreadyMappedJob.tdl.version + "/" +
                alreadyMappedJob.name + ":" + alreadyMappedJob.id + "]";
            LOGGER.error(errorText);

            return badRequest(errorText);
          }
          AbstractJob job = AbstractJob.findById(jobId);
          job.httpEndpoint = decoratedHttpEndpoint;
        }
      }
    }
    return Utils.doEditField(JobEditorModel.class, Job.class, jsonNode, authentication.getLocalUser());
  }

}
