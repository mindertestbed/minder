package rest.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import models.Tdl;
import models.TestAssertion;
import models.TestCase;
import models.TestGroup;
import models.Wrapper;
import models.WrapperParam;
import models.WrapperVersion;
import mtdl.MinderTdl;
import mtdl.TdlCompiler;
import mtdl.Rivet;

import com.gitb.core.v1.Actor;
import com.gitb.core.v1.Metadata;
import com.gitb.core.v1.Roles;
import com.gitb.core.v1.TestRole;
import com.gitb.core.v1.TestRoleEnumeration;
import com.gitb.tbs.v1.BasicRequest;
import com.gitb.tbs.v1.GetActorDefinitionRequest;
import com.gitb.tbs.v1.GetActorDefinitionResponse;
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
import scala.collection.JavaConversions;

public class GitbTestbedController extends Controller {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
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

		//Converting to http body to BasicRequest object
		BasicRequest basicRequest = null;
		try {
			basicRequest = (BasicRequest) contentProcessor.parseRequest(BasicRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		//tcId parameter matches tdl id.
		Tdl tdl = Tdl.findById(Long.parseLong(basicRequest.getTcId()));
		TestCase minderTestCase = TestCase.findById(tdl.testCase.id);
		
		//checks $wrapper existence. these wappers are not applicable with gitb
		List<WrapperParam> wrapperParams = WrapperParam.findByTestCase(tdl);
		if(wrapperParams != null && !wrapperParams.isEmpty())
			return internalServerError("This testcase is not compatible with GITB");
		
	    GetTestCaseDefinitionResponse serviceResponse = new GetTestCaseDefinitionResponse();
		
	    com.gitb.tpl.v1.TestCase gitbTestCase = new com.gitb.tpl.v1.TestCase(); 
	    
	    //sets metadata
	    Metadata metadata = new Metadata();
		metadata.setName(minderTestCase.name + "_" + tdl.version);
		metadata.setVersion(tdl.version);
		metadata.setAuthors(minderTestCase.owner.name);
		metadata.setDescription(minderTestCase.shortDescription);
		metadata.setPublished(dateFormat.format(tdl.creationDate));
		metadata.setLastModified(dateFormat.format(tdl.creationDate));
		
		gitbTestCase.setMetadata(metadata);
		
		//preliminary is not supported so that set null
		gitbTestCase.setPreliminary(new Preliminary());
		
		//compiles tdl script and interprets it
		TestAssertion testAssertion = TestAssertion.findById(minderTestCase.testAssertion.id);		
		TestGroup testGroup = TestGroup.findById(testAssertion.testGroup.id);  
		String packageRoot = "_" + testGroup.id;  
		String packagePath = packageRoot + "/_" + minderTestCase.id;  
		Class<MinderTdl> cls = TdlCompiler.compileTdl(packageRoot, packagePath, testGroup.dependencyString, minderTestCase.name, tdl.tdl, tdl.version);
		
		//initializes MinderTdl class
		MinderTdl minderTdl = null;
		try {
			Constructor<MinderTdl> minderConstructor = (Constructor<MinderTdl>) cls.getConstructors()[0];
			minderTdl = minderConstructor.newInstance(null, java.lang.Boolean.FALSE);
			if(minderTdl == null)
				return internalServerError("Cannot get wrapperdefs.");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			return internalServerError(e1.toString());
		}
			
		//get wrappers from tdl. minder wrapper is equal to gitb actor 
		Set<String> wrapperDefs = JavaConversions.setAsJavaSet(minderTdl.wrapperDefs());
		Roles actors = new Roles();
		
		//set actors
		for (String wrapperDef : wrapperDefs) {
			if(!wrapperDef.contains("$") && !wrapperDef.equals("NULLWRAPPER"))
			{
				Wrapper wrapper = Wrapper.findByName(wrapperDef);
				WrapperVersion wrapperVersion = WrapperVersion.findById(wrapper.id);
				TestRole testRole = new TestRole();
				testRole.setId(wrapperVersion.id.toString());
				//TODO:endpoind set et.
				testRole.setName(wrapper.name);
				testRole.setRole(TestRoleEnumeration.SUT);
				actors.getActor().add(testRole);
			}
		}
		
		gitbTestCase.setActors(actors);
		
		//get rivets
		Sequence sequence = new Sequence();
		List<Rivet> rivets = minderTdl.RivetDefs();
		
		//get sequences from rivets and set
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
		
		//set test case id
		gitbTestCase.setId(basicRequest.getTcId());
		
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

		GetActorDefinitionRequest actorDefinitionRequest = null;
		try {
			actorDefinitionRequest = (GetActorDefinitionRequest) contentProcessor
					.parseRequest(GetActorDefinitionRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());

		}
		
		String actorId = actorDefinitionRequest.getActorId();
		
		WrapperParam expectedWrapper = WrapperParam.findById(Long.parseLong(actorId));
		
		String tcId = actorDefinitionRequest.getTcId();
		
		TestCase minderTestCase = TestCase.findById(Long.parseLong(tcId));
		List<Tdl> list = Tdl.findByTestCase(minderTestCase);

	    if (list.size() == 0)
	      return internalServerError("TDL not exist.");;

	    Tdl newestTdl = list.get(0);
	    List<WrapperParam> wrapperParams = WrapperParam.findByTestCase(newestTdl);
	    
	    WrapperParam actualParam = null;
	    for (WrapperParam wrapperParam : wrapperParams) {
			if(wrapperParam.id.longValue() == expectedWrapper.id.longValue())
			{
				actualParam = wrapperParam;
				break;
			}
		}
	    
	    if(actualParam == null)	    
	    	return internalServerError("Given actor id is not existed: " + expectedWrapper.id.longValue());
	    
	    GetActorDefinitionResponse actorDefinitionResponse = new GetActorDefinitionResponse();
	    Actor actor = new Actor();
	    actor.setId(expectedWrapper.id.toString());
	    //actor.setDesc(expectedWrapper.);
	    
	    //actorDefinitionResponse.setActor(value);
		
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
