package rest.controllers;

import minderengine.MinderWrapperRegistry;
import models.Wrapper;
import models.WrapperVersion;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestWrapper;
import rest.models.RestWrapperVersion;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Minder server side REST service for wrapper related operations.
 *
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 17/11/15.
 */
public class WrapperController extends Controller {
    /**
     * This method receives JSON or XML request which includes wrapper version id information.
     * The XSD for the client request is given in rest/models/xsd/restwrapperversion.xsd
     * <p>
     * The sample JSON request:
     * {"id":"1"}
     *
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

        if (null==restWrapperVersion.getWrapperVersionId())
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
     * The XSD for the client request is given in rest/models/xsd/restwrapperversion.xsd
     * <p>
     * The sample JSON request:
     * {"wrapperName":"xml-content-verifier"}
     *
     * XML equivalent:
     * </?xml version="1.0" encoding="UTF-8"?>
     * <wrapper>
     *     <wrapperName>xml-content-verifier</wrapperName>
     * </wrapper>
     *
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     *
     * JSON
     * =====
     * {"result":"SUCCESS","description":"Test group deleted!"}
     *
     * XML
     * ====
     * </?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     *  <wrapper xmlns:ns2="http://www.gitb.com/tpl/v1/" xmlns:ns3="http://www.gitb.com/core/v1/">
     *      <id>1</id>
     *      <wrapperName>xml-content-verifier</wrapperName>
     *      <shortDescription>XMl Content Verifier</shortDescription>
     *      <userName>ROOT</userName>
     *      <wrapperversions>
     *          <restWrapperVersion>
     *              <wrapperVersionId>2</wrapperVersionId>
     *              <wrapperName>xml-content-verifier</wrapperName>
     *              <version>1</version>
     *              <creationDate>2015-11-13T15:02:53.156+02:00</creationDate>
     *              <isAvailable>true</isAvailable>
     *           </restWrapperVersion>
     *      </wrapperversions>
     *  </wrapper>
     *
     *
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

        if (null==restWrapper.getWrapperName())
            return badRequest("Please provide a Wrapper name");


        //Checking the wrapper status
        Wrapper wrapper  = Wrapper.findByName(restWrapper.getWrapperName());

        restWrapperResponse.setWrapperName(wrapper.name);
        restWrapperResponse.setDescription(wrapper.description);
        restWrapperResponse.setId(String.valueOf(wrapper.id));
        restWrapperResponse.setShortDescription(wrapper.shortDescription);
        restWrapperResponse.setUserName(wrapper.user.name);
        restWrapperResponse.setRestWrapperVersion(new ArrayList<RestWrapperVersion>());

        List<WrapperVersion> wrapperVersionList = wrapper.wrapperVersions;
        for(WrapperVersion wv : wrapperVersionList){
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
}
