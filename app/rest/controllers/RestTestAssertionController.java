package rest.controllers;


import global.Util;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestTestAssertion;
import rest.models.RestTestAssertionList;
import rest.models.RestTestCase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for test assertion related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 18/11/15.
 */
public class RestTestAssertionController extends Controller {
    /**
     * This method receives JSON or XML request and returns all test assertions for a given group id
     * To get the details of test assertions please use getTestAssertion method.
     *
     * The sample JSON request:
     * {"groupId":"1"}
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     *{
     *    "restTestAssertions":[
     *        {
     *            "id":"2",
     *            "groupId":null,
     *            "testAssertionId":"AllSchemaTests",
     *            "normativeSource":null,
     *            "target":null,
     *            "prerequisites":null,
     *            "predicate":null,
     *            "variables":null,
     *            "tag":null,
     *            "description":null,
     *            "shortDescription":"A sample xml-content-validation test",
     *            "prescriptionLevel":null,
     *            "owner":null,
     *            "testcases":null
     *        },
     *        {
     *            "id":"1",
     *            "groupId":null,
     *            "testAssertionId":"SampleXmlValidation",
     *            "normativeSource":null,
     *            "target":null,
     *            "prerequisites":null,
     *            "predicate":null,
     *            "variables":null,
     *            "tag":null,
     *            "description":null,
     *            "shortDescription":"A sample xml-content-validation test",
     *            "prescriptionLevel":null,
     *            "owner":null,
     *            "testcases":null
     *        }
     *    ]
     *}
     *
     * <p>
     * groupId is mandatory.
     */
    public static Result listTestAssertions() {
        RestTestAssertionList restTestAssertionListResponse = new RestTestAssertionList();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE),request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestTestAssertion restTestAssertion = null;
        try {
            restTestAssertion = (RestTestAssertion) contentProcessor.parseRequest(RestTestAssertion.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null==restTestAssertion.getGroupId())
            return badRequest("Please provide a Test Group ID");


        //Getting the  test group
        TestGroup tg = TestGroup.findById(Long.parseLong(restTestAssertion.getGroupId()));
        if (tg == null)
            return badRequest("Test group with id [" + restTestAssertion.getId() + "] not found!");


        //Getting all test assertions
        restTestAssertionListResponse.setRestTestAssertions(new ArrayList<RestTestAssertion>());

        List<TestAssertion> testAssertionList = TestAssertion.findByGroup(tg);
        for(TestAssertion ta : testAssertionList){
            RestTestAssertion rta = new RestTestAssertion();
            rta.setTestAssertionId(ta.taId);
            rta.setId(String.valueOf(ta.id));
            rta.setShortDescription(ta.shortDescription);

            restTestAssertionListResponse.getRestTestAssertions().add(rta);
        }

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestTestAssertionList.class.getName(), restTestAssertionListResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }


