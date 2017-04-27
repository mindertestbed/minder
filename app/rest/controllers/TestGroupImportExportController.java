package rest.controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.server.lib.util.NotFoundException;
import controllers.TestCaseController;
import dependencyutils.DependencyClassLoaderCache;
import models.*;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.*;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides importing and exporting functions for a test group including
 * its all test assertions, test cases, test assets, utility classes and dependencies.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 09/02/16.
 */
public class TestGroupImportExportController extends Controller {
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * This method receives JSON or XML request which includes test group id and returns extended test group which includes all
   * details, test assertions, test cases, test assets, dependencies and utility classes. This method aims to export all data of
   * a test group to transfer it.
   * <p>
   * The sample JSON request:
   * {"id":"1"}
   * <p>
   * Group id is required.
   */
  public static Result exportTestGroup() {
        /*
        * Parse client request and get user
        */
    String authorizationData = request().getHeader(AUTHORIZATION);
    HashMap<String, String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);
    //User user = User.findByEmail(clientRequest.get("username"));
    //TODO: RBAC yapınca bunu root rolü olarak değiştir. root@minder
    if (!clientRequest.get("username").equals("root@minder")) {
      return unauthorized(Constants.RESULT_UNAUTHORIZED_ONLY_ROOT);
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

    RestTestGroup responseRestTestGroup = new RestTestGroup();
    try {
      responseRestTestGroup = exportTestGroupData(Long.parseLong(restTestGroup.getId()));
    } catch (NotFoundException e) {
      return internalServerError(e.getMessage());
    } catch (IOException e) {
      return internalServerError(e.getMessage());
    }

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
   * This method receives JSON or XML request which includes an exported test group info and import test group, which includes all
   * details, test assertions, test cases, test assets, dependencies and utility classes, in to the DB.
   * <p>
   * The JSON request will be;
   * {"result":"SUCCESS","description":"Test group imported successfully!"}
   */
  public static Result importTestGroup() {
    String authorizationData = request().getHeader(AUTHORIZATION);
    HashMap<String, String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);
    //User user = User.findByEmail(clientRequest.get("username"));
    //TODO: RBAC yapınca bunu root rolü olarak değiştir. root@minder
    if (!clientRequest.get("username").equals("root@minder")) {
      return unauthorized(Constants.RESULT_UNAUTHORIZED_ONLY_ROOT);
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


    try {
      importTestGroupData(restTestGroup, clientRequest.get("username"));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }


        /*
        * Preparing response
        * */
    RestMinderResponse minderResponse = new RestMinderResponse();

    minderResponse.setResult(Constants.SUCCESS);
    minderResponse.setDescription("Test group imported successfully!");

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

  public static RestTestGroup exportTestGroupData(Long groupId) throws NotFoundException, IOException {
    RestTestGroup responseRestTestGroup = new RestTestGroup();

    //getting the  test group
    TestGroup tg = TestGroup.findById(groupId);
    if (tg == null)
      throw new NotFoundException("Test group with id [" + groupId + "] not found!");

    //
    responseRestTestGroup.setId(String.valueOf(tg.id));
    responseRestTestGroup.setGroupName(tg.name);
    responseRestTestGroup.setShortDescription(tg.shortDescription);
    responseRestTestGroup.setDescription(tg.description);
    responseRestTestGroup.setOwner("");
    responseRestTestGroup.setDependencyString(tg.dependencyString);

    //Add Test Assertions
    responseRestTestGroup.setTestassertions(new ArrayList<RestTestAssertion>());
    List<TestAssertion> testAssertionList = tg.testAssertions;
    for (TestAssertion ta : testAssertionList) {
      RestTestAssertion rta = new RestTestAssertion();
      rta.setTestAssertionId(ta.taId);
      rta.setId(String.valueOf(ta.id));
      rta.setShortDescription(ta.shortDescription);
      rta.setDescription(ta.description);
      rta.setGroupId(String.valueOf(tg.id));
      rta.setNormativeSource(ta.normativeSource);
      rta.setPredicate(ta.predicate);
      rta.setPrerequisites(ta.prerequisites);
      rta.setPrescriptionLevel(ta.prescriptionLevel.toString());
      rta.setTag(ta.tag);
      rta.setTarget(ta.target);
      rta.setVariables(ta.variables);

      //Add Test Cases
      rta.setTestcases(new ArrayList<RestTestCase>());
      List<TestCase> testCaseList = ta.testCases;
      for (TestCase tc : testCaseList) {
        RestTestCase rtc = new RestTestCase();
        rtc.setId(String.valueOf(tc.id));
        rtc.setName(tc.name);
        rtc.setTestAssertionId(ta.taId);

        //Add TDLs
        rtc.setTdls(new ArrayList<RestTdl>());
        List<Tdl> tdlList = tc.tdls;
        for (Tdl tdl : tdlList) {
          RestTdl rt = new RestTdl();
          rt.setId(String.valueOf(tdl.id));
          rt.setTestCaseId(String.valueOf(tc.id));
          rt.setCreationDate(dateFormat.format(tdl.creationDate));
          rt.setTdl(tdl.tdl.getBytes());
          rt.setVersion(tdl.version);

          rtc.getTdls().add(rt);
        }

        rta.getTestcases().add(rtc);
      }

      responseRestTestGroup.getTestassertions().add(rta);
    }

    //Add all test assets
    responseRestTestGroup.setTestassets(new ArrayList<RestTestAsset>());
    List<TestAsset> testAssetList = tg.testAssets;
    for (TestAsset ta : testAssetList) {
      RestTestAsset rta = new RestTestAsset();
      rta.setId(String.valueOf(ta.id));
      rta.setName(ta.name);
      rta.setShortDescription(ta.shortDescription);
      rta.setDescription(ta.description);
      rta.setGroupId(String.valueOf(tg.id));
      byte[] asset = null;
      try {
        asset = RestTestAssetController.handleFileDownload(ta);
      } catch (IOException e) {
        throw new IOException("Test Asset " + "[" + ta.name + "] cannot be downloaded." + e.getMessage());
      }
      rta.setAsset(asset);

      responseRestTestGroup.getTestassets().add(rta);
    }

    //Add all utility classes
    responseRestTestGroup.setUtilClasses(new ArrayList<RestUtilClass>());
    List<UtilClass> utilClassList = tg.utilClasses;
    for (UtilClass uc : utilClassList) {
      RestUtilClass ruc = new RestUtilClass();
      ruc.setId(String.valueOf(uc.id));
      ruc.setName(uc.name);
      ruc.setShortDescription(uc.shortDescription);
      ruc.setGroupId(String.valueOf(tg.id));
      ruc.setSource(uc.source.getBytes());

      responseRestTestGroup.getUtilClasses().add(ruc);
    }

    return responseRestTestGroup;
  }

  public static void importTestGroupData(RestTestGroup restTestGroup, String userName) throws IllegalArgumentException, IllegalAccessException, IOException, NullPointerException, FileNotFoundException {

    //Creating the new test group
    TestGroup group = TestGroup.findByName(restTestGroup.getGroupName());
    if (group != null) {
      throw new IllegalArgumentException("The group with name [" + group.name + "] already exists");
    }

    final User localUser = User.findByEmail(userName);
    if (null == localUser) {
      throw new IllegalAccessException("You must login to Minder with root account.");
    }

    try {
      Ebean.beginTransaction();

      //Save Test Group
      group = new TestGroup();
      group.owner = localUser;
      group.name = restTestGroup.getGroupName();
      group.shortDescription = restTestGroup.getShortDescription();
      group.description = restTestGroup.getDescription();

      //Save Dependency String
      String dependencyString = restTestGroup.getDependencyString();
      if (dependencyString != null) {
        dependencyString = dependencyString.trim();

        if (dependencyString.length() != 0) {
          try {
            DependencyClassLoaderCache.getDependencyClassLoader(dependencyString);
          } catch (Exception ex) {
            Logger.error(ex.getMessage(), ex);
            throw new IOException("There was a problem with the dependency string.<br /> \n" +
               "Please make sure that the dependencies are in format:<br />\n " +
               "groupId:artifactId[:extension[:classifier]]:version]]" + ex.toString());
          }
        }
      }
      group.dependencyString = dependencyString;

      try {
        group.save();
      } catch (Exception e) {
        throw new IOException("An error occurred during test group add: " + e.getMessage());
      }

      //Save Utility Classes
      List<RestUtilClass> restUtilClassList = restTestGroup.getUtilClasses();
      for (RestUtilClass ruc : restUtilClassList) {
        UtilClass uc = new UtilClass();
        uc.name = ruc.getName();
        uc.owner = localUser;
        uc.shortDescription = ruc.getShortDescription();
        uc.testGroup = group;
        uc.source = new String(ruc.getSource());

        try {
          uc.saveNoTransaction();
        } catch (Exception e) {
          throw new IOException("An error occurred during save of util class: " + e.getMessage());
        }
      }

      //Save Test Assertions
      List<RestTestAssertion> rtaList = restTestGroup.getTestassertions();
      for (RestTestAssertion rta : rtaList) {
        TestAssertion ta = TestAssertion.findByTaId(rta.getTestAssertionId());
        if (ta != null) {
          throw new IOException("The test assertion with ID [" + ta.taId + "] already exists");
        }

        ta = new TestAssertion();
        ta.testGroup = group;
        ta.description = rta.getDescription();
        ta.normativeSource = rta.getNormativeSource();
        ta.owner = localUser;
        ta.predicate = rta.getPredicate();
        ta.prerequisites = rta.getPrerequisites();
        ta.shortDescription = rta.getShortDescription();
        ta.tag = rta.getTag();
        ta.taId = rta.getTestAssertionId();
        ta.target = rta.getTarget();
        ta.variables = rta.getVariables();

        PrescriptionLevel prescriptionLevel = null;
        try {
          prescriptionLevel = PrescriptionLevel.valueOf(rta.getPrescriptionLevel());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("The given prescription level [" + rta.getPrescriptionLevel()
             + "] is not defined. Please select one of these: Mandatory, Preffered or Permitted");
        } catch (NullPointerException e) {
          throw new NullPointerException("The prescription level cannot be null. Please select one of these: Mandatory, Preffered or Permitted");
        }
        ta.prescriptionLevel = prescriptionLevel;

        try {
          ta.save();
        } catch (Exception e) {
          throw new IOException("An error occurred during save of test assertion: " + e.getMessage());
        }


        //Save Test Cases
        List<RestTestCase> rtcList = rta.getTestcases();
        for (RestTestCase rtc : rtcList) {
          TestCase tc = TestCase.findByName(rtc.getName());
          if (tc != null) {
            throw new IOException("The test case with name [" + rtc.getName() + "] already exists");
          }

          tc = new TestCase();
          tc.owner = localUser;
          tc.testAssertion = ta;
          tc.name = rtc.getName();

          try {
            tc.save();
          } catch (Exception e) {
            throw new IOException("An error occurred during save of test case: " + e.getMessage());
          }

          //Save TDLs
          for (RestTdl restTdl : rtc.tdls) {
            Tdl tdl = new Tdl();
            tdl.creationDate = new Date();
            tdl.version = restTdl.getVersion();
            tdl.tdl = new String(restTdl.getTdl());
            tdl.testCase = tc;

            try {
              tdl.save();
              TestCaseController.detectAndSaveParameters(tdl);
            } catch (Exception e) {
              throw new IOException("An error occurred during save of tdl: " + e.getMessage());
            }
          }
        }
      }

      //Save test assets
      List<RestTestAsset> restTestAssetList = restTestGroup.getTestassets();
      for (RestTestAsset rta : restTestAssetList) {
        TestAsset ta = new TestAsset();
        ta.name = rta.getName();
        ta.shortDescription = rta.getShortDescription();
        ta.description = rta.getDescription();
        ta.testGroup = group;

        try {
          ta.save();
        } catch (Exception e) {
          throw new IOException("An error occurred during save of test asset: " + e.getMessage());
        }

        if (rta.getAsset() != null && rta.getAsset().length > 0) {
          try {
            RestTestAssetController.handleFileUpload(ta, rta.getAsset());
          } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.getCause().toString());
          } catch (IOException e) {
            throw new IOException(e.getCause().toString());
          }
        }
      }
      Ebean.commitTransaction();
    } finally {
      Ebean.endTransaction();
    }

  }
}
