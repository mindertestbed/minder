package rest.controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.Utils;
import com.yerlibilgin.dependencyutils.DependencyClassLoaderCache;
import editormodels.GroupEditorModel;
import utils.Util;
import models.*;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.JsonNodeStructure;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for test group related operations.
 *
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 10/11/15.
 */
public class RestTestGroupController extends Controller {
  public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * This method receives JSON or XML request and returns all test groups defined in db.
   *
   * The sample produced response by Minder (with the status code 200in the header):
   * <p>
   * JSON
   * =====
   *{
   *    "restTestGroups":[
   *        {
   *            "id":"1",
   *            "groupName":"Showcase",
   *            "shortDescription":"Showcase",
   *            "description":"A sample test group that contains tests that *demonstate minder kapabilities",
   *            "owner":"Tester",
   *            "dependencyString":null,
   *            "testassertions":null
   *        }
   *    ]
   *}
   *
   * <p>
   * No input is necessary.
   */
  public Result listTestGroups() {
    RestTestGroupList restTestGroupListResponse = new RestTestGroupList();

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
    List<TestGroup> testGroupList = TestGroup.findAll();
    restTestGroupListResponse.setRestTestGroups(new ArrayList<RestTestGroup>());

    for (TestGroup testGroup : testGroupList) {
      RestTestGroup rt = new RestTestGroup();
      rt.setId(String.valueOf(testGroup.id));
      rt.setGroupName(testGroup.name);
      rt.setShortDescription(testGroup.shortDescription);
      rt.setDependencyString(testGroup.dependencyString);
      rt.setDescription(testGroup.description);
      rt.setOwner(testGroup.owner.name);

      restTestGroupListResponse.getRestTestGroups().add(rt);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestTestGroupList.class.getName(), restTestGroupListResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes test group id and returns detailed testgroup info.
   * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   *
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"id":"1","groupName":"Showcase","shortDescription":"Showcase",
   * "description":"A sample test group that contains tests that demonstate minder
   * capabilities","owner":"tester@minder","dependencyString":null}
   * <p>
   * Group id is required.
   *
   */
  public Result getTestGroup() {
    RestTestGroup responseRestTestGroup = new RestTestGroup();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestTestGroup restTestGroup = null;
    try {
      restTestGroup = (RestTestGroup) contentProcessor.parseRequest(RestTestGroup.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestGroup.getId())
      return badRequest("Please provide Test Group ID");


    //getting the  test group
    TestGroup tg = TestGroup.findById(Long.parseLong(restTestGroup.getId()));
    if (tg == null)
      return badRequest("Test group with id [" + restTestGroup.getId() + "] not found!");

    //
    responseRestTestGroup.setId(String.valueOf(tg.id));
    responseRestTestGroup.setGroupName(tg.name);
    responseRestTestGroup.setShortDescription(tg.shortDescription);
    responseRestTestGroup.setDescription(tg.description);
    responseRestTestGroup.setOwner(tg.owner.email);
    responseRestTestGroup.setDependencyString(tg.dependencyString);

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestTestGroup.class.getName(), responseRestTestGroup);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }
    System.out.println("responseValue:" + responseValue);

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes test group information and deletes the test group.
   * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   *
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Test group deleted!"}
   * <p>
   * Group id is required.
   */
  public Result deleteTestGroup() {
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

    RestTestGroup restTestGroup = null;
    try {
      restTestGroup = (RestTestGroup) contentProcessor.parseRequest(RestTestGroup.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestGroup.getId())
      return badRequest("Please provide Test Group ID");


    //Creating the new test group
    TestGroup tg = TestGroup.findById(Long.parseLong(restTestGroup.getId()));
    if (tg == null)
      return badRequest("Test group with id [" + restTestGroup.getId() + "] not found!");


    if (!Util.canAccess(user, tg.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      tg.delete();
    } catch (Exception ex) {
      return internalServerError("An error occurred during test group delete: " + ex.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Test group deleted!");

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
   * This method receives JSON or XML request which includes test group information and edits the test group.
   * The XSD for the client request is given in rest/models/xsd/resttestgroup.xsd
   * <p>
   * The sample JSON request:
   * {"id":"1","groupName":"Validation","shortDescription":"content","description":"A sample test group that contain"}
   *
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Test group edited!"}
   * <p>
   * id is required and cannot be null for the request.
   * For a test group, group name and  short description are required. If you want to
   * edit these fields, please send a not null value in the request. If you do not want to edit these fields, simply not to mention their tags
   * in the request will make you keep their current values in DB.
   * <p>
   * The field description is optional. If you do not want to set any value to it, simply not mention
   * its tag in the request.
   */
  public Result editTestGroup() {
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

    RestTestGroup restTestGroup = null;
    try {
      restTestGroup = (RestTestGroup) contentProcessor.parseRequest(RestTestGroup.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestGroup.getId())
      return badRequest("Please provide an Id");


    //Editing the new test group
    TestGroup tg = TestGroup.findById(Long.parseLong(restTestGroup.getId()));
    if (tg == null)
      return badRequest("Test group with id [" + restTestGroup.getId() + "] not found!");


    if (!Util.canAccess(user, tg.owner))
      return badRequest("You don't have permission to modify this resource");

    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(tg, restTestGroup);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      tg.update();
    } catch (Exception e) {
      return internalServerError("An error occurred during test group update: " + e.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Test group edited!");

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

  public Result addTestGroup() {
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

    RestTestGroup restTestGroup = null;
    try {
      restTestGroup = (RestTestGroup) contentProcessor.parseRequest(RestTestGroup.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTestGroup.getGroupName())
      return badRequest("Please provide Test Group Name");
    if (null == restTestGroup.getShortDescription())
      return badRequest("Please provide short description");


    //Creating the new test group
    TestGroup group = TestGroup.findByName(restTestGroup.getGroupName());
    if (group != null) {
      return badRequest("The group with name [" + group.name + "] already exists");
    }

    group = new TestGroup();
    group.owner = user;

    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(group, restTestGroup);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      group.save();
    } catch (Exception e) {
      return internalServerError("An error occurred during test group add: " + e.getMessage());
    }

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


  /**
   * This method receives JSON or XML request which includes groupId and dependecy string, and apply both add/edit/delete
   * operations.
   * The XSD for the client request is given in rest/models/xsd/dependencystring.xsd
   * <p>
   * The sample JSON request:
   * {"groupId":1,"value":"junit:junit:4.12"}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"200 Success"}
   * <p>
   * The format for the maven dependecies:
   * "groupId:artifactId[:extension[:classifier]]:version]]"
   * <p>
   * You may give more than one dependency in the "value" section by seperatin them with "\n".
   * Eg. {"groupId":1,"value":"junit:junit:4.12\ncom.typesafe.play:play-java-ws_2.11:2.4.2"}
   * <p>
   * To delete a dependency string, simple assign the value as "".
   * Eg. {"groupId":1,"value":""}
   *
   * If you do not want to change the current value, please simply do not send this tag in the request.
   */
  public Result editDependency() {
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

    RestDependencyString dependencyString = null;
    try {
      dependencyString = (RestDependencyString) contentProcessor.parseRequest(RestDependencyString.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == dependencyString.getGroupId())
      return badRequest("Please provide Test Group id");
    if (null == dependencyString.getValue())
      return badRequest("Please provide dependencyString value, even it is null String");

    String fieldName = null;
    try {
      fieldName = TestGroup.class.getField("dependencyString").getName();
    } catch (NoSuchFieldException e) {
      internalServerError(e.getCause().toString());
    }

    JsonNodeStructure jsonNodeStructure = new JsonNodeStructure();
    jsonNodeStructure.setId(dependencyString.getGroupId());
    jsonNodeStructure.setField(fieldName);
    jsonNodeStructure.setNewValue(dependencyString.getValue());

    JsonNode minderCommand = Json.toJson(jsonNodeStructure);

    try {
      editGroup(minderCommand, user);
    } catch (ParseException e) {
      return badRequest(e.getCause().toString());
    } catch (NoSuchFieldException e) {
      return badRequest(e.getCause().toString());
    } catch (IllegalAccessException e) {
      return badRequest(e.getCause().toString());
    }

    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription(Constants.RESULT_SUCCESS);

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
   * This method receives JSON or XML request which includes groupId and get dependencyString.
   * <p>
   * <p>
   * The sample JSON request:
   * {"groupId":1}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"org.beybunproject:xoola:1.0.1"}
   * <p>
   * The format for the received maven dependecies:
   * "groupId:artifactId[:extension[:classifier]]:version]]"
   */
  public Result getDependency() {
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

    RestDependencyString dependencyString = null;
    try {
      dependencyString = (RestDependencyString) contentProcessor.parseRequest(RestDependencyString.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }


    Long grId = 0L;
    try {
      grId = Long.parseLong(dependencyString.getGroupId());
    } catch (RuntimeException e) {
      internalServerError(e.getCause().toString());
    }

    TestGroup tg = TestGroup.findById(grId);

    if (tg == null) {
      return badRequest("No group with id " + grId + ".");
    }

    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription(tg.dependencyString);

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

  private void editGroup(JsonNode jsonNode, User user) throws ParseException, NoSuchFieldException, IllegalAccessException {
    try {
      Ebean.beginTransaction();
      Utils.doEditField(GroupEditorModel.class, TestGroup.class, jsonNode, user);

      String field = jsonNode.findPath("field").asText();

      String dependencyString = jsonNode.findPath("newValue").asText();
      if (dependencyString != null)
        dependencyString = dependencyString.trim();
      if (dependencyString == null || dependencyString.length() == 0) {
        return; //do nothing
      } else {
        try {
          DependencyClassLoaderCache.getDependencyClassLoader(dependencyString);
          Ebean.commitTransaction();
        } catch (Exception ex) {
          Logger.error(ex.getMessage(), ex);
          throw new ParseException("There was a problem with the dependency string.<br /> \n" +
              "Please make sure that the dependencies are in format:<br />\n " +
              "groupId:artifactId[:extension[:classifier]]:version]]" + ex.toString(), 0);
        }
      }
    } finally {
      Ebean.endTransaction();
    }

  }

  private void checkAndAssignRequiredFields(TestGroup tg, RestTestGroup restTestGroup) throws IllegalArgumentException {

    //Checking the required fields
    if (null != restTestGroup.getGroupName()) {
      if (restTestGroup.getGroupName().equals("")) {
        throw new IllegalArgumentException("The required field groupName cannot be empty");

      }
      tg.name = restTestGroup.getGroupName();
    }

    if (null != restTestGroup.getShortDescription()) {
      if (restTestGroup.getShortDescription().equals("")) {
        throw new IllegalArgumentException("The required field shortDescription cannot be empty.");

      }
      tg.shortDescription = restTestGroup.getShortDescription();
    }

    //Checking the other editable fields
    if (null != restTestGroup.getDescription()) {
      tg.description = restTestGroup.getDescription();
    }


  }


}