    /**
     * This method receives JSON or XML request which includes test assertion id and returns detailed test assertion info
     * with its test cases. To get more detailed test case information please use Test Case services.
     * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
     * <p>
     * The sample JSON request:
     * {"testAssertionId":"SampleXmlValidation"}
     * <p>
     * <p>
     * <p>
     * {
     * "id":"1",
     * "groupName":"Showcase",
     * "testAssertionId":"SampleXmlValidation",
     * "normativeSource":"The system must generate valid book entries",
     * "target":"books xml",
     * "prerequisites":"The XML generator system is connected and ready.",
     * "predicate":"The generated books xml conforms to books.xsd",
     * "variables":"The generated books xml conforms to books.xsd",
     * "tag":null,
     * "description":null,
     * "shortDescription":"A sample xml-content-validation test",
     * "prescriptionLevel":"Mandatory",
     * "owner":"tester@minder",
     * "testcases":[
     * {
     * "id":"1",
     * "testAssertionId":"SampleXmlValidation",
     * "name":"TestCaseSample1",
     * "shortDescription":"Books test",
     * "description":null,
     * "owner":"tester@minder"
     * }
     * ]
     * }
     * <p>
     * Test Assertion id is required.
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

        if (null == restTestAssertion.getId())
            return badRequest("Please provide an ID");


        //Getting the test assertion
        TestAssertion ta = TestAssertion.findById(Long.parseLong(restTestAssertion.getId()));
        if (ta == null) {
            return badRequest("Test assertion with ID [" + restTestAssertion.getId() + "] not found!");

        }

        responseRestTestAssertion.setId(String.valueOf(ta.id));
        responseRestTestAssertion.setGroupId(String.valueOf(ta.testGroup.id));
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
     * This method receives JSON or XML request which includes test assertion information and creates a new test assertion.
     * <p>
     * The sample JSON request:
     * {"groupId":"1",
     * "testAssertionId":"SampleXmlValidation2",
     * "shortDescription":"A sample xml-content-validation test",
     * "normativeSource":"The system must generate valid book entries",
     * "target":"books xml",
     * "predicate":"The generated books xml conforms to books.xsd",
     * "prescriptionLevel":"Mandatory",
     * "prerequisites":"The XML generator system is connected and ready."}
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Test assertion created!"}
     * <p>
     * Group name, test assertion id, short description, normative source,target, predicate and prescription level are required, whereas, other fields are optional.
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

        if (null == restTestAssertion.getGroupId())
            return badRequest("Please provide Test Group Id");
        if (null == restTestAssertion.getTestAssertionId())
            return badRequest("Please provide a unique test assertion id");
        if (null == restTestAssertion.getShortDescription())
            return badRequest("Please provide a short description");
        if (null == restTestAssertion.getNormativeSource())
            return badRequest("Please provide a normative source");
        if (null == restTestAssertion.getTarget())
            return badRequest("Please provide a target");
        if (null == restTestAssertion.getPredicate())
            return badRequest("Please provide a unique test assertion id");
        if (null == restTestAssertion.getPrescriptionLevel())
            return badRequest("Please provide a prescription level: Mandatory, Preffered or Permitted");


        //Creating the new test assertion
        TestAssertion ta = TestAssertion.findByTaId(restTestAssertion.getTestAssertionId());
        if (ta != null) {
            return badRequest("The test assertion with ID [" + ta.taId + "] already exists");
        }

        TestGroup tg = TestGroup.findById(Long.parseLong(restTestAssertion.getGroupId()));

        if (tg == null) {
            return badRequest("No group found with id [" + restTestAssertion.getGroupId() + "]");
        }

        ta = new TestAssertion();
        ta.testGroup = tg;
        //Check the required fields' values.
        try {
            checkAndAssignRequiredFields(ta, restTestAssertion);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }

        PrescriptionLevel prescriptionLevel = null;
        try {
            prescriptionLevel = PrescriptionLevel.valueOf(restTestAssertion.getPrescriptionLevel());
        } catch (IllegalArgumentException e) {
            return badRequest("The given prescription level [" + restTestAssertion.getPrescriptionLevel()
                    + "] is not defined. Please select one of these: Mandatory, Preffered or Permitted");
        } catch (NullPointerException e) {
            return badRequest("The prescription level cannot be null. Please select one of these: Mandatory, Preffered or Permitted");
        }
        ta.prescriptionLevel = prescriptionLevel;
        ta.owner = user;

        try {
            ta.save();
        } catch (Exception e) {
            return internalServerError("An error occurred during save of test assertion: " + e.getMessage());
        }

        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test assertion created!");

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


    /**
     * This method receives JSON or XML request which includes test assertion information and edits the given test assertion.
     * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
     * <p>
     * The sample JSON request:
     * {"id":"1",
     * "shortDescription":"A sample sth"
     * }
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Test assertion updated!"}
     * <p>
     * id is required and cannot be null for the request.
     * For a test assertion testAssertionId,short description, normative source, target,predicate and prescription levels are required. If you want to
     * edit any of these fields, please send a not null value in the request. If you do not want to edit these fields, simply not to mention their tags
     * in the request will make you keep their current values in DB.
     * <p>
     * The fields variables, tag, description and prerequisites are optinal. If you do not want to set any value to them, simply not mention
     * their tags in the request.
     */

