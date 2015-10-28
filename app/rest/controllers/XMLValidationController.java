package rest.controllers;

import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.controllers.xmlmodel.response.MinderResponse;
import rest.controllers.xmlmodel.xmlvalidation.request.ValidationRequest;
import rest.controllers.xmlmodel.xmlvalidation.request.verifier.ISchemaVerifier;
import rest.controllers.xmlmodel.xmlvalidation.request.verifier.SchematronVerifier;
import rest.controllers.xmlmodel.xmlvalidation.request.verifier.XMLVerifier;

import java.util.Arrays;

import static rest.controllers.common.Constants.*;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 22/10/15.
 */
public class XMLValidationController extends Controller {

    public static Result validateContent() {
        //TODO her şeyi throw yap burada yakala ki nok gönderelim

        /*
        * Handling the request message
        * */
        IRestContentProcessor contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
        ValidationRequest validationRequest = (ValidationRequest) contentProcessor.parseRequest(ValidationRequest.class.getName());

        /*
        * Calling Minder's built-in XML content verifier
        */
        MinderResponse minderResponse = new MinderResponse();
        checkValidation(minderResponse,validationRequest);

        /*
        * Preparing response
        * */
        //TODO nok ver
        String responseValue = contentProcessor.prepareResponse(MinderResponse.class.getName(), minderResponse);
        System.out.println("responseValue:" + responseValue);

        return ok(responseValue);
    }


    private static MinderResponse checkValidation(MinderResponse minderResponse,ValidationRequest validationRequest) {
        ISchemaVerifier schemaVerifier = null;
        switch (validationRequest.getSchemaType()) {
            case TYPE_XSD:
                schemaVerifier = new XMLVerifier();
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
        try {
            switch (validationRequest.getSchemaSubType()) {
                case SUB_TYPE_JAR:
                    schema = schemaVerifier.getSchema(validationRequest.getPathToSchema(), validationRequest.getSchema(), ArchiveType.JAR);
                    minderResponse.setDescription(schemaVerifier.verify(schema, validationRequest.getDocument()));
                    minderResponse.setResult(schemaVerifier.getPositiveResult());
                    return minderResponse;

                case SUB_TYPE_PLAIN:
                    schema = schemaVerifier.getSchema(validationRequest.getSchema());
                    minderResponse.setDescription(schemaVerifier.verify(schema,validationRequest.getDocument()));
                    minderResponse.setResult(schemaVerifier.getPositiveResult());
                    return minderResponse;

                case SUB_TYPE_ZIP:
                    schema = schemaVerifier.getSchema(validationRequest.getPathToSchema(), validationRequest.getSchema(), ArchiveType.ZIP);
                    minderResponse.setDescription(schemaVerifier.verify(schema,validationRequest.getDocument()));
                    minderResponse.setResult(schemaVerifier.getPositiveResult());
                    return minderResponse;

                case SUB_TYPE_URL:
                    minderResponse.setDescription(schemaVerifier.verify(Arrays.toString(validationRequest.getSchema()),validationRequest.getDocument()));
                    minderResponse.setResult(schemaVerifier.getPositiveResult());
                    return minderResponse;

                default:
                    minderResponse.setResult(FAILURE);
                    minderResponse.setDescription("Undefined Sub Type:" + validationRequest.getSchemaSubType());
                    return minderResponse;

            }
        }catch(Exception e){
                minderResponse.setResult(FAILURE);
                minderResponse.setDescription("Exception received:" + e.getMessage());
                return minderResponse;
        }

    }
}
