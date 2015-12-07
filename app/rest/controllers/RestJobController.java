package rest.controllers;

import com.avaje.ebean.Ebean;
import global.Util;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestJob;
import rest.models.RestMinderResponse;
import rest.models.RestParametersForWrappers;

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
     * This method receives JSON or XML request which includes job information and creates a new job.
     * <p>
     * The sample JSON request:
     *{"name":"SampleJob_1",
     * "tdlId":"3",
     * "mtdlParameters":"xsdName:books.xsd \n xmlName:sample-book.xml",
     * "parametersForWrappers":[
     *    {
     *      "wrapperParamId":"3",
     *      "wrapperVersionId":"2"
     *    }
     *  ]
     *}
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Job with Id [2] deleted!"}
     * <p>
     * name, tdlId are required, whereas, other fields are optional. If the tdl has wrapper parameters then "parametersForWrappers" is also required.
     */

    public static Result createJob() {
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
            if (null == restJob.getParametersForWrappers())
                return badRequest("You have to provide all parameters for used wrappers [" + restJob.getTdlId() + "]");
            if (restJob.getParametersForWrappers().size() != tdl.parameters.size())
                return badRequest("You have to fill all parameters for used wrappers [" + restJob.getTdlId() + "]");
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
            List<MappedWrapper> mappedWrappers = new ArrayList<>();

            List<WrapperParam> tdlwrapperParams = tdl.parameters;
            for (RestParametersForWrappers paramForWrappers : restJob.getParametersForWrappers()) {
                WrapperParam wrapperParam = WrapperParam.findById(Long.parseLong(paramForWrappers.getWrapperParamId()));
                if (null == wrapperParam)
                    return badRequest("Wrapper Param id [" + paramForWrappers.getWrapperParamId() + "] does not exist");

                WrapperVersion wrapperVersion = WrapperVersion.findById(Long.parseLong(paramForWrappers.getWrapperVersionId()));
                if (null == wrapperVersion)
                    return badRequest("Wrapper Version id [" + paramForWrappers.getWrapperVersionId() + "] does not exist");

                tdlwrapperParams.get(0).equals(wrapperParam);
                if (!tdlwrapperParams.contains(wrapperParam))
                    return badRequest("The provided Wrapper Param with id [" + paramForWrappers.getWrapperParamId() + "] is not a valid parameter");

                tdlwrapperParams.remove(wrapperParam);
                MappedWrapper mappedWrapper = new MappedWrapper();
                mappedWrapper.parameter = wrapperParam;
                mappedWrapper.wrapperVersion = wrapperVersion;
                mappedWrapper.job = job;
                mappedWrapper.save();

            }

            if (0 != tdlwrapperParams.size())
                return badRequest("There are/is [" + tdlwrapperParams.size() + "] wrapper param(s) that you do not provide. Please provide all wrapper params.");

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

    public static Result deleteJob() {
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
        minderResponse.setDescription("Job with Id ["+restJob.getId() +"] deleted!");

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

    private static void checkAndAssignRequiredFields(AbstractJob job, RestJob restJob) {
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
