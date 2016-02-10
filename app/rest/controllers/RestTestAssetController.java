package rest.controllers;

import global.Util;
import models.TestAsset;
import models.TestGroup;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;
import rest.models.RestTestAsset;
import rest.models.RestTestAssetList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides Minder server side REST service for test asset related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * The field "asset" indicates the actual data of the asset as bytearray. Therefore, one need to provide asset in Base64 notation.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 20/11/15.
 */
public class RestTestAssetController extends Controller {
    /**
     * This method receives JSON or XML request and returns all test asset for a given group id
     * To get the details of test assertions please use getTestAssertion method.
     * <p>
     * The sample JSON request:
     * {"groupId":"1"}
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     * {
     * "restTestAssets":[
     * {
     * "id":"1",
     * "groupId":null,
     * "name":"books.xsd",
     * "shortDescription":null,
     * "description":null
     * },
     * {
     * "id":"2",
     * "groupId":null,
     * "name":"keys.sch",
     * "shortDescription":null,
     * "description":null
     * }
     * ]
     * }
     * <p>
     * <p>
     * groupId is mandatory.
     */
    public static Result listTestAssets() {
        RestTestAssetList restTestAssetList = new RestTestAssetList();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestTestAsset restTestAsset = null;
        try {
            restTestAsset = (RestTestAsset) contentProcessor.parseRequest(RestTestAsset.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restTestAsset.getGroupId())
            return badRequest("Please provide Test Group ID");


        //getting the  test group
        TestGroup tg = TestGroup.findById(Long.parseLong(restTestAsset.getGroupId()));
        if (tg == null)
            return badRequest("Test group with id [" + restTestAsset.getGroupId() + "] not found!");


        //Getting all test assertions
        restTestAssetList.setRestTestAssets(new ArrayList<RestTestAsset>());

        List<TestAsset> testAssetList = TestAsset.findByGroup(tg);
        for (TestAsset ta : testAssetList) {
            RestTestAsset rta = new RestTestAsset();
            rta.setId(String.valueOf(ta.id));
            rta.setName(ta.name);
            rta.setShortDescription(ta.shortDescription);

            restTestAssetList.getRestTestAssets().add(rta);
        }

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestTestAssetList.class.getName(), restTestAssetList);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }


    /**
     * This method receives JSON or XML request which includes test asset id and returns detailed test asset info.
     * <p>
     * The sample JSON request:
     * {"id":"1"}
     * <p>
     * <p>
     * <p>
     * {
     * "id":"1",
     * "groupId":"1",
     * "name":"books.xsd",
     * "shortDescription":null,
     * "description":null
     * }
     * <p>
     * Test Assertion id is required.
     */
    public static Result getTestAsset() {
        RestTestAsset restTestAssetResponse = new RestTestAsset();

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getCause().toString());
        }

        RestTestAsset restTestAsset = null;
        try {
            restTestAsset = (RestTestAsset) contentProcessor.parseRequest(RestTestAsset.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restTestAsset.getId())
            return badRequest("Please provide an ID");


        //Getting the test asset
        TestAsset ta = TestAsset.findById(Long.parseLong(restTestAsset.getId()));
        if (ta == null) {
            return badRequest("Test asset with ID [" + restTestAsset.getId() + "] not found!");

        }

        restTestAssetResponse.setId(String.valueOf(ta.id));
        restTestAssetResponse.setGroupId(String.valueOf(ta.testGroup.id));
        restTestAssetResponse.setName(ta.name);
        restTestAssetResponse.setDescription(ta.description);
        restTestAssetResponse.setShortDescription(ta.shortDescription);

        /*
        * Preparing response
        * */
        String responseValue = null;
        try {
            responseValue = contentProcessor.prepareResponse(RestTestAsset.class.getName(), restTestAssetResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

        response().setContentType(contentProcessor.getContentType());
        return ok(responseValue);
    }


    /**
     * This method receives JSON or XML request which includes test asset information and creates a new test asset.
     * <p>
     * The sample JSON request:
     * {"groupId":"1",
     * "name":"mel.xsd",
     * "shortDescription":"me mel melo",
     * "asset":"PHhzZDpzY2hlbWEgeG1sbnM6eHNkPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIK
     * ICAgICAgICAgICAgdGFyZ2V0TmFtZXNwYWNlPSJ1cm46Ym9va3MiCiAgICAgICAgICAgIHhtbG5z
     * OmJrcz0idXJuOmJvb2tzIj4KCiAgICA8eHNkOmVsZW1lbnQgbmFtZT0iYm9va3MiIHR5cGU9ImJr
     * czpCb29rc0Zvcm0iLz4KCiAgICA8eHNkOmNvbXBsZXhUeXBlIG5hbWU9IkJvb2tzRm9ybSI+CiAg
     * ICAgICAgPHhzZDpzZXF1ZW5jZT4KICAgICAgICAgICAgPHhzZDplbGVtZW50IG5hbWU9ImJvb2si
     * CiAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlPSJia3M6Qm9va0Zvcm0iCiAgICAgICAgICAg
     * ICAgICAgICAgICAgICBtaW5PY2N1cnM9IjAiCiAgICAgICAgICAgICAgICAgICAgICAgICBtYXhP
     * Y2N1cnM9InVuYm91bmRlZCIvPgogICAgICAgIDwveHNkOnNlcXVlbmNlPgogICAgPC94c2Q6Y29t
     * cGxleFR5cGU+CgogICAgPHhzZDpjb21wbGV4VHlwZSBuYW1lPSJCb29rRm9ybSI+CiAgICAgICAg
     * PHhzZDpzZXF1ZW5jZT4KICAgICAgICAgICAgPHhzZDplbGVtZW50IG5hbWU9ImF1dGhvciIgICB0
     * eXBlPSJ4c2Q6c3RyaW5nIi8+CiAgICAgICAgICAgIDx4c2Q6ZWxlbWVudCBuYW1lPSJ0aXRsZSIg
     * ICAgdHlwZT0ieHNkOnN0cmluZyIvPgogICAgICAgICAgICA8eHNkOmVsZW1lbnQgbmFtZT0iZ2Vu
     * cmUiICAgIHR5cGU9InhzZDpzdHJpbmciLz4KICAgICAgICAgICAgPHhzZDplbGVtZW50IG5hbWU9
     * InByaWNlIiAgICB0eXBlPSJ4c2Q6ZmxvYXQiIC8+CiAgICAgICAgICAgIDx4c2Q6ZWxlbWVudCBu
     * YW1lPSJwdWJfZGF0ZSIgdHlwZT0ieHNkOmRhdGUiIC8+CiAgICAgICAgICAgIDx4c2Q6ZWxlbWVu
     * dCBuYW1lPSJyZXZpZXciICAgdHlwZT0ieHNkOnN0cmluZyIvPgogICAgICAgIDwveHNkOnNlcXVl
     * bmNlPgogICAgICAgIDx4c2Q6YXR0cmlidXRlIG5hbWU9ImlkIiAgIHR5cGU9InhzZDpzdHJpbmci
     * Lz4KICAgIDwveHNkOmNvbXBsZXhUeXBlPgo8L3hzZDpzY2hlbWE+Cg=="}
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200 in the header):
     * {"result":"SUCCESS","description":"Test asset created!"}
     * <p>
     * Group id, name and short description are required, whereas, description is optional.
     *
     */

    public static Result addTestAsset() {
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

        RestTestAsset restTestAsset = null;
        try {
            restTestAsset = (RestTestAsset) contentProcessor.parseRequest(RestTestAsset.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restTestAsset.getGroupId())
            return badRequest("Please provide Test Group Id");
        if (null == restTestAsset.getName())
            return badRequest("Please provide a test asset name");
        if (null == restTestAsset.getShortDescription())
            return badRequest("Please provide a short description");


        //Creating the new test asset
        TestGroup tg = TestGroup.findById(Long.parseLong(restTestAsset.getGroupId()));
        if (tg == null) {
            return badRequest("No group found with id [" + restTestAsset.getGroupId() + "]");
        }

        TestAsset ta = TestAsset.findByGroup(tg, restTestAsset.getName());
        if (ta != null) {
            return badRequest("The test asset with ID [" + ta.name + "] already exists in the group with ID [" + tg.id + "]");
        }

        ta = new TestAsset();
        ta.testGroup = tg;

        //Check the required fields' values.
        try {
            checkAndAssignRequiredFields(ta, restTestAsset);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }

        try {
            ta.save();
        } catch (Exception e) {
            return internalServerError("An error occurred during save of test asset: " + e.getMessage());
        }

        if (restTestAsset.getAsset() != null && restTestAsset.getAsset().length > 0) {
            try {
                handleFileUpload(ta,restTestAsset.getAsset());
            } catch (FileNotFoundException e) {
                return internalServerError(e.getCause().toString());
            } catch (IOException e) {
                return internalServerError(e.getCause().toString());
            }
        }

        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test asset created!");

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
     * This method receives JSON or XML request which includes test asset information and edits the given test asset.
     * <p>
     * The sample JSON request:
     * {"id":"1",
     * "shortDescription":"A sample sth"
     * }
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Test asset updated!"}
     * <p>
     * id is required and cannot be null for the request.
     * For a test asset name and short description are required. If you want to
     * edit any of these fields, please send a not null value in the request. If you do not want to edit these fields, simply not to mention their tags
     * in the request will make you keep their current values in DB.
     * <p>
     * The field description is optional. If you do not want to set any value to them, simply not mention
     * their tags in the request.
     */

    public static Result editTestAsset() {
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

        RestTestAsset restTestAsset = null;
        try {
            restTestAsset = (RestTestAsset) contentProcessor.parseRequest(RestTestAsset.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restTestAsset.getId())
            return badRequest("Please provide an id");

        //Editing the test asset
        TestAsset ta = TestAsset.findById(Long.parseLong(restTestAsset.getId()));
        if (ta == null) {
            return badRequest("The test asset with ID [" + restTestAsset.getId() + "] not found");
        }

        if (!Util.canAccess(user, ta.testGroup.owner))
            return badRequest("You don't have permission to modify this resource");

        //Check the required fields' values.
        try {
            checkAndAssignRequiredFields(ta, restTestAsset);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }

        try {
            ta.update();
        } catch (Exception e) {
            return internalServerError("An error occurred during update of test asset: " + e.getMessage());
        }

        if (restTestAsset.getAsset() != null && restTestAsset.getAsset().length > 0) {
            try {
                handleFileUpload(ta,restTestAsset.getAsset());
            } catch (FileNotFoundException e) {
                return internalServerError(e.getCause().toString());
            } catch (IOException e) {
                return internalServerError(e.getCause().toString());
            }
        }

        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test asset updated!");

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
     * This method receives JSON or XML request which includes test asset id and deletes the test asset.
     * <p>
     * The sample JSON request:
     * {"id":"16"}
     * <p>
     * <p>
     * The sample produced response by Minder (with the status code 200in the header):
     * {"result":"SUCCESS","description":"Test asset deleted!"}
     * <p>
     * id is required.
     */

    public static Result deleteTestAsset() {
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

        RestTestAsset restTestAsset = null;
        try {
            restTestAsset = (RestTestAsset) contentProcessor.parseRequest(RestTestAsset.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getCause().toString());
        }

        if (null == restTestAsset.getId())
            return badRequest("Please provide an ID");

        //Deleting the test asset
        TestAsset ta = TestAsset.findById(Long.parseLong(restTestAsset.getId()));
        if (ta == null) {
            return badRequest("Test asset with id " + restTestAsset.getId() + " does not exist.");
        }

        if (!Util.canAccess(user, ta.testGroup.owner))
            return badRequest("You don't have permission to modify this resource");

        try {
            ta.delete();
        } catch (Exception ex) {
            return internalServerError("An error occurred during test asset delete: " + ex.getMessage());
        }

        new File("assets/_" + ta.testGroup.id + "/" + ta.name).delete();

        //
        minderResponse.setResult(Constants.SUCCESS);
        minderResponse.setDescription("Test asset deleted!");

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


    private static void checkAndAssignRequiredFields(TestAsset ta, RestTestAsset restTestAsset) throws IllegalArgumentException {

        //Checking the required fields
        if (null != restTestAsset.getName()) {
            if (restTestAsset.getName().equals("")) {
                throw new IllegalArgumentException("The required field name cannot be empty");

            }
            ta.name = restTestAsset.getName();
        }

        if (null != restTestAsset.getShortDescription()) {
            if (restTestAsset.getShortDescription().equals("")) {
                throw new IllegalArgumentException("The required field shortDescription cannot be empty.");

            }
            ta.shortDescription = restTestAsset.getShortDescription();
        }


        //Checking the other editable fields
        if (null != restTestAsset.getDescription()) {
            ta.description = restTestAsset.getDescription();
        }

    }

    protected static void handleFileUpload(TestAsset testAsset, byte[] asset) throws IOException, FileNotFoundException {
        new File("assets/_" + testAsset.testGroup.id).mkdirs();
        File fl = new File("assets/_" + testAsset.testGroup.id + "/" + testAsset.name);

        FileOutputStream fos;
        InputStream is = new ByteArrayInputStream(asset);
        try {
            fos = new FileOutputStream(fl);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Asset for [_" + testAsset.testGroup.id + "/" + testAsset.name + "] couldn't be created");
        }
        int r = -1;
        byte[] bulk = new byte[1024];

        try {
            while ((r = is.read(bulk)) != -1) {
                fos.write(bulk, 0, r);
            }
        } catch (IOException e) {
            throw new IOException("Error occurred during the read of test asset: " + e.getCause());
        }
        try {
            is.close();
        } catch (IOException e) {
            throw new IOException("Error occurred during the closing of input stream: " + e.getCause());
        }

        try {
            fos.close();
        } catch (IOException e) {
            throw new IOException("Error occurred during the closing of output stream: " + e.getCause());
        }
    }

    protected static byte[] handleFileDownload(TestAsset testAsset) throws IOException {
        Path path = Paths.get("assets/_" + testAsset.testGroup.id + "/" + testAsset.name);
        byte[] asset = null;
        try {
            asset = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IOException("Error occurred during the reading bytes from input stream: " + e.getCause());
        }

        return asset;
    }
}