    public static Result editTestAssertion() {
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

        if (null == restTestAssertion.getId())
            return badRequest("Please provide an id");

        //Editing the test assertion
        TestAssertion ta = TestAssertion.findById(Long.parseLong(restTestAssertion.getId()));
        if (ta == null) {
            return badRequest("The test assertion with ID [" + restTestAssertion.getTestAssertionId() + "] not found");
        }

        if (!Util.canAccess(user, ta.owner))
            return badRequest("You don't have permission to modify this resource");

        //Check the required fields' values.
        try {
            checkAndAssignRequiredFields(ta, restTestAssertion);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }

        try {
            ta.update();
        } catch (Exception e) {
            return internalServerError("An error occurred during update of test assertion: " + e.getMessage());
        }

        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test assertion updated!");

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

    /**
     * This method receives JSON or XML request which includes test assertion id and deletes the test assertion.
     * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
     * <p>
     * The sample JSON request:
     * {"id":"1"}
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Test assertion deleted!"}
     * <p>
     * id is required.
     */

    public static Result deleteTestAssertion() {
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

        if (null == restTestAssertion.getId())
            return badRequest("Please provide an ID");

        //Deleting the test assertion
        TestAssertion ta = TestAssertion.findById(Long.parseLong(restTestAssertion.getId()));
        if (ta == null) {
            return badRequest("Test assertion with id " + restTestAssertion.getId() + " does not exist.");
        }

        if (!Util.canAccess(user, ta.owner))
            return badRequest("You don't have permission to modify this resource");

        try {
            ta.delete();
        } catch (Exception ex) {
            return internalServerError("An error occurred during test assertion delete: " + ex.getMessage());
        }

        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test assertion deleted!");

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


    private static void checkAndAssignRequiredFields(TestAssertion ta, RestTestAssertion restTestAssertion) throws IllegalArgumentException {

        //Checking the required fields
        if (null != restTestAssertion.getTestAssertionId()) {
            if (restTestAssertion.getTestAssertionId().equals("")) {
                throw new IllegalArgumentException("The required field testAssertionId cannot be empty.");

            }
            ta.taId = restTestAssertion.getTestAssertionId();
        }

        if (null != restTestAssertion.getShortDescription()) {
            if (restTestAssertion.getShortDescription().equals("")) {
                throw new IllegalArgumentException("The required field shortDescription cannot be empty");

            }
            ta.shortDescription = restTestAssertion.getShortDescription();
        }

        if (null != restTestAssertion.getNormativeSource()) {
            if (restTestAssertion.getNormativeSource().equals("")) {
                throw new IllegalArgumentException("The required field normativeSource cannot be empty");
            }
            ta.normativeSource = restTestAssertion.getNormativeSource();
        }

        if (null != restTestAssertion.getTarget()) {
            if (restTestAssertion.getTarget().equals("")) {
                throw new IllegalArgumentException("The required field target cannot be empty");
            }
            ta.target = restTestAssertion.getTarget();
        }

        if (null != restTestAssertion.getPredicate()) {
            if (restTestAssertion.getPredicate().equals("")) {
                throw new IllegalArgumentException("The required field predicate cannot be empty.");

            }
            ta.predicate = restTestAssertion.getPredicate();
        }

        if (null != restTestAssertion.getPrescriptionLevel()) {
            if (restTestAssertion.getPrescriptionLevel().equals("")) {
                throw new IllegalArgumentException("The required field prescriptionLevel cannot be empty. If you do not want to change the current value, please" +
                        "simply do not send this tag in the request.");

            }

            PrescriptionLevel prescriptionLevel = null;
            try {
                prescriptionLevel = PrescriptionLevel.valueOf(restTestAssertion.getPrescriptionLevel());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The given prescription level [" + restTestAssertion.getPrescriptionLevel()
                        + "] is not defined. Please select one of these: Mandatory, Preffered or Permitted");
            }
            ta.prescriptionLevel = prescriptionLevel;
        }


        //Checking the other editable fields
        if (null != restTestAssertion.getPrerequisites()) {
            ta.prerequisites = restTestAssertion.getPrerequisites();
        }

        if (null != restTestAssertion.getVariables()) {
            ta.variables = restTestAssertion.getVariables();
        }

        if (null != restTestAssertion.getTag()) {
            ta.tag = restTestAssertion.getTag();
        }

        if (null != restTestAssertion.getDescription()) {
            ta.description = restTestAssertion.getDescription();
        }

    }

}
