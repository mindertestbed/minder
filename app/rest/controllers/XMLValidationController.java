package rest.controllers;

import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;

import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.contentvalidationverifier.ISchemaVerifier;
import rest.controllers.contentvalidationverifier.SchematronVerifier;
import rest.controllers.contentvalidationverifier.XSDVerifier;
import rest.models.RestValidationRequest;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestMinderResponse;

import java.net.MalformedURLException;
import java.text.ParseException;

import static rest.controllers.common.Constants.*;

/**
 * This class provides Minder server side REST service for Schema and Schematron validation for XML files.
 * The schema/schematron may be given in 4 different ways
 * <li>PLAIN: directly given schema/schmatron file as byte array<li/>
 * <li>URL: the url of a schema/schmatron as String<li/>
 * <li>ZIP: a zip file which contains specific schemas/schematrons. In this case, you must also provide the relative path of the root
 * schema/schematron. <li/>
 * <li>JAR: a jar file which contains specific schemas/schematrons. In this case, you must also provide the relative path of the root
 * schema/schematron. <li/>
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 22/10/15.
 */
public class XMLValidationController extends Controller {

    /**
     * This method receives an authenticated rest request for content validation. First, it creates related content processor according
     * to the content type (XML, JSON etc) of the request.
     * Content processor parses the request message and returns the ValidationRequest object (Content Processor is generic and
     * actually unaware of the ValidationRequest class.).
     * <p>
     * The validationrequest is processed in private checkValidation method, which fills MinderResponse object.
     * <p>
     * After receiving the MinderResponse, content processor prepare REST response according to the request's content type(XML or JSON).
     * <p>
     * At last, method returns REST response to the client.
     */
    public Result validateContent() {
        RestMinderResponse minderResponse = new RestMinderResponse();

        String[] parts = request().body().toString().split("Some\\(");
        if(parts.length >0){
            String[] parts2 = parts[1].split("\\)\\,");
            if(parts2.length >0){
            	System.out.println("request body:" + parts2[0]);
            }
        }

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = null;
        try {
            contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }

        RestValidationRequest validationRequest = null;
        try {
            validationRequest = (RestValidationRequest) contentProcessor.parseRequest(RestValidationRequest.class.getName());
        } catch (ParseException e) {
            return internalServerError(e.getMessage());

        }


        /*
        * Calling Minder's built-in XML content verifier
        */
        checkValidation(minderResponse, validationRequest);

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
     * This method is called by validateContent method to call Minder's built-in content validation functions.
     * <p>
     * It creates the ISchemaVerifier object according to the schema type: XSD or schematron. A Schema verifier hides the
     * details of schema and schematron validations and prevents many "if-then-else"s by using polymorphism and interface.
     */
    private static RestMinderResponse checkValidation(RestMinderResponse minderResponse, RestValidationRequest validationRequest) {
        ISchemaVerifier schemaVerifier = null;
        switch (validationRequest.getSchemaType()) {
            case TYPE_XSD:
                schemaVerifier = new XSDVerifier();
                break;
            case TYPE_SCHEMATRON:
                schemaVerifier = new SchematronVerifier();
                break;
            default:
                minderResponse.setResult(FAILURE);
                minderResponse.setDescription("Undefined Type:" + validationRequest.getSchemaType());
                return minderResponse;
        }

        Schema schema;
        String report;
        try {
            switch (validationRequest.getSchemaSubType()) {
                case SUB_TYPE_JAR:
                    schema = schemaVerifier.getSchema(validationRequest.getPathToSchema(), validationRequest.getSchema(), ArchiveType.JAR);
                    report = schemaVerifier.verify(schema, validationRequest.getDocument());
                    minderResponse.setDescription(report);
                    minderResponse.setResult(schemaVerifier.getResult(report));
                    return minderResponse;

                case SUB_TYPE_PLAIN:
                    schema = schemaVerifier.getSchema(validationRequest.getSchema());
                    report = schemaVerifier.verify(schema, validationRequest.getDocument());
                    minderResponse.setDescription(report);
                    minderResponse.setResult(schemaVerifier.getResult(report));
                    return minderResponse;

                case SUB_TYPE_ZIP:
                    schema = schemaVerifier.getSchema(validationRequest.getPathToSchema(), validationRequest.getSchema(), ArchiveType.ZIP);
                    report = schemaVerifier.verify(schema, validationRequest.getDocument());
                    minderResponse.setDescription(report);
                    minderResponse.setResult(schemaVerifier.getResult(report));
                    return minderResponse;

                case SUB_TYPE_URL:
                	  String schemaUrl = new String(validationRequest.getSchema());
                    report = schemaVerifier.verify(schemaUrl, validationRequest.getDocument());
                    minderResponse.setDescription(report);
                    minderResponse.setResult(schemaVerifier.getResult(report));
                    return minderResponse;

                default:
                    minderResponse.setResult(FAILURE);
                    minderResponse.setDescription("Undefined Sub Type:" + validationRequest.getSchemaSubType());
                    return minderResponse;

            }
        } catch (RuntimeException e) {
            minderResponse.setResult(FAILURE);
            minderResponse.setDescription("Exception in content validation:" + e.getMessage());
            return minderResponse;
        } catch (MalformedURLException e) {
            minderResponse.setResult(FAILURE);
            minderResponse.setDescription("Exception in content validation:" + e.getMessage());
            return minderResponse;
        }
    }

}
