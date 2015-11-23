package rest.controllers;

import java.text.ParseException;

import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;

import com.gitb.ms.v1.BasicRequest;
import com.gitb.ms.v1.BeginTransactionRequest;
import com.gitb.ms.v1.FinalizeRequest;
import com.gitb.ms.v1.GetModuleDefinitionResponse;
import com.gitb.ms.v1.InitiateRequest;
import com.gitb.ms.v1.InitiateResponse;
import com.gitb.ms.v1.SendRequest;
import com.gitb.ms.v1.SendResponse;

public class GitbMessagingController extends Controller {

	public static Result getModuleDefinition() {
		
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}
		
		//TODO: method operations
		
		GetModuleDefinitionResponse serviceResponse = new GetModuleDefinitionResponse();
		
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(GetModuleDefinitionResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}

	public static Result initiate(InitiateRequest parameters){
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

		//Converting to http body to InitiateRequest object
		InitiateRequest initiateRequest = null;
		try {
			initiateRequest = (InitiateRequest) contentProcessor.parseRequest(InitiateRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		//TODO: method operations
		
		InitiateResponse serviceResponse = new InitiateResponse();
		
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(InitiateResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}

	public static Result beginTransaction(BeginTransactionRequest parameters){
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

		//Converting to http body to BeginTransactionRequest object
		BeginTransactionRequest beginTransactionRequest = null;
		try {
			beginTransactionRequest = (BeginTransactionRequest) contentProcessor.parseRequest(BeginTransactionRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		//TODO: method operations
		
		InitiateResponse serviceResponse = new InitiateResponse();
		
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(InitiateResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}

	public static Result endTransaction(BasicRequest parameters){
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

		//Converting to http body to BasicRequest object
		BasicRequest basicRequest = null;
		try {
			basicRequest = (BasicRequest) contentProcessor.parseRequest(BasicRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		//TODO: method operations
		
		InitiateResponse serviceResponse = new InitiateResponse();
		
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(InitiateResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}

	public static Result send(SendRequest parameters){
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

		//Converting to http body to SendRequest object
		SendRequest sendRequest = null;
		try {
			sendRequest = (SendRequest) contentProcessor.parseRequest(SendRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		//TODO: method operations
		
		SendResponse serviceResponse = new SendResponse();
		
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(SendResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}

	public static Result finalize(FinalizeRequest parameters){
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}
		
		//TODO: method operations
		
		InitiateResponse serviceResponse = new InitiateResponse();

		//Converting to http body to FinalizeRequest object
		FinalizeRequest finalizeRequest = null;
		try {
			finalizeRequest = (FinalizeRequest) contentProcessor.parseRequest(FinalizeRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(InitiateResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}
}
