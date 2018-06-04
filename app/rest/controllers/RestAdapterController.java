package rest.controllers;

import minderengine.MinderAdapterRegistry;
import models.Adapter;
import models.User;
import models.AdapterVersion;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestAdapter;
import rest.models.RestAdapterList;
import rest.models.RestAdapterVersion;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for adapter related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 17/11/15.
 */
public class RestAdapterController extends Controller {
  /**
   * This method receives JSON or XML request which includes adapter version id information.
   * The XSD for the client request is given in rest/models/xsd/restadapterversion.xsd
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   * <p>
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"id":"1","adapterName":null,"version":null,"creationDate":null,"available":true}
   * <p>
   * adapter version id is required. Note that "id" is the identifier for adapterversion table, not the version itself.
   */
  public Result getAdapterStatus() {
    RestAdapterVersion restAdapterVersionResponse = new RestAdapterVersion();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestAdapterVersion restAdapterVersion = null;
    try {
      restAdapterVersion = (RestAdapterVersion) contentProcessor.parseRequest(RestAdapterVersion.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restAdapterVersion.getAdapterVersionId())
      return badRequest("Please provide Adapter id");


    //Checking the adapter status
    long wrapId = 0L;
    try {
      wrapId = Long.parseLong(restAdapterVersion.getAdapterVersionId());
    } catch (NumberFormatException e) {
      return badRequest("Please provide a long value in the adapter version field.");
    }


    AdapterVersion adapterVersion = AdapterVersion.findById(wrapId);

    boolean isAdapterAvailable = MinderAdapterRegistry.get().isAdapterAvailable(adapterVersion);

    restAdapterVersionResponse.setAdapterVersionId(restAdapterVersion.getAdapterVersionId());
    restAdapterVersionResponse.setAvailable(isAdapterAvailable);



        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestAdapterVersion.class.getName(), restAdapterVersionResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }

  /**
   * This method receives JSON or XML request which includes adapter name information and returns adapter information
   * in detail including adapter versions.
   * The XSD for the client request is given in rest/models/xsd/restadapterversion.xsd and restadapter.xsd
   * <p>
   * The sample JSON request:
   * {"adapterName":"xml-content-verifier"}
   * <p>
   * XML equivalent:
   * </?xml version="1.0" encoding="UTF-8"?>
   * <adapter>
   * <adapterName>xml-content-verifier</adapterName>
   * </adapter>
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
   * <adapter xmlns:ns2="http://www.gitb.com/tpl/v1/" xmlns:ns3="http://www.gitb.com/core/v1/">
   * <id>1</id>
   * <adapterName>xml-content-verifier</adapterName>
   * <shortDescription>XMl Content Verifier</shortDescription>
   * <userName>ROOT</userName>
   * <adapterversions>
   * <restAdapterVersion>
   * <adapterVersionId>2</adapterVersionId>
   * <adapterName>xml-content-verifier</adapterName>
   * <version>1</version>
   * <creationDate>2015-11-13T15:02:53.156+02:00</creationDate>
   * <isAvailable>true</isAvailable>
   * </restAdapterVersion>
   * </adapterversions>
   * </adapter>
   * <p>
   * <p>
   * <p>
   * adapter name is required.
   */
  public Result getAdapter() {
    RestAdapter restAdapterResponse = new RestAdapter();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestAdapter restAdapter = null;
    try {
      restAdapter = (RestAdapter) contentProcessor.parseRequest(RestAdapter.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restAdapter.getAdapterName())
      return badRequest("Please provide a Adapter name");


    //Checking the adapter status
    Adapter adapter = Adapter.findByName(restAdapter.getAdapterName());

    restAdapterResponse.setAdapterName(adapter.name);
    restAdapterResponse.setDescription(adapter.description);
    restAdapterResponse.setId(String.valueOf(adapter.id));
    restAdapterResponse.setShortDescription(adapter.shortDescription);
    restAdapterResponse.setUserName(adapter.user.name);
    restAdapterResponse.setRestAdapterVersion(new ArrayList<RestAdapterVersion>());

    List<AdapterVersion> adapterVersionList = adapter.adapterVersions;
    for (AdapterVersion wv : adapterVersionList) {
      RestAdapterVersion rwv = new RestAdapterVersion();
      rwv.setAdapterName(adapter.name);
      rwv.setAdapterVersionId(String.valueOf(wv.id));
      rwv.setVersion(wv.version);
      rwv.setCreationDate(wv.creationDate);
      rwv.setAvailable(MinderAdapterRegistry.get().isAdapterAvailable(wv));

      restAdapterResponse.getRestAdapterVersion().add(rwv);
    }


        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestAdapter.class.getName(), restAdapterResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes adapter name and short description and adds the new adapter.
   * The XSD for the client request is given in rest/models/xsd/restadapter.xsd
   * <p>
   * The sample JSON request:
   * {"adapterName":"newAdapterName","shortDescription":"new adapter that added"}
   * <p>
   * XML equivalent:
   * </?xml version="1.0" encoding="UTF-8"?>
   * <adapter>
   * <adapterName>newAdapterName</adapterName>
   * <shortDescription>new adapter that added</shortDescription>
   * </adapter>
   * <p>
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * <p>
   * JSON
   * =====
   * {"result":"SUCCESS","description":"Adapter created!"}
   * <p>
   * XML
   * ====
   * </?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <minderResponse xmlns:ns2="http://www.gitb.com/tpl/v1/" xmlns:ns3="http://www.gitb.com/core/v1/">
   * <result>SUCCESS</result>
   * <description>Adapter created!</description>
   * </minderResponse>
   * <p>
   * <p>
   * <p>
   * adapter name and short description are required.
   */
  public Result addAdapter() {
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

    RestAdapter restAdapter = null;
    try {
      restAdapter = (RestAdapter) contentProcessor.parseRequest(RestAdapter.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restAdapter.getAdapterName())
      return badRequest("Please provide a Adapter name!");
    if (null == restAdapter.getShortDescription())
      return badRequest("Please provide a short description!");


    //Adding the new adapter
    Adapter adapter = Adapter.findByName(restAdapter.getAdapterName());
    if (adapter != null) {
      return badRequest("The adapter with name [" + restAdapter.getAdapterName() + "] already exists");
    }

    adapter = new Adapter();
    adapter.user = user;
    adapter.name = restAdapter.getAdapterName();
    adapter.shortDescription = restAdapter.getShortDescription();

    try {
      adapter.save();
    } catch (Exception e) {
      return internalServerError("An error occurred during adapter save: " + e.getMessage());
    }


    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Adapter created!");

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
   * This method receives JSON or XML request which includes adapter name and short description and edit the short description
   * of the adapter.
   * The XSD for the client request is given in rest/models/xsd/restadapter.xsd
   * <p>
   * The sample JSON request:
   * {"adapterName":"newAdapterName","shortDescription":"new short description"}
   * <p>
   * XML equivalent:
   * </?xml version="1.0" encoding="UTF-8"?>
   * <adapter>
   * <adapterName>newAdapterName</adapterName>
   * <shortDescription>new short description</shortDescription>
   * </adapter>
   * <p>
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * <p>
   * JSON
   * =====
   * {"result":"SUCCESS","description":"Adapter editted!"}
   * <p>
   * XML
   * ====
   * </?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *  <minderResponse xmlns:ns2="http://www.gitb.com/core/v1/" xmlns:ns3="http://www.gitb.com/tpl/v1/">
   *      <result>SUCCESS</result>
   *      <description>Adapter editted!</description>
   *  </minderResponse>
   *
   * <p>
   * adapter name and short description are required.
   */
  public Result editAdapter() {
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

    RestAdapter restAdapter = null;
    try {
      restAdapter = (RestAdapter) contentProcessor.parseRequest(RestAdapter.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restAdapter.getAdapterName())
      return badRequest("Please provide a Adapter name!");
    if (null == restAdapter.getShortDescription())
      return badRequest("Please provide a short description!");


    //Editing the new adapter
    Adapter adapter = Adapter.findByName(restAdapter.getAdapterName());
    if (adapter == null) {
      return badRequest("Adapter with name [" + restAdapter.getAdapterName() + "] not found!");
    }

    adapter.shortDescription = restAdapter.getShortDescription();
    try {
      adapter.update();
    } catch (Exception e) {
      return internalServerError("An error occurred during adapter update: " + e.getMessage());
    }

    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Adapter editted!");

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
   * This method receives JSON or XML request which includes adapter name and delete adapter.
   * <p>
   * The sample JSON request:
   * {"adapterName":"newAdapterName"}
   *
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Adapter deleted!"}
   *
   * adapter name is required.
   */
  public Result deleteAdapter() {
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

    RestAdapter restAdapter = null;
    try {
      restAdapter = (RestAdapter) contentProcessor.parseRequest(RestAdapter.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restAdapter.getAdapterName())
      return badRequest("Please provide a Adapter name!");

    //Editing the new adapter
    Adapter adapter = Adapter.findByName(restAdapter.getAdapterName());
    if (adapter == null) {
      return badRequest("Adapter with name [" + restAdapter.getAdapterName() + "] not found!");
    }

    try {
      adapter.delete();
    } catch (Exception e) {
      return internalServerError("An error occurred during adapter delete: " + e.getMessage());
    }

    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Adapter deleted!");

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
   * This method receives JSON or XML request and returns all adapters defined in db.
   * The XSD for the client request is given in rest/models/xsd/restadapter.xsd and restadapterlist.xsd
   *
   * The sample produced response by Minder (with the status code 200in the header):
   * <p>
   * JSON
   * =====
   *{
   *    "restAdapters":[
   *        {
   *            "id":"1",
   *            "adapterName":"tamelizer",
   *            "shortDescription":"The tamelizer adapter for minder",
   *            "description":null,
   *            "userName":"ROOT",
   *            "restAdapterVersion":null
   *        },
   *        {
   *            "id":"2",
   *            "adapterName":"xml-content-verifier",
   *            "shortDescription":"The default minder xml content verifier",
   *            "description":null,
   *            "userName":"ROOT",
   *            "restAdapterVersion":null
   *        },
   *        {
   *            "id":"3",
   *            "adapterName":"peppol-validator",
   *            "shortDescription":"A minder adapted version of the Peppol *XML Content Validation Engine",
   *            "description":null,
   *            "userName":"ROOT",
   *            "restAdapterVersion":null
   *        }
   *    ]
   *}
   *
   * <p>
   * No input is necessary.
   */
  public Result listAdapters() {
    RestAdapterList restAdapterListResponse = new RestAdapterList();


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
    List<Adapter> adapterList = Adapter.getAll();
    restAdapterListResponse.setRestAdapters(new ArrayList<RestAdapter>());

    for (Adapter adapter : adapterList) {
      RestAdapter rw = new RestAdapter();
      rw.setId(String.valueOf(adapter.id));
      rw.setAdapterName(adapter.name);
      rw.setDescription(adapter.description);
      rw.setId(String.valueOf(adapter.id));
      rw.setShortDescription(adapter.shortDescription);
      rw.setUserName(adapter.user.name);

      restAdapterListResponse.getRestAdapters().add(rw);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestAdapterList.class.getName(), restAdapterListResponse);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }
}
