package rest.controllers;


import models.TestAssertion;
import models.TestCase;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestTestAssertion;
import rest.models.RestTestCase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for test assertion related operations.
 *
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 18/11/15.
 */
public class RestTestAssertionController extends Controller {
    /**
     * This method receives JSON or XML request which includes test assertion id and returns detailed test assertion info
     * with its test cases. To get more detailed test case information please use Test Case services.
     * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
     * <p>
     * The sample JSON request:
     * {"testAssertionId":"SampleXmlValidation"}
     *
     * <p>
     * <p>
     *{
     *    "id":"1",
     *    "groupName":"Showcase",
     *    "testAssertionId":"SampleXmlValidation",
     *    "normativeSource":"The system must generate valid book entries",
     *    "target":"books xml",
     *    "prerequisites":"The XML generator system is connected and ready.",
     *    "predicate":"The generated books xml conforms to books.xsd",
     *    "variables":"The generated books xml conforms to books.xsd",
     *    "tag":null,
     *    "description":null,
     *    "shortDescription":"A sample xml-content-validation test",
     *    "prescriptionLevel":"Mandatory",
     *    "owner":"tester@minder",
     *    "testcases":[
     *        {
     *            "id":"1",
     *            "testAssertionId":"SampleXmlValidation",
     *            "name":"TestCaseSample1",
     *            "shortDescription":"Books test",
     *            "description":null,
     *            "owner":"tester@minder"
     *        }
     *    ]
     *}
     * <p>
     * Test Assertion id is required.
     *
     */
    public static Result getTestAssertion() {
        RestTestAssertion responseRestTestAssertion = new RestTestAssertion();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestTestAssertion restTestAssertion = null;
        try {
            restTestAssertion = (RestTestAssertion) contentProcessor.parseRequest(RestTestAssertion.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null==restTestAssertion.getTestAssertionId())
            return badRequest("Please provide Test Assertion ID");


        //Getting the test assertion
        TestAssertion ta = TestAssertion.findByTaId(restTestAssertion.getTestAssertionId());
        if (ta == null) {
            return badRequest("Test assertion with taId [" + restTestAssertion.getTestAssertionId() + "] not found!");

        }

        responseRestTestAssertion.setId(String.valueOf(ta.id));
        responseRestTestAssertion.setGroupName(ta.testGroup.name);
        responseRestTestAssertion.setTestAssertionId(ta.taId);
        responseRestTestAssertion.setNormativeSource(ta.normativeSource);
        responseRestTestAssertion.setTarget(ta.target);
        responseRestTestAssertion.setPrerequisites(ta.prerequisites);
        responseRestTestAssertion.setPredicate(ta.predicate);
        responseRestTestAssertion.setVariables(ta.predicate);
        responseRestTestAssertion.setTag(ta.tag);
        responseRestTestAssertion.setDescription(ta.description);
        responseRestTestAssertion.setShortDescription(ta.shortDescription);
        responseRestTestAssertion.setPrescriptionLevel(ta.prescriptionLevel.toString());
        responseRestTestAssertion.setOwner(ta.owner.email);
        responseRestTestAssertion.setTestcases(new ArrayList<RestTestCase>());

        List<TestCase> testCaseList = ta.testCases;
        for (TestCase tc : testCaseList) {
            RestTestCase restTestCase = new RestTestCase();
            restTestCase.setId(String.valueOf(tc.id));
            restTestCase.setTestAssertionId(tc.testAssertion.taId);
            restTestCase.setName(tc.name);
            restTestCase.setShortDescription(tc.shortDescription);
            restTestCase.setDescription(tc.description);
            restTestCase.setOwner(tc.owner.email);

            responseRestTestAssertion.getTestcases().add(restTestCase);
        }

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestTestAssertion.class.getName(), responseRestTestAssertion);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }

    /**
     * This method receives JSON or XML request which includes test group information and creates a new test group.
     * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
     *
     * The sample JSON request:
     * {"groupName":"ValidationTests","shortDescription":"content validation","description":"A sample test group that contains tests that demonstrate minder capabilities"}
     *
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Test group created!"}
     *
     * Group name and short description is required, whereas, description is optional.
     *
     */

    public static Result addTestAssertion() {
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

        RestTestAssertion restTestAssertion = null;
        try {
            restTestAssertion = (RestTestAssertion) contentProcessor.parseRequest(RestTestAssertion.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null==restTestAssertion.getGroupName())
            return badRequest("Please provide Test Group Name");
        if (null==restTestAssertion.getTestAssertionId())
            return badRequest("Please provide a unique test assertion id");
        if (null==restTestAssertion.getShortDescription())
            return badRequest("Please provide a short description");
        if (null==restTestAssertion.getNormativeSource())
            return badRequest("Please provide a normative source");
        if (null==restTestAssertion.getTarget())
            return badRequest("Please provide a target");
        if (null==restTestAssertion.getPredicate())
            return badRequest("Please provide a unique test assertion id");
        if (null==restTestAssertion.getPrescriptionLevel())
            return badRequest("Please provide a prescription level: Mandatory, Preffered or Permitted");


        //Creating the new test group
        /*TestGroup group = TestGroup.findByName(restTestGroup.getGroupName());
        if (group != null) {
            return badRequest("The group with name [" + group.name + "] already exists");
        }

        group = new TestGroup();
        group.owner = user;
        group.shortDescription = restTestGroup.getShortDescription();
        group.description = restTestGroup.getDescription();
        group.name = restTestGroup.getGroupName();
        group.dependencyString = "";

        group.save();
*/
        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test group created!");

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestMinderResponse.class.getName(), minderResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
        System.out.println("responseValue:" + responseValue);

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }

}
