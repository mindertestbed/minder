package rest.controllers;

import com.avaje.ebean.Ebean;
import utils.Util;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for job related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 03/12/15.
 */
public class RestJobController extends Controller {
  /**
   * This method receives JSON or XML request and returns all jobs defined in db.
   *
   * The sample produced response by Minder (with the status code 200in the header):
   * <p>
   * JSON
   * =====
   *{
   *    "restJobs":[
   *        {
   *            "id":"1",
   *            "name":"TestCaseSample1(1)",
   *            "tdlId":"1",
   *            "owner":"tester@minder",
   *            "mtdlParameters":null,
   *            "parametersForAdapters":null
   *        },
   *        {
   *            "id":"3",
   *            "name":"SampleJob_1",
   *            "tdlId":"3",
   *            "owner":"tester@minder",
   *            "mtdlParameters":null,
   *            "parametersForAdapters":null
   *        }
   *    ]
   *}
   *
   * <p>
   * No input is necessary.
   */
  public Result listJobs() {
    RestJobList restJobListResponse = new RestJobList();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE));
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    //Getting all adapters
    List<Job> jobList = Job.findAll();
    restJobListResponse.setRestJobs(new ArrayList<RestJob>());

    for (Job job : jobList) {
      RestJob restJob = new RestJob();
      restJob.setId(String.valueOf(job.id));
      restJob.setName(job.name);
      restJob.setOwner(job.owner.email);
      restJob.setTdlId(String.valueOf(job.tdl.id));

      restJobListResponse.getRestJobs().add(restJob);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestJobList.class.getName(), restJobListResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes job id and returns detailed job info.
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   *
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   *{
   *    "id":"6",
   *    "name":"TestCaseSample2",
   *    "tdlId":"4",
   *    "owner":"tester@minder",
   *    "mtdlParameters":"",
   *    "parametersForAdapters":[
   *        {
   *            "id":"4",
   *            "adapterParamId":"4",
   *            "adapterVersionId":"2"
   *        }
   *    ]
   *}
   * Job id is required.
   *
   */
  public Result getJob() {
    RestJob restJobResponse = new RestJob();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestJob restJob = null;
    try {
      restJob = (RestJob) contentProcessor.parseRequest(RestJob.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restJob.getId())
      return badRequest("Please provide a Job ID");


    //Getting the job
    Job job = Job.findById(Long.parseLong(restJob.getId()));
    if (job == null)
      return badRequest("Job with id [" + restJob.getId() + "] not found!");

    restJobResponse.setId(String.valueOf(job.id));
    restJobResponse.setName(job.name);
    restJobResponse.setOwner(job.owner.email);
    restJobResponse.setTdlId(String.valueOf(job.tdl.id));
    restJobResponse.setMtdlParameters(job.mtdlParameters);
    restJobResponse.setParametersForAdapters(new ArrayList<>());

    for (MappedAdapter mappedAdapter : job.mappedAdapters) {
      RestParametersForAdapters restParametersForAdapters = new RestParametersForAdapters();
      restParametersForAdapters.setId(String.valueOf(mappedAdapter.id));
      restParametersForAdapters.setAdapterParamId(String.valueOf(mappedAdapter.parameter.id));
      restParametersForAdapters.setAdapterVersionId(String.valueOf(mappedAdapter.adapterVersion.id));

      restJobResponse.getParametersForAdapters().add(restParametersForAdapters);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestJob.class.getName(), restJobResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes job information and creates a new job.
   * <p>
   * The sample JSON request:
   *{"name":"SampleJob_1",
   * "tdlId":"3",
   * "mtdlParameters":"xsdName:books.xsd \n xmlName:sample-book.xml",
   * "parametersForAdapters":[
   *    {
   *      "adapterParamId":"3",
   *      "adapterVersionId":"2"
   *    }
   *  ]
   *}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Job with Id [2] deleted!"}
   * <p>
   * name, tdlId are required, whereas, other fields are optional. If the tdl has adapter parameters then "parametersForAdapters" is also required.
   */

  public Result createJob() {
    RestMinderResponse minderResponse = new RestMinderResponse();

        /*
        * Parse client request and get user
        */
    String authorizationData = request().getHeader(AUTHORIZATION);
    HashMap<String, String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);
    User user = User.findByEmail(clientRequest.get("username"));
    if (null == user) {
      return unauthorized(Constants.RESULT_UNAUTHORIZED);
    }

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestJob restJob = null;
    try {
      restJob = (RestJob) contentProcessor.parseRequest(RestJob.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restJob.getName())
      return badRequest("Please provide a unique Job Name");
    if (null == restJob.getTdlId())
      return badRequest("Please provide a tdl id");


    //Creating the new job
    Tdl tdl = Tdl.findById(Long.parseLong(restJob.getTdlId()));
    if (tdl == null) {
      return badRequest("No tdl found with id [" + restJob.getTdlId() + "]");
    }
    if (tdl.parameters.size() > 0) {
      if (null == restJob.getParametersForAdapters())
        return badRequest("You have to provide all parameters for used adapters [" + restJob.getTdlId() + "]");
      if (restJob.getParametersForAdapters().size() != tdl.parameters.size())
        return badRequest("You have to fill all parameters for used adapters [" + restJob.getTdlId() + "]");
    }

    AbstractJob job = Job.findByTdlAndName(tdl, restJob.getName());
    if (null != job) {
      return badRequest("The job with name [" + restJob.getName() + "] already exists");
    }

    job = new Job();
    job.owner = user;
    job.tdl = tdl;

    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(job, restJob);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      Ebean.beginTransaction();
      List<MappedAdapter> mappedAdapters = new ArrayList<>();

      List<AdapterParam> tdladapterParams = tdl.parameters;
      for (RestParametersForAdapters paramForAdapters : restJob.getParametersForAdapters()) {
        AdapterParam adapterParam = AdapterParam.findById(Long.parseLong(paramForAdapters.getAdapterParamId()));
        if (null == adapterParam)
          return badRequest("Adapter Param id [" + paramForAdapters.getAdapterParamId() + "] does not exist");

        AdapterVersion adapterVersion = AdapterVersion.findById(Long.parseLong(paramForAdapters.getAdapterVersionId()));
        if (null == adapterVersion)
          return badRequest("Adapter Version id [" + paramForAdapters.getAdapterVersionId() + "] does not exist");

        tdladapterParams.get(0).equals(adapterParam);
        if (!tdladapterParams.contains(adapterParam))
          return badRequest("The provided Adapter Param with id [" + paramForAdapters.getAdapterParamId() + "] is not a valid parameter");

        tdladapterParams.remove(adapterParam);
        MappedAdapter mappedAdapter = new MappedAdapter();
        mappedAdapter.parameter = adapterParam;
        mappedAdapter.adapterVersion = adapterVersion;
        mappedAdapter.job = job;
        mappedAdapter.save();

      }

      if (0 != tdladapterParams.size())
        return badRequest("There are/is [" + tdladapterParams.size() + "] adapter param(s) that you do not provide. Please provide all adapter params.");

      job.save();

      Ebean.commitTransaction();
    } catch (Exception ex) {
      return internalServerError("An error occurred during job save: " + ex.getMessage());
    }


    job = Job.findByName(restJob.getName());
    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Job created with id [" + job.id + "]");

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestMinderResponse.class.getName(), minderResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }

  /**
   * This method receives JSON or XML request which includes test case information and deletes the test case.
   * <p>
   * The sample JSON request:
   * {"id":"2"}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200 in the header):
   * {"result":"SUCCESS","description":"Test case deleted!"}
   * <p>
   * id is required.
   */

  public Result deleteJob() {
    RestMinderResponse minderResponse = new RestMinderResponse();

        /*
        * Parse client request and get user
        */
    String authorizationData = request().getHeader(AUTHORIZATION);
    HashMap<String, String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);
    User user = User.findByEmail(clientRequest.get("username"));
    if (null == user) {
      return unauthorized(Constants.RESULT_UNAUTHORIZED);
    }

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestJob restJob = null;
    try {
      restJob = (RestJob) contentProcessor.parseRequest(RestJob.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restJob.getId())
      return badRequest("Please provide a job ID");

    //Deleting the  test case
    Job job = Job.findById(Long.parseLong(restJob.getId()));
    if (job == null) {
      return badRequest("Job with id " + restJob.getId() + " does not exist.");
    }
    if (!Util.canAccess(user, job.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      job.delete();
    } catch (Exception ex) {
      return internalServerError("An error occurred during test case delete: " + ex.getMessage());
    }


    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Job with Id [" + restJob.getId() + "] deleted!");

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestMinderResponse.class.getName(), minderResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }

  public Result runJob() {
    return null;
  }

  private void checkAndAssignRequiredFields(AbstractJob job, RestJob restJob) {
    //Checking the required fields
    if (null != restJob.getName()) {
      if (restJob.getName().equals("")) {
        throw new IllegalArgumentException("The required field name cannot be empty");

      }
      job.name = restJob.getName();
    }

    //Checking the other editable fields
    if (null != restJob.getMtdlParameters()) {
      job.mtdlParameters = restJob.getMtdlParameters();
    }
  }
}
