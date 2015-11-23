package rest.controllers;

import models.TestGroup;
import models.UtilClass;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestUtilClass;
import rest.models.RestUtilClassList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Minder server side REST service for util class related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 23/11/15.
 */
public class RestUtilClassController extends Controller {

    /**
     * This method receives JSON or XML request and returns all util classes for a given group id
     * To get the details of a util class please use getUtilClass method.
     *
     * The sample JSON request:
     * {"groupId":"1"}
     *
     * The sample produced response by Minder (with the status code 200 in the header):
     * <p>
     *{
     *    "restUtilClasses":[
     *        {
     *            "id":"3",
     *            "groupId":null,
     *            "name":"SampleUtilClass",
     *            "shortDescription":"1231312313131",
     *            "source":null,
     *            "ownerName":"Tester"
     *        }
     *    ]
     *}
     *
     * <p>
     * groupId is mandatory.
     */
    public static Result listUtilClasses() {
        RestUtilClassList restUtilClassListResponse = new RestUtilClassList();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestUtilClass restUtilClass = null;
        try {
            restUtilClass = (RestUtilClass) contentProcessor.parseRequest(RestUtilClass.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null==restUtilClass.getGroupId())
            return badRequest("Please provide a Test Group ID");


        //Getting the  test group
        TestGroup tg = TestGroup.findById(Long.parseLong(restUtilClass.getGroupId()));
        if (tg == null)
            return badRequest("Test group with id [" + restUtilClass.getGroupId() + "] not found!");


        //Getting all test assertions
        restUtilClassListResponse.setRestUtilClasses(new ArrayList<RestUtilClass>());

        List<UtilClass> utilClassList = UtilClass.findByGroup(tg);
        for(UtilClass uc : utilClassList){
            RestUtilClass ruc = new RestUtilClass();
            ruc.setId(String.valueOf(uc.id));
            ruc.setName(uc.name);
            ruc.setOwnerName(uc.owner.name);
            ruc.setShortDescription(uc.shortDescription);

            restUtilClassListResponse.getRestUtilClasses().add(ruc);
        }

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestUtilClassList.class.getName(), restUtilClassListResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }

}
