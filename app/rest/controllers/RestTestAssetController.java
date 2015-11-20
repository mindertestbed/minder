package rest.controllers;

import models.TestAsset;
import models.TestGroup;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestTestAsset;
import rest.models.RestTestAssetList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Minder server side REST service for test asset related operations.
 * <p>
 * Valid for all methods:
 * If you send an XML request, you will gather an XML response.
 * If you send an JSON request, you will gather an JSON response.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 20/11/15.
 */
public class RestTestAssetController extends Controller {
    /**
     * This method receives JSON or XML request and returns all test asset for a given group id
     * To get the details of test assertions please use getTestAssertion method.
     *
     * The sample JSON request:
     * {"groupId":"1"}
     *
     * The sample produced response by Minder (with the status code 200in the header):
     * <p>
     *{
     *    "restTestAssets":[
     *        {
     *            "id":"1",
     *            "groupId":null,
     *            "name":"books.xsd",
     *            "shortDescription":null,
     *            "description":null
     *        },
     *        {
     *            "id":"2",
     *            "groupId":null,
     *            "name":"keys.sch",
     *            "shortDescription":null,
     *            "description":null
     *        }
     *    ]
     *}
     *
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

        if (null==restTestAsset.getGroupId())
            return badRequest("Please provide Test Group ID");


        //getting the  test group
        TestGroup tg = TestGroup.findById(Long.parseLong(restTestAsset.getGroupId()));
        if (tg == null)
            return badRequest("Test group with id [" + restTestAsset.getGroupId() + "] not found!");


        //Getting all test assertions
        restTestAssetList.setRestTestAssets(new ArrayList<RestTestAsset>());

        List<TestAsset> testAssetList = TestAsset.findByGroup(tg);
        for(TestAsset ta : testAssetList){
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
}
