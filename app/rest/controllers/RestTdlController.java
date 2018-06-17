package rest.controllers;

import com.avaje.ebean.Ebean;
import controllers.TestCaseController;
import utils.TdlUtils;
import utils.Util;
import minderengine.TestEngine;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestTdl;
import rest.models.RestTdlAdapterParam;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class provides Minder server side REST service for tdl related operations.
 *
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * The field "tdl" indicates the actual tdl code as bytearray. Therefore, one need to provide asset in Base64 notation.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 07/12/15.
 */
public class RestTdlController extends Controller {
  TestCaseController testCaseController;
  TestEngine testEngine;

  @Inject
  public RestTdlController(TestCaseController testCaseController, TestEngine testEngine) {
    this.testCaseController = testCaseController;
    this.testEngine = testEngine;
  }

  public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * This method receives JSON or XML request which includes tdl id and returns detailed tdl info.
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   *
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   *{
   *    "id":"2",
   *    "testCaseId":"1",
   *    "tdl":"var bookXsd:Array[Byte] = null\r\nvar bookXML:Array[Byte] = *null\r\n\r\n//initialize the data, in a rivet\r\nrunAsRivet(()=>{\r\n  //read the *parameter 'xsdName' from the JOB\r\n  val xsdAssetName = getParameter(\"xsdName\")\r\n  *val xmlAssetName = getParameter(\"xmlName\")\r\n  //read the asset and assign it to *bookXsd\r\n  bookXsd = getAsset(xsdAssetName).getValue\r\n  bookXML = *getAsset(xmlAssetName).getValue\r\n})\r\n//Third, and last rivet\r\n//Wait for XML *generator to generate the XML data and feed it to the\r\n//built-in xml content *verifier.\r\n//But before that, pipe the XML through a converter function\r\n//and log *its contents.\r\n//NOTE: we can also perform schema verification via the built *in\r\n//verifyXsd function. But here we have preferred this.\r\nval rivet3 = *\"verifyXsd(byte[],byte[])\" of \"$xml-content-verifier\" shall(\r\n  *map(invokeLater(()=>bookXML) --> 1),\r\n  map(invokeLater(()=>bookXsd) --> 1)\r\n)\r\n ",
   *    "version":"2",
   *    "creationDate":"2015-12-04T15:19:07.724+02:00",
   *    "parameters":[
   *        {
   *            "id":"2",
   *            "name":"$generator",
   *            "signatures":[
   *                "generateXML(byte[])",
   *                "xmlProduced(byte[])"
   *            ]
   *        },
   *        {
   *            "id":"1",
   *            "name":"$initiator",
   *            "signatures":[
   *                "initialDataCreated(byte[])",
   *                "generateBooksData(int)"
   *            ]
   *        }
   *    ]
   *}
   * <p>
   * tdl id is required.
   * The field "tdl"  will be provided in Base64 format. Please convert it into String.
   */
  public Result getTdl() {
    RestTdl responseRestTdl = new RestTdl();

        /*
        * Handling the request message
        * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    RestTdl restTdl = null;
    try {
      restTdl = (RestTdl) contentProcessor.parseRequest(RestTdl.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTdl.getId())
      return badRequest("Please provide a TDL id");


    //Getting the tdl
    Tdl tdl = Tdl.findById(Long.parseLong(restTdl.getId()));
    if (tdl == null)
      return badRequest("TDL with id [" + restTdl.getId() + "] not found!");

    responseRestTdl.setId(String.valueOf(tdl.id));
    responseRestTdl.setTestCaseId(String.valueOf(tdl.testCase.id));
    responseRestTdl.setCreationDate(dateFormat.format(tdl.creationDate));
    responseRestTdl.setTdl(tdl.tdl.getBytes());
    responseRestTdl.setVersion(tdl.version);
    responseRestTdl.setParameters(new ArrayList<RestTdlAdapterParam>());

    for (AdapterParam adapterParam : tdl.parameters) {
      RestTdlAdapterParam restWP = new RestTdlAdapterParam();
      restWP.setId(String.valueOf(adapterParam.id));
      restWP.setName(adapterParam.name);
      restWP.setSignatures(new ArrayList<String>());
      for (ParamSignature ps : adapterParam.signatures) {
        restWP.getSignatures().add(ps.signature);
      }

      responseRestTdl.getParameters().add(restWP);
    }

        /*
        * Preparing response
        * */
    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestTdl.class.getName(), responseRestTdl);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }

    response().setContentType(contentProcessor.getContentType());
    return ok(responseValue);
  }


  /**
   * This method receives JSON or XML request which includes tdl information and creates a new tdl.
   * <p>
   * The sample JSON request:
   *{
   *    "testCaseId":"5",
   *    "tdl":"Ly9pbml0aWFsaXplIHRoZSBkYXRhLCBpbiBhIHJpdmV0IHZhbCByaXZldDMgPSAidmVyaWZ5WHNkKGJ5dGVbXSxieXRlW10pIiBvZiAiJHhtbC1jb250ZW50LXZlcmlmaWVyIiBzaGFsbChtYXAoaW52b2tlTGF0ZXIoKCk9PmdldEFzc2V0KCJib29rcy54c2QiKSkgLS0+IDEpLG1hcChpbnZva2VMYXRlcigoKT0+Z2V0QXNzZXQoInNhbXBsZS1ib29rLnhtbCIpKSAtLT4gMSkp",
   *    "version":"2-BETA"
   *}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Tdl created wit the id [7]"}
   * <p>
   * testCaseId, tdl and version are required.
   * TDl must ve provided in Base64 format.
   */

  public Result addTdl() {
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

    RestTdl restTdl = null;
    try {
      restTdl = (RestTdl) contentProcessor.parseRequest(RestTdl.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTdl.getTestCaseId())
      return badRequest("Please provide a Test Case ID");
    if (null == restTdl.getTdl())
      return badRequest("Please provide related a non-null tdl");
    if (null == restTdl.getVersion())
      return badRequest("Please provide a version identifier");


    //Creating the new TDL
    TestCase testCase = TestCase.findById(Long.parseLong(restTdl.getTestCaseId()));
    if (testCase == null) {
      return badRequest("No test case found with id [" + restTdl.getTestCaseId() + "].");
    }

    Tdl tdl = Tdl.findByTestCaseAndVersion(testCase, restTdl.getVersion());
    if (tdl != null) {
      return badRequest("The tdl with test case id [" + restTdl.getTestCaseId() + "] and with version [" + restTdl.getVersion() + "] already exists");
    }


    tdl = new Tdl();
    tdl.testCase = testCase;

    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(tdl, restTdl);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      Ebean.beginTransaction();
      tdl.save();
      TdlUtils.detectAndSaveParameters(tdl);
      Ebean.commitTransaction();
    } catch (Exception ex) {
      return internalServerError("An error occurred during tdl save or compile: " + ex.getMessage());
    } finally {
      Ebean.endTransaction();
    }

    //
    tdl = Tdl.findByTestCaseAndVersion(testCase, restTdl.getVersion());
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Tdl created with the id [" + tdl.id + "]");

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
   * This method receives JSON or XML request which includes tdl information and creates a new tdl.
   * <p>
   * The sample JSON request:
   *{
   *    "id":"6",
   *    "tdl":"Ly9pbml0aWFsaXplIHRoZSBkYXRhLCBpbiBhIHJpdmV0IHZhbCByaXZldDMgPSAidmVyaWZ5WHNkKGJ5dGVbXSxieXRlW10pIiBvZiAiJHhtbC1jb250ZW50LXZlcmlmaWVyIiBzaGFsbChtYXAoaW52b2tlTGF0ZXIoKCk9PmdldEFzc2V0KCJib29rcy54c2QiKSkgLS0+IDEpLG1hcChpbnZva2VMYXRlcigoKT0+Z2V0QXNzZXQoInNhbXBsZS1ib29rLnhtbCIpKSAtLT4gMSkp"
   *}
   * <p>
   * <p>
   * The sample produced response by Minder (with the status code 200in the header):
   * {"result":"SUCCESS","description":"Tdl edited!"}
   * <p>
   * tdl id and tdl are required
   * TDl must ve provided in Base64 format.
   */

  public Result editTdl() {
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

    RestTdl restTdl = null;
    try {
      restTdl = (RestTdl) contentProcessor.parseRequest(RestTdl.class.getName());
    } catch (ParseException e) {
      return internalServerError(e.getCause().toString());
    }

    if (null == restTdl.getId())
      return badRequest("Please provide a TDL Id");
    if (null == restTdl.getTdl())
      return badRequest("Please provide related a non-null tdl");


    //Editing the TDL
    Tdl tdl = Tdl.findById(Long.parseLong(restTdl.getId()));
    if (tdl == null) {
      return badRequest("No tdl found with id [" + restTdl.getId() + "].");
    }

    if (!Util.canAccess(user, tdl.testCase.owner))
      return badRequest("You don't have permission to modify this resource");


    //Check the required fields' values.
    try {
      checkAndAssignRequiredFields(tdl, restTdl);
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    }

    try {
      testEngine.describeTdl(tdl);
    } catch (Exception ex) {
      return internalServerError("An error occurred during tdl compile: " + ex.getMessage());
    }

    try {
      tdl.update();
    } catch (Exception e) {
      return internalServerError("An error occurred during update of tdl: " + e.getMessage());
    }

    //
    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Tdl edited!");

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


  private void checkAndAssignRequiredFields(Tdl tdl, RestTdl restTdl) throws IllegalArgumentException {

    //Checking the required fields
    if (null != restTdl.getVersion()) {
      if (restTdl.getVersion().equals("")) {
        throw new IllegalArgumentException("The required field version cannot be empty");

      }
      tdl.version = restTdl.getVersion();
    }

    if (null != restTdl.getTdl()) {
      if (restTdl.getTdl().equals("")) {
        throw new IllegalArgumentException("The required field tdl cannot be empty.");

      }
      tdl.tdl = new String(restTdl.getTdl());
    }
  }
}
