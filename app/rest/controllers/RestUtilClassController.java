package rest.controllers;

import utils.Util;
import models.TestGroup;
import models.User;
import models.UtilClass;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestUtilClass;
import rest.models.RestUtilClassList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for util class related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * The field "source" indicates the actual util class code as bytearray. Therefore, one need to provide asset in Base64 notation.
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
  public Result listUtilClasses() {
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

    if (null == restUtilClass.getGroupId())
      return badRequest("Please provide a Test Group ID");


    //Getting the  test group
    TestGroup tg = TestGroup.findById(Long.parseLong(restUtilClass.getGroupId()));
    if (tg == null)
      return badRequest("Test group with id [" + restUtilClass.getGroupId() + "] not found!");


    //Getting all test assertions
    restUtilClassListResponse.setRestUtilClasses(new ArrayList<RestUtilClass>());

    List<UtilClass> utilClassList = UtilClass.findByGroup(tg);
    for (UtilClass uc : utilClassList) {
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

  /**
   * This method receives JSON or XML request which includes util class id and returns detailed util class info
   * <p>
   * The sample JSON request:
   * {"id":"3"}
   * <p>
   * <p>
   * <p>
   *{
   *    "id":"3",
   *    "groupId":"1",
   *    "name":"SampleUtilClass",
   *    "shortDescription":"SampleUtilClass",
   *    "source":"ZGVmIHByZXBhcmVHRVRDb21tYW5kKHNlcnZlckFkZHJlc3M6U3RyaW5nLGlkZW50aWZpZXJTY2hlbWU6U3RyaW5nLGlkOlN0cmluZykgOiBTdHJpbmcgPSB7DQogICBzZXJ2ZXJBZGRyZXNzICsgIi8iICsgaWRlbnRpZmllclNjaGVtZSArICI6OiIraWQ7DQp9DQoNCg==",
   *    "ownerName":"tester@minder"
   *}
   * <p>
   * Util class id is required.
   */
  public Result getUtilClass() {
    RestUtilClass restUtilClassResponse = new RestUtilClass();

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

    if (null == restUtilClass.getId())
      return badRequest("Please provide an ID");


    //Getting the util class
    UtilClass uc = UtilClass.findById(Long.parseLong(restUtilClass.getId()));
    if (uc == null) {
      return badRequest("Util class with ID [" + restUtilClass.getId() + "] not found!");

    }

    restUtilClassResponse.setId(String.valueOf(uc.id));
    restUtilClassResponse.setGroupId(String.valueOf(uc.testGroup.id));
    restUtilClassResponse.setOwnerName(uc.owner.email);
    restUtilClassResponse.setName(uc.name);
    restUtilClassResponse.setShortDescription(uc.name);
    restUtilClassResponse.setSource(uc.source.getBytes());

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestUtilClass.class.getName(), restUtilClassResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes util class information and creates a new util class.
   * <p>
   * The sample JSON request:
   *{
   *    "groupId":"1",
   *    "name":"SampleUtilClass",
   *    "shortDescription":"A sample xml-content-validation test",
   *    "source":"ZGVmIHByZXBhcmVHRVRDb21tYW5kKHNlcnZlckFkZHJlc3M6U3RyaW5nLGlkZW50aWZpZXJTY2hlbWU6U3RyaW5nLGlkOlN0cmluZykgOiBTdHJpbmcgPSB7DQogICBzZXJ2ZXJBZGRyZXNzICsgIi8iICsgaWRlbnRpZmllclNjaGVtZSArICI6OiIraWQ7DQp9DQoNCg=="
   *}
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Util class created!"}
   * <p>
   * group id, a unique name and short description are required, whereas, other "source" is optional.
   */

  public Result addUtilClass() {
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

    RestUtilClass restUtilClass = null;
    try {
      restUtilClass = (RestUtilClass) contentProcessor.parseRequest(RestUtilClass.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restUtilClass.getGroupId())
      return badRequest("Please provide Test Group Id");
    if (null == restUtilClass.getName())
      return badRequest("Please provide a util class name");
    if (null == restUtilClass.getShortDescription())
      return badRequest("Please provide a short description");

    //Creating the new test assertion
    UtilClass uc = UtilClass.findByGroupIdAndName(Long.parseLong(restUtilClass.getGroupId()), restUtilClass.getName());
    if (uc != null) {
      return badRequest("The util class with name [" + restUtilClass.getName() + "] for the test group with id " +
          "[" + restUtilClass.getGroupId() + "] already exists. Please provide another name.");
    }

    TestGroup tg = TestGroup.findById(Long.parseLong(restUtilClass.getGroupId()));
    if (tg == null) {
      return badRequest("No group found with id [" + restUtilClass.getGroupId() + "]");
    }

    uc = new UtilClass();
    uc.testGroup = tg;
    uc.owner = user;
    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(uc, restUtilClass);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      uc.save();
    } catch (Exception e) {
      return internalServerError("An error occurred during save of util class: " + e.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Util class created!");

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
   * This method receives JSON or XML request which includes util class information and edits the given util class.
   * <p>
   * The sample JSON request:
   *{
   *    "id":"4",
   *    "name":"MyUtilClass",
   *    "shortDescription":"A sample sth",
   *    "source":"ZGVmIHByZXBhcmVHRVRDb21tYW5kKHNlcnZlckFkZHJlc3M6U3RyaW5nLGlkZW50aWZpZXJTY2hlbWU6U3RyaW5nLGlkOlN0cmluZykgOiBTdHJpbmcgPSB7DQogICBzZXJ2ZXJBZGRyZXNzICsgIi8iICsgaWRlbnRpZmllclNjaGVtZSArICI6OiIraWQ7DQp9DQoNCg=="
   *}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Util class updated!"}
   * <p>
   * id is required and cannot be null for the request.
   * For a util class name and short description are required. If you want to
   * edit any of these fields, please send a not null value in the request. If you do not want to edit these fields, simply not to mention their tags
   * in the request will make you keep their current values in DB.
   * <p>
   * The field source is optional. If you do not want to set any value to it, simply not mention
   * its tag in the request.
   */

  public Result editUtilClass() {
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

    RestUtilClass restUtilClass = null;
    try {
      restUtilClass = (RestUtilClass) contentProcessor.parseRequest(RestUtilClass.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restUtilClass.getId())
      return badRequest("Please provide an id");

    //Editing the util class
    UtilClass uc = UtilClass.findById(Long.parseLong(restUtilClass.getId()));
    if (uc == null) {
      return badRequest("The util clas with ID [" + restUtilClass.getId() + "] not found");
    }

    if (!Util.canAccess(user, uc.owner))
      return badRequest("You don't have permission to modify this resource");

    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(uc, restUtilClass);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      uc.update();
    } catch (Exception e) {
      return internalServerError("An error occurred during update of util class: " + e.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Util class updated!");

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
   * This method receives JSON or XML request which includes util class id and deletes the util class.
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200 in the header):
   * {"result":"SUCCESS","description":"Util class deleted!"}
   * <p>
   * id is required.
   */

  public Result deleteUtilClass() {
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

    RestUtilClass restUtilClass = null;
    try {
      restUtilClass = (RestUtilClass) contentProcessor.parseRequest(RestUtilClass.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restUtilClass.getId())
      return badRequest("Please provide an ID");

    //Deleting the util class
    UtilClass uc = UtilClass.findById(Long.parseLong(restUtilClass.getId()));
    if (uc == null) {
      return badRequest("Util class with id " + restUtilClass.getId() + " does not exist.");
    }

    if (!Util.canAccess(user, uc.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      uc.delete();
    } catch (Exception ex) {
      return internalServerError("An error occurred during util class delete: " + ex.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Util class deleted!");

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


  private void checkAndAssignRequiredFields(UtilClass uc, RestUtilClass restUtilClass) throws IllegalArgumentException {

    //Checking the required fields
    if (null != restUtilClass.getName()) {
      if (restUtilClass.getName().equals("")) {
        throw new IllegalArgumentException("The required field name cannot be empty.");

      }
      uc.name = restUtilClass.getName();
    }

    if (null != restUtilClass.getShortDescription()) {
      if (restUtilClass.getShortDescription().equals("")) {
        throw new IllegalArgumentException("The required field shortDescription cannot be empty");

      }
      uc.shortDescription = restUtilClass.getShortDescription();
    }

    //Checking the other editable fields
    if (null != restUtilClass.getSource()) {
      uc.source = new String(restUtilClass.getSource());
    }

  }
}
