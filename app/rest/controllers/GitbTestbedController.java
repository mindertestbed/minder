package rest.controllers;

import java.text.ParseException;
import java.util.List;

import minderengine.TestEngine;
import models.Tdl;
import models.TestAssertion;
import models.TestCase;
import models.TestGroup;
import models.WrapperParam;
import mtdl.MinderTdl;
import mtdl.TdlCompiler;
import mtdl.Rivet;

import com.gitb.core.v1.Metadata;
import com.gitb.core.v1.Roles;
import com.gitb.core.v1.TestRole;
import com.gitb.core.v1.TestRoleEnumeration;
import com.gitb.tbs.v1.BasicRequest;
import com.gitb.tbs.v1.GetTestCaseDefinitionResponse;
import com.gitb.tpl.v1.DecisionStep;
import com.gitb.tpl.v1.MessagingStep;
import com.gitb.tpl.v1.Preliminary;
import com.gitb.tpl.v1.Sequence;
import com.gitb.tpl.v1.TestStep;

import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;

public class GitbTestbedController extends Controller {

	public static Result getTestCaseDefinition() {
		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

		BasicRequest basicRequest = null;
		try {
			basicRequest = (BasicRequest) contentProcessor.parseRequest(BasicRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		String tcId = basicRequest.getTcId();
		
		TestCase minderTestCase = TestCase.findById(Long.parseLong(tcId));
		List<Tdl> list = Tdl.findByTestCase(minderTestCase);

	    if (list.size() == 0)
	      return internalServerError("TDL not exist.");;

	    Tdl newestTdl = list.get(0);
	    Tdl oldestTdl = list.get(list.size()-1);
	    List<WrapperParam> wrapperParams = WrapperParam.findByTestCase(newestTdl);
		
	    GetTestCaseDefinitionResponse serviceResponse = new GetTestCaseDefinitionResponse();
		
	    com.gitb.tpl.v1.TestCase gitbTestCase = new com.gitb.tpl.v1.TestCase(); 
	    
	    Metadata metadata = new Metadata();
		metadata.setName(minderTestCase.name);
		metadata.setVersion(newestTdl.version);
		metadata.setAuthors(minderTestCase.owner.name);
		metadata.setDescription(minderTestCase.description);
		metadata.setPublished(oldestTdl.creationDate.toString());
		metadata.setLastModified(newestTdl.creationDate.toString());
		
		gitbTestCase.setMetadata(metadata);
		
		Roles actors = new Roles();
		for (WrapperParam wrapperParam : wrapperParams) {
			TestRole testRole = new TestRole();
			testRole.setId(wrapperParam.id.toString());
			testRole.setName(wrapperParam.name.toString());
			testRole.setRole(TestRoleEnumeration.SUT);
			actors.getActor().add(testRole);
		}
		
		gitbTestCase.setActors(actors);
		
		gitbTestCase.setPreliminary(new Preliminary());
		
		Sequence sequence = new Sequence();
		
		TestAssertion testAssertion = TestAssertion.findById(minderTestCase.testAssertion.id);		
		TestGroup testGroup = TestGroup.findById(testAssertion.testGroup.id);  
		String packageRoot = "_" + testGroup.id;  
		String packagePath = packageRoot + "/_" + minderTestCase.id;  
		Class<MinderTdl> cls = TdlCompiler.compileTdl(packageRoot, packagePath, testGroup.dependencyString, minderTestCase.name, newestTdl.tdl, newestTdl.version);
		List<Rivet> rivets = TestEngine.describe(cls);
		
		for (Rivet rivet : rivets) {
			TestStep testStep = null;
			switch (rivet.tplStepType()) {
			case SEQUENCE://TODO:can be removed
				testStep = new Sequence();
				break;
			case MESSAGING_STEP:
				testStep = new MessagingStep();
				break;
			case DECISION_STEP:
				testStep = new DecisionStep();
				break;
			default:
				testStep = new TestStep();
				break;
			}
			
			testStep.setId(String.valueOf(rivet.tplStepId()));
			testStep.setDesc(rivet.tplStepDescription());
			sequence.getMsgOrDecisionOrLoop().add(testStep);
		}
		
		gitbTestCase.setSteps(sequence);
		
		gitbTestCase.setId(String.valueOf(minderTestCase.id));
		
		serviceResponse.setTestcase(gitbTestCase);
				
		/*
		 * Preparing response
		 */
		String responseValue = null;
		try {
            responseValue = contentProcessor.prepareResponse(GetTestCaseDefinitionResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		return ok(responseValue);
	}

	public static Result getActorDefinition() {

		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

//		ValidationRequest validationRequest = null;
//		try {
//			validationRequest = (ValidationRequest) contentProcessor
//					.parseRequest(ValidationRequest.class.getName());
//		} catch (ParseException e) {
//			return internalServerError(e.getMessage());
//
//		}
		String responseValue = null;
		
		System.out.println("responseValue:" + responseValue);

		return ok("responseValue");
	}

	public static Result initiate() {

		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

//		ValidationRequest validationRequest = null;
//		try {
//			validationRequest = (ValidationRequest) contentProcessor
//					.parseRequest(ValidationRequest.class.getName());
//		} catch (ParseException e) {
//			return internalServerError(e.getMessage());
//
//		}
		
		String responseValue = null;

		System.out.println("responseValue:" + responseValue);

		return ok("responseValue");
	}

	public static Result configure() {

		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

//		ValidationRequest validationRequest = null;
//		try {
//			validationRequest = (ValidationRequest) contentProcessor
//					.parseRequest(ValidationRequest.class.getName());
//		} catch (ParseException e) {
//			return internalServerError(e.getMessage());
//
//		}
		String responseValue = null;

		System.out.println("responseValue:" + responseValue);

		return ok("responseValue");
	}

	public static Result start() {

		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

//		ValidationRequest validationRequest = null;
//		try {
//			validationRequest = (ValidationRequest) contentProcessor
//					.parseRequest(ValidationRequest.class.getName());
//		} catch (ParseException e) {
//			return internalServerError(e.getMessage());
//
//		}
		
		String responseValue = null;

		System.out.println("responseValue:" + responseValue);

		return ok("responseValue");
	}

	public static Result stop() {

		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}

//		ValidationRequest validationRequest = null;
//		try {
//			validationRequest = (ValidationRequest) contentProcessor
//					.parseRequest(ValidationRequest.class.getName());
//		} catch (ParseException e) {
//			return internalServerError(e.getMessage());
//
//		}
		
		String responseValue = null;

		System.out.println("responseValue:" + responseValue);

		return ok("responseValue");
	}

}
