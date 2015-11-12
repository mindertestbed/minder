package rest.controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.enumeration.Utils;
import dependencyutils.DependencyClassLoaderCache;
import editormodels.GroupEditorModel;
import models.TestGroup;
import models.User;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.JsonNodeStructure;
import rest.controllers.common.RestUtils;
import rest.controllers.response.MinderResponse;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.controllers.testgroup.DependencyString;

import java.text.ParseException;
import java.util.HashMap;

/**
 * This class provides Minder server side REST service for test group related operations.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 10/11/15.
 */
public class TestGroupController extends Controller {

    /**
     * This method receives JSON or XML request which includes groupId and dependecy string, and apply both add/edit/delete
     * operations.
     * The XSD for the client request is given in rest/testgroup/DependencyString.xsd
     *
     * The sample JSON request:
     * {"groupId":1,"value":"junit:junit:4.12"}
     *
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"200 Success"}
     *
     * The format for the maven dependecies:
     * "groupId:artifactId[:extension[:classifier]]:version]]"
     *
     * You may give more than one dependency in the "value" section by seperatin them with "\n".
     * Eg. {"groupId":1,"value":"junit:junit:4.12\ncom.typesafe.play:play-java-ws_2.11:2.4.2"}
     *
     * To delete a dependency string, simple assign the value as "".
     * Eg. {"groupId":1,"value":""}
     */
    public static Result editDependency() {
        MinderResponse minderResponse = new MinderResponse();

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

        DependencyString dependencyString = null;
        try {
            dependencyString = (DependencyString) contentProcessor.parseRequest(DependencyString.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

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
            responseValue = contentProcessor.prepareResponse(MinderResponse.class.getName(), minderResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
        System.out.println("responseValue:" + responseValue);

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }

    /**
     * This method receives JSON or XML request which includes groupId and get dependencyString.
     *
     *
     * The sample JSON request:
     * {"groupId":1}
     *
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"org.beybunproject:xoola:1.0.1"}
     *
     * The format for the received maven dependecies:
     * "groupId:artifactId[:extension[:classifier]]:version]]"
     *
     */
    public static Result getDependency() {
        MinderResponse minderResponse = new MinderResponse();

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

        DependencyString dependencyString = null;
        try {
            dependencyString = (DependencyString) contentProcessor.parseRequest(DependencyString.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }


        Long grId=0L;
        try {
            grId = Long.parseLong(dependencyString.getGroupId());
        }catch (RuntimeException e){
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
            responseValue = contentProcessor.prepareResponse(MinderResponse.class.getName(), minderResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
        System.out.println("responseValue:" + responseValue);

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }

    private static void editGroup(JsonNode jsonNode, User user) throws ParseException, NoSuchFieldException, IllegalAccessException {
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


}
