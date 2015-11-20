package rest.controllers;

import minderengine.MinderWrapperRegistry;
import models.User;
import models.Wrapper;
import models.WrapperVersion;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestWrapper;
import rest.models.RestWrapperList;
import rest.models.RestWrapperVersion;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for wrapper related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 17/11/15.
 */
public class RestWrapperController extends Controller {
    /**
     * This method receives JSON or XML request which includes wrapper version id information.
     * The XSD for the client request is given in rest/models/xsd/restwrapperversion.xsd
     * <p>
     * The sample JSON request:
     * {"id":"1"}
     * <p>
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"id":"1","wrapperName":null,"version":null,"creationDate":null,"available":true}
     * <p>
     * wrapper version id is required. Note that "id" is the identifier for wrapperversion table, not the version itself.
     */
    public static Result getWrapperStatus() {
        RestWrapperVersion restWrapperVersionResponse = new RestWrapperVersion();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestWrapperVersion restWrapperVersion = null;
        try {
            restWrapperVersion = (RestWrapperVersion) contentProcessor.parseRequest(RestWrapperVersion.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restWrapperVersion.getWrapperVersionId())
            return badRequest("Please provide Wrapper id");


        //Checking the wrapper status
        long wrapId = 0L;
        try {
            wrapId = Long.parseLong(restWrapperVersion.getWrapperVersionId());
        } catch (NumberFormatException e) {
            return badRequest("Please provide a long value in the wrapper version field.");
        }


        WrapperVersion wrapperVersion = WrapperVersion.findById(wrapId);

        boolean isWrapperAvailable = MinderWrapperRegistry.get().isWrapperAvailable(wrapperVersion);

        restWrapperVersionResponse.setWrapperVersionId(restWrapperVersion.getWrapperVersionId());
        restWrapperVersionResponse.setAvailable(isWrapperAvailable);



        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestWrapperVersion.class.getName(), restWrapperVersionResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }

    /**
     * This method receives JSON or XML request which includes wrapper name information and returns wrapper information
     * in detail including wrapper versions.
     * The XSD for the client request is given in rest/models/xsd/restwrapperversion.xsd and restwrapper.xsd
     * <p>
     * The sample JSON request:
     * {"wrapperName":"xml-content-verifier"}
     * <p>
     * XML equivalent:
     * </?xml version="1.0" encoding="UTF-8"?>
     * <wrapper>
     * <wrapperName>xml-content-verifier</wrapperName>
     * </wrapper>
     * <p>
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     * JSON
     * =====
     * {"result":"SUCCESS","description":"Test group deleted!"}
     * <p>
     * XML
     * ====
     * </?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     * <wrapper xmlns:ns2="http://www.gitb.com/tpl/v1/" xmlns:ns3="http://www.gitb.com/core/v1/">
     * <id>1</id>
     * <wrapperName>xml-content-verifier</wrapperName>
     * <shortDescription>XMl Content Verifier</shortDescription>
     * <userName>ROOT</userName>
     * <wrapperversions>
     * <restWrapperVersion>
     * <wrapperVersionId>2</wrapperVersionId>
     * <wrapperName>xml-content-verifier</wrapperName>
     * <version>1</version>
     * <creationDate>2015-11-13T15:02:53.156+02:00</creationDate>
     * <isAvailable>true</isAvailable>
     * </restWrapperVersion>
     * </wrapperversions>
     * </wrapper>
     * <p>
     * <p>
     * <p>
     * wrapper name is required.
     */
    public static Result getWrapper() {
        RestWrapper restWrapperResponse = new RestWrapper();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestWrapper restWrapper = null;
        try {
            restWrapper = (RestWrapper) contentProcessor.parseRequest(RestWrapper.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restWrapper.getWrapperName())
            return badRequest("Please provide a Wrapper name");


        //Checking the wrapper status
        Wrapper wrapper = Wrapper.findByName(restWrapper.getWrapperName());

        restWrapperResponse.setWrapperName(wrapper.name);
        restWrapperResponse.setDescription(wrapper.description);
        restWrapperResponse.setId(String.valueOf(wrapper.id));
        restWrapperResponse.setShortDescription(wrapper.shortDescription);
        restWrapperResponse.setUserName(wrapper.user.name);
        restWrapperResponse.setRestWrapperVersion(new ArrayList<RestWrapperVersion>());

        List<WrapperVersion> wrapperVersionList = wrapper.wrapperVersions;
        for (WrapperVersion wv : wrapperVersionList) {
            RestWrapperVersion rwv = new RestWrapperVersion();
            rwv.setWrapperName(wrapper.name);
            rwv.setWrapperVersionId(String.valueOf(wv.id));
            rwv.setVersion(wv.version);
            rwv.setCreationDate(wv.creationDate);
            rwv.setAvailable(MinderWrapperRegistry.get().isWrapperAvailable(wv));

            restWrapperResponse.getRestWrapperVersion().add(rwv);
        }


        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestWrapper.class.getName(), restWrapperResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }


    /**
     * This method receives JSON or XML request which includes wrapper name and short description and adds the new wrapper.
     * The XSD for the client request is given in rest/models/xsd/restwrapper.xsd
     * <p>
     * The sample JSON request:
     * {"wrapperName":"newWrapperName","shortDescription":"new wrapper that added"}
     * <p>
     * XML equivalent:
     * </?xml version="1.0" encoding="UTF-8"?>
     * <wrapper>
     * <wrapperName>newWrapperName</wrapperName>
     * <shortDescription>new wrapper that added</shortDescription>
     * </wrapper>
     * <p>
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     * JSON
     * =====
     * {"result":"SUCCESS","description":"Wrapper created!"}
     * <p>
     * XML
     * ====
     * </?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     * <minderResponse xmlns:ns2="http://www.gitb.com/tpl/v1/" xmlns:ns3="http://www.gitb.com/core/v1/">
     * <result>SUCCESS</result>
     * <description>Wrapper created!</description>
     * </minderResponse>
     * <p>
     * <p>
     * <p>
     * wrapper name and short description are required.
     */
    public static Result addWrapper() {
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

        RestWrapper restWrapper = null;
        try {
            restWrapper = (RestWrapper) contentProcessor.parseRequest(RestWrapper.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restWrapper.getWrapperName())
            return badRequest("Please provide a Wrapper name!");
        if (null == restWrapper.getShortDescription())
            return badRequest("Please provide a short description!");


        //Adding the new wrapper
        Wrapper wrapper = Wrapper.findByName(restWrapper.getWrapperName());
        if (wrapper != null) {
            return badRequest("The wrapper with name [" + restWrapper.getWrapperName() + "] already exists");
        }

        wrapper = new Wrapper();
        wrapper.user = user;
        wrapper.name = restWrapper.getWrapperName();
        wrapper.shortDescription = restWrapper.getShortDescription();

        try {
            wrapper.save();
        } catch (Exception e) {
            return internalServerError("An error occurred during wrapper save: " + e.getMessage());
        }


        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Wrapper created!");

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
     * This method receives JSON or XML request which includes wrapper name and short description and edit the short description
     * of the wrapper.
     * The XSD for the client request is given in rest/models/xsd/restwrapper.xsd
     * <p>
     * The sample JSON request:
     * {"wrapperName":"newWrapperName","shortDescription":"new short description"}
     * <p>
     * XML equivalent:
     * </?xml version="1.0" encoding="UTF-8"?>
     * <wrapper>
     * <wrapperName>newWrapperName</wrapperName>
     * <shortDescription>new short description</shortDescription>
     * </wrapper>
     * <p>
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     * JSON
     * =====
     * {"result":"SUCCESS","description":"Wrapper editted!"}
     * <p>
     * XML
     * ====
     * </?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     *  <minderResponse xmlns:ns2="http://www.gitb.com/core/v1/" xmlns:ns3="http://www.gitb.com/tpl/v1/">
     *      <result>SUCCESS</result>
     *      <description>Wrapper editted!</description>
     *  </minderResponse>
     *
     * <p>
     * wrapper name and short description are required.
     */
    public static Result editWrapper() {
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

        RestWrapper restWrapper = null;
        try {
            restWrapper = (RestWrapper) contentProcessor.parseRequest(RestWrapper.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restWrapper.getWrapperName())
            return badRequest("Please provide a Wrapper name!");
        if (null == restWrapper.getShortDescription())
            return badRequest("Please provide a short description!");


        //Editing the new wrapper
        Wrapper wrapper = Wrapper.findByName(restWrapper.getWrapperName());
        if (wrapper == null) {
            return badRequest("Wrapper with name [" + restWrapper.getWrapperName() + "] not found!");
        }

        wrapper.shortDescription = restWrapper.getShortDescription();
        try {
            wrapper.update();
        } catch (Exception e) {
            return internalServerError("An error occurred during wrapper update: " + e.getMessage());
        }

        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Wrapper editted!");

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
     * This method receives JSON or XML request which includes wrapper name and delete wrapper.
     * <p>
     * The sample JSON request:
     * {"wrapperName":"newWrapperName"}
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Wrapper deleted!"}
     *
     * wrapper name is required.
     */
    public static Result deleteWrapper() {
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

        RestWrapper restWrapper = null;
        try {
            restWrapper = (RestWrapper) contentProcessor.parseRequest(RestWrapper.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restWrapper.getWrapperName())
            return badRequest("Please provide a Wrapper name!");

        //Editing the new wrapper
        Wrapper wrapper = Wrapper.findByName(restWrapper.getWrapperName());
        if (wrapper == null) {
            return badRequest("Wrapper with name [" + restWrapper.getWrapperName() + "] not found!");
        }

        try {
            wrapper.delete();
        } catch (Exception e) {
            return internalServerError("An error occurred during wrapper delete: " + e.getMessage());
        }

        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Wrapper deleted!");

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
     * This method receives JSON or XML request and returns all wrappers defined in db.
     * The XSD for the client request is given in rest/models/xsd/restwrapper.xsd and restwrapperlist.xsd
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     * JSON
     * =====
     *{
     *    "restWrappers":[
     *        {
     *            "id":"1",
     *            "wrapperName":"tamelizer",
     *            "shortDescription":"The tamelizer wrapper for minder",
     *            "description":null,
     *            "userName":"ROOT",
     *            "restWrapperVersion":null
     *        },
     *        {
     *            "id":"2",
     *            "wrapperName":"xml-content-verifier",
     *            "shortDescription":"The default minder xml content verifier",
     *            "description":null,
     *            "userName":"ROOT",
     *            "restWrapperVersion":null
     *        },
     *        {
     *            "id":"3",
     *            "wrapperName":"peppol-validator",
     *            "shortDescription":"A minder adapted version of the Peppol *XML Content Validation Engine",
     *            "description":null,
     *            "userName":"ROOT",
     *            "restWrapperVersion":null
     *        }
     *    ]
     *}
     *
     * <p>
     * No input is necessary.
     */
    public static Result listWrappers() {
        RestWrapperList restWrapperListResponse = new RestWrapperList();


        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        //Getting all wrappers
       List<Wrapper> wrapperList = Wrapper.getAll();
       restWrapperListResponse.setRestWrappers(new ArrayList<RestWrapper>());

        for(Wrapper wrapper : wrapperList){
            RestWrapper rw = new RestWrapper();
            rw.setId(String.valueOf(wrapper.id));
            rw.setWrapperName(wrapper.name);
            rw.setDescription(wrapper.description);
            rw.setId(String.valueOf(wrapper.id));
            rw.setShortDescription(wrapper.shortDescription);
            rw.setUserName(wrapper.user.name);

            restWrapperListResponse.getRestWrappers().add(rw);
        }

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestWrapperList.class.getName(), restWrapperListResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }
}
