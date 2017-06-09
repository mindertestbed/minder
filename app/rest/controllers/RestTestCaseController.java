package rest.controllers;

import utils.Util;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for test case related operations.
 *
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 19/11/15.
 */
public class RestTestCaseController extends Controller {

  public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * This method receives JSON or XML request and returns all test cases for a given test assertion id
   * To get the details of test cases please use getTestCase method.
   *
   * The sample JSON request:
   * {"testAssertionId":"1"}
   *
   * The sample produced response by Minder (with the status code 200in the header):
   * <p>
   *{
   *    "restTestCases":[
   *        {
   *            "id":"1",
   *            "testAssertionId":null,
   *            "name":"TestCaseSample1",
   *            "shortDescription":"Books test",
   *            "description":null,
   *            "owner":null,
   *            "tdls":null
   *        }
   *    ]
   *}
   *
   * <p>
   * testAssertionId is mandatory.
   */
  public Result listTestCases() {
    RestTestCaseList restTestCaseList = new RestTestCaseList();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestTestCase restTestCase = null;
    try {
      restTestCase = (RestTestCase) contentProcessor.parseRequest(RestTestCase.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestCase.getTestAssertionId())
      return badRequest("Please provide Test Assertion Id");


    //getting the  test group
    TestAssertion ta = TestAssertion.findById(Long.parseLong(restTestCase.getTestAssertionId()));
    if (ta == null)
      return badRequest("Test assertion with id [" + restTestCase.getTestAssertionId() + "] not found!");


    //Getting all test assertions
    restTestCaseList.setRestTestCases(new ArrayList<RestTestCase>());

    List<TestCase> testCaseList = ta.testCases;
    for (TestCase tc : testCaseList) {
      RestTestCase rtc = new RestTestCase();
      rtc.setId(String.valueOf(tc.id));
      rtc.setName(tc.name);

      restTestCaseList.getRestTestCases().add(rtc);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestTestCaseList.class.getName(), restTestCaseList);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }

  /**
   * This method receives JSON or XML request which includes test case id and returns detailed testcase info.
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   *
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   *{
   *    "id":"1",
   *    "testAssertionId":"SampleXmlValidation",
   *    "name":"TestCaseSample1",
   *    "shortDescription":"Books test",
   *    "description":null,
   *    "owner":"tester@minder",
   *    "tdls":[
   *        {
   *            "id":"1",
   *            "testCaseName":null,
   *            "tdl":null,
   *            "version":"0.0.1",
   *            "creationDate":"2015-11-19T10:23:21.203+02:00"
   *        }
   *    ]
   *}
   * <p>
   * test case id is required.
   * To get the details of TDLs, please use RestTdlController by using tdl ids provided by the list.
   */
  public Result getTestCase() {
    RestTestCase responseRestTestCase = new RestTestCase();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestTestCase restTestCase = null;
    try {
      restTestCase = (RestTestCase) contentProcessor.parseRequest(RestTestCase.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestCase.getId())
      return badRequest("Please provide Test Case id");


    //Getting the test case
    TestCase tc = TestCase.findById(Long.parseLong(restTestCase.getId()));
    if (tc == null)
      return badRequest("Test case with id [" + restTestCase.getId() + "] not found!");

    responseRestTestCase.setId(String.valueOf(tc.id));
    responseRestTestCase.setName(tc.name);
    responseRestTestCase.setOwner(tc.owner.email);
    responseRestTestCase.setTestAssertionId(tc.testAssertion.taId);
    responseRestTestCase.setTdls(new ArrayList<RestTdl>());

    List<Tdl> tdlList = tc.tdls;
    for (Tdl tdl : tdlList) {
      RestTdl restTdl = new RestTdl();
      restTdl.setId(String.valueOf(tdl.id));
      restTdl.setVersion(tdl.version);
      restTdl.setCreationDate(dateFormat.format(tdl.creationDate));

      responseRestTestCase.getTdls().add(restTdl);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestTestCase.class.getName(), responseRestTestCase);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes test case information and creates a new test case.
   * <p>
   * The sample JSON request:
   * {"testAssertionId":"SampleXmlValidation",
   * "name":"newTestCase",
   * "shortDescription":"A sample xml-content-validation test"
   * }
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Test case created!"}
   * <p>
   * name, test assertion id and short description are required, whereas, other fields are optional.
   */

  public Result addTestCase() {
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

    RestTestCase restTestCase = null;
    try {
      restTestCase = (RestTestCase) contentProcessor.parseRequest(RestTestCase.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestCase.getName())
      return badRequest("Please provide Test Case Name");
    if (null == restTestCase.getTestAssertionId())
      return badRequest("Please provide related test assertion id");


    //Creating the new test assertion
    TestCase tc = TestCase.findByName(restTestCase.getName());
    if (tc != null) {
      return badRequest("The test case with name [" + restTestCase.getName() + "] already exists");
    }

    TestAssertion ta = TestAssertion.findByTaId(restTestCase.getTestAssertionId());
    if (ta == null) {
      return badRequest("No test assertion found with id [" + restTestCase.getTestAssertionId() + "]");
    }


    tc = new TestCase();
    tc.owner = user;
    tc.testAssertion = ta;
    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(tc, restTestCase);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      tc.save();
    } catch (Exception e) {
      return internalServerError("An error occurred during test case save: " + e.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Test case created!");

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
   * This method receives JSON or XML request which includes test case information and edits the test case.
   * <p>
   * The sample JSON request:
   * {"id":"4",
   * "shortDescription":"A sample sth"}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Test case edited!"}
   * <p>
   * Group name, test assertion id, short description, normative source,target, predicate and prescription level are required, whereas, other fields are optional.
   */

  public Result editTestCase() {
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

    RestTestCase restTestCase = null;
    try {
      restTestCase = (RestTestCase) contentProcessor.parseRequest(RestTestCase.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestCase.getId())
      return badRequest("Please provide Test Case Id");

    //Editing the  test case
    TestCase tc = TestCase.findById(Long.parseLong(restTestCase.getId()));
    if (tc == null) {
      return badRequest("The test assertion with ID [" + restTestCase.getId() + "] not found");
    }

    if (!Util.canAccess(user, tc.owner))
      return badRequest("You don't have permission to modify this resource");

    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(tc, restTestCase);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      tc.update();
    } catch (Exception e) {
      return internalServerError("An error occurred during update of test case: " + e.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Test case edited!");

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
   * {"id":"4"}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200 in the header):
   * {"result":"SUCCESS","description":"Test case deleted!"}
   * <p>
   * id is required.
   */

  public Result deleteTestCase() {
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

    RestTestCase restTestCase = null;
    try {
      restTestCase = (RestTestCase) contentProcessor.parseRequest(RestTestCase.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestCase.getId())
      return badRequest("Please provide Test Case Id");

    //Deleting the  test case
    if (null == restTestCase.getId())
      return badRequest("Please provide a test case id");

    TestCase tc = TestCase.findById(Long.parseLong(restTestCase.getId()));
    if (tc == null) {
      return badRequest("Test case with id " + restTestCase.getId() + " does not exist.");
    }

    if (!Util.canAccess(user, tc.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      tc.delete();
    } catch (Exception ex) {
      return internalServerError("An error occurred during test case delete: " + ex.getMessage());
    }


    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Test case deleted!");

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


  private void checkAndAssignRequiredFields(TestCase tc, RestTestCase restTestCase) throws IllegalArgumentException {

    //Checking the required fields
    if (null != restTestCase.getName()) {
      if (restTestCase.getName().equals("")) {
        throw new IllegalArgumentException("The required field name cannot be empty");

      }
      tc.name = restTestCase.getName();
    }

  }


}
