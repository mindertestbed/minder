package rest.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import minderengine.MinderSignalRegistry;
import minderengine.MinderWrapperRegistry;
import minderengine.SessionMap;
import models.GitbEndpoint;
import models.GitbJob;
import models.GitbParameter;
import models.MappedWrapper;
import models.TOperationType;
import models.Tdl;
import models.TestAssertion;
import models.TestCase;
import models.TestGroup;
import models.TestRun;
import models.User;
import models.UserHistory;
import models.Wrapper;
import models.WrapperParam;
import models.WrapperVersion;
import mtdl.MinderTdl;
import mtdl.SignalSlotInfoProvider;
import mtdl.TdlCompiler;
import mtdl.Rivet;

import com.avaje.ebean.Ebean;
import com.gitb.core.v1.Actor;
import com.gitb.core.v1.ActorConfiguration;
import com.gitb.core.v1.ConfigurationType;
import com.gitb.core.v1.Endpoint;
import com.gitb.core.v1.Metadata;
import com.gitb.core.v1.Parameter;
import com.gitb.core.v1.Roles;
import com.gitb.core.v1.TestRole;
import com.gitb.core.v1.TestRoleEnumeration;
import com.gitb.core.v1.UsageEnumeration;
import com.gitb.tbs.v1.BasicCommand;
import com.gitb.tbs.v1.BasicRequest;
import com.gitb.tbs.v1.ConfigureRequest;
import com.gitb.tbs.v1.ConfigureResponse;
import com.gitb.tbs.v1.GetActorDefinitionRequest;
import com.gitb.tbs.v1.GetActorDefinitionResponse;
import com.gitb.tbs.v1.GetTestCaseDefinitionResponse;
import com.gitb.tbs.v1.InitiateResponse;
import com.gitb.tpl.v1.DecisionStep;
import com.gitb.tpl.v1.MessagingStep;
import com.gitb.tpl.v1.Preliminary;
import com.gitb.tpl.v1.Sequence;
import com.gitb.tpl.v1.TestStep;

import controllers.TestRunContext;
import controllers.common.enumeration.OperationType;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.GetTestCaseDefinitions;
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
		
		com.gitb.tpl.v1.TestCase gitbTestCase;
		try {
			gitbTestCase = getGitbTestCase(tdl);
		} catch (Exception e) {
			return internalServerError(e.getMessage());
		}
		
		GetTestCaseDefinitionResponse serviceResponse = new GetTestCaseDefinitionResponse();
		
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

		response().setContentType(contentProcessor.getContentType());
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
		
		//TODO: check if getting wrapper lazy 
		WrapperVersion wrapperVersion = WrapperVersion.findById(Long.parseLong(actorDefinitionRequest.getActorId()));
		Wrapper wrapper = Wrapper.findById(wrapperVersion.wrapper.id);
	    
	    GetActorDefinitionResponse serviceResponse = new GetActorDefinitionResponse();
	    Actor actor = new Actor();
	    actor.setId(wrapperVersion.id.toString());
	    actor.setName(wrapper.name + "_" + wrapperVersion.version);
		
		List<GitbEndpoint> gitbEndpoints = wrapperVersion.gitbEndpoints;
		//set all endpoints
		for (GitbEndpoint gitbEndpoint : gitbEndpoints) {
			Endpoint endpoint = new Endpoint();
			endpoint.setName(gitbEndpoint.name);
			endpoint.setDesc(gitbEndpoint.description);
			
			List<GitbParameter> gitbParameters = gitbEndpoint.params;
			
			for (GitbParameter gitbParameter : gitbParameters) {
				Parameter parameter = new Parameter();
				parameter.setName(gitbParameter.name);
				parameter.setDesc(gitbParameter.description);
				parameter.setValue(gitbParameter.value);
				parameter.setKind(ConfigurationType.valueOf(gitbParameter.kind.toString()));
				parameter.setUse(UsageEnumeration.valueOf(gitbParameter.use.toString()));
				endpoint.getConfig().add(parameter);
			}
			actor.getEndpoint().add(endpoint);
		}
	    
		serviceResponse.setActor(actor);
		
		String responseValue = null;
		
		try {
            responseValue = contentProcessor.prepareResponse(GetActorDefinitionResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
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

		//Converting to http body to BasicRequest object
		BasicRequest basicRequest = null;
		try {
			basicRequest = (BasicRequest) contentProcessor.parseRequest(BasicRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		//tcId parameter matches tdl id.
		Tdl tdl = Tdl.findById(Long.parseLong(basicRequest.getTcId()));
		
		if(tdl == null)
			return internalServerError("Gitb test case not existed.");
		
		TestCase minderTestCase = TestCase.findById(tdl.testCase.id);
		
		GitbJob gitbJob = GitbJob.findByTdl(tdl);
		
		//if job is not existed, create one
		if(gitbJob == null)
		{
			gitbJob = new GitbJob();
			gitbJob.name = minderTestCase.name + "_" + tdl.version + "_job";
			gitbJob.tdl = tdl;
			gitbJob.owner = getCurrentUser(request());
		
		 try {
		      Ebean.beginTransaction();
		      List<MappedWrapper> mappedWrappers = new ArrayList<>();
		      gitbJob.mappedWrappers = mappedWrappers;
		      gitbJob.mtdlParameters = "";
		      gitbJob.save();
		      Ebean.commitTransaction();
		 } catch (Exception ex) {
		      ex.printStackTrace();
		      Ebean.endTransaction();
		      return internalServerError(ex.getMessage());
		    }
		}
		
		InitiateResponse serviceResponse = new InitiateResponse();
		
		serviceResponse.setTcInstanceId(String.valueOf(gitbJob.id));
		
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

		ConfigureRequest configureRequest = null;
		try {
			configureRequest = (ConfigureRequest) contentProcessor
					.parseRequest(ConfigureRequest.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());

		}
		
		String tcInstanceId = configureRequest.getTcInstanceId();
		List<ActorConfiguration> configurations = configureRequest.getConfigs();
		
		
		ConfigureResponse serviceResponse = new ConfigureResponse();		
		
		String responseValue = null;
		
		try {
            responseValue = contentProcessor.prepareResponse(ConfigureResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
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

		BasicCommand basicCommand = null;
		try {
			basicCommand = (BasicCommand) contentProcessor
					.parseRequest(BasicCommand.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());
		}
		
		GitbJob gitbJob = GitbJob.findById(Long.valueOf(basicCommand.getTcInstanceId()));
		
		if (gitbJob == null)
			return internalServerError("A job with id [" + basicCommand.getTcInstanceId() + "] was not found!");
		
		User currentUser = getCurrentUser(request());
		SessionMap.registerObject(currentUser.email, "signalRegistry", new MinderSignalRegistry());
        SignalSlotInfoProvider.setSignalSlotInfoProvider(MinderWrapperRegistry.get());

		TestRunContext testRunContext = createTestRun(gitbJob, currentUser);
		
		testRunContext.run();
		
		ConfigureResponse serviceResponse = new ConfigureResponse();		
		
		String responseValue = null;
		
		try {
            responseValue = contentProcessor.prepareResponse(ConfigureResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		
		return ok(responseValue);	
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

		BasicCommand basicCommand = null;
		try {
			basicCommand = (BasicCommand) contentProcessor
					.parseRequest(BasicCommand.class.getName());
		} catch (ParseException e) {
			return internalServerError(e.getMessage());

		}
		
		GitbJob gitbJob = GitbJob.findById(Long.valueOf(basicCommand.getTcInstanceId()));
		
		ConfigureResponse serviceResponse = new ConfigureResponse();		
		
		String responseValue = null;
		
		try {
            responseValue = contentProcessor.prepareResponse(ConfigureResponse.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }

		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		
		return ok(responseValue);
	}
	
	public static Result getGetTestCaseDefinitions() {

		/*
		 * Handling the request message
		 */
		IRestContentProcessor contentProcessor = null;
		try {
			contentProcessor = RestUtils.createContentProcessor(request().getHeader(CONTENT_TYPE), request().body());
		} catch (IllegalArgumentException e) {
			return badRequest(e.getMessage());
		}
	
		//tcId parameter matches tdl id.
		List<Tdl> tdls = Tdl.getAllUnparametricTdls();
		List<com.gitb.tpl.v1.TestCase> testCases = new ArrayList<com.gitb.tpl.v1.TestCase>();
		
		if(tdls.size() == 0)
			return internalServerError("There is not any gitb test cases.");
		
		try {
			for (Tdl tdl : tdls) {
				testCases.add(getGitbTestCase(tdl));
			}
		} catch (Exception e) {
			return internalServerError(e.getMessage());
		}
		
		GetTestCaseDefinitions serviceResponse = new GetTestCaseDefinitions();
		serviceResponse.setTestcases(testCases);
		
		String responseValue = null;

		try {
            responseValue = contentProcessor.prepareResponse(GetTestCaseDefinitions.class.getName(), serviceResponse);
        } catch (ParseException e) {
            return internalServerError(e.getMessage());
        }
		
		System.out.println("responseValue:" + responseValue);

		response().setContentType(contentProcessor.getContentType());
		return ok(responseValue);
	}
	
	private static com.gitb.tpl.v1.TestCase getGitbTestCase(Tdl tdl) throws Exception
	{
		TestCase minderTestCase = TestCase.findById(tdl.testCase.id);
		
		//checks $wrapper existence. these wappers are not applicable with gitb
		List<WrapperParam> wrapperParams = WrapperParam.findByTestCase(tdl);
		if(wrapperParams != null && !wrapperParams.isEmpty())
			throw new Exception("This testcase is not compatible with GITB");
		
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
				throw new Exception("Cannot get wrapperdefs.");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			throw new Exception(e1.toString());
		}
			
		//get wrappers from tdl. minder wrapper is equal to gitb actor 
		Set<String> wrapperDefs = JavaConversions.setAsJavaSet(minderTdl.wrapperDefs());
		Roles actors = new Roles();
		
		//get wrappers
		for (String wrapperDef : wrapperDefs) {
			if(!wrapperDef.contains("$") && !wrapperDef.equals("NULLWRAPPER"))
			{
				Wrapper wrapper = Wrapper.findByName(wrapperDef);
				List<WrapperVersion> wrapperVersions = WrapperVersion.getAllByWrapper(wrapper);
				//set all wrapper versions as actor
				for (WrapperVersion wrapperVersion : wrapperVersions) {
					TestRole testRole = new TestRole();
					testRole.setId(wrapperVersion.id.toString());
					testRole.setName(wrapper.name + "_" + wrapperVersion.version);
					testRole.setRole(TestRoleEnumeration.SUT);
					
					List<GitbEndpoint> gitbEndpoints = wrapperVersion.gitbEndpoints;
					//set all endpoints
					for (GitbEndpoint gitbEndpoint : gitbEndpoints) {
						Endpoint endpoint = new Endpoint();
						endpoint.setName(gitbEndpoint.name);
						endpoint.setDesc(gitbEndpoint.description);
						
						List<GitbParameter> gitbParameters = gitbEndpoint.params;
						
						for (GitbParameter gitbParameter : gitbParameters) {
							Parameter parameter = new Parameter();
							parameter.setName(gitbParameter.name);
							parameter.setDesc(gitbParameter.description);
							parameter.setValue(gitbParameter.value);
							parameter.setKind(ConfigurationType.valueOf(gitbParameter.kind.toString()));
							parameter.setUse(UsageEnumeration.valueOf(gitbParameter.use.toString()));
							endpoint.getConfig().add(parameter);
						}
						testRole.getEndpoint().add(endpoint);
					}
					
					actors.getActor().add(testRole);
				}
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
		gitbTestCase.setId(String.valueOf(tdl.id));
		
		return gitbTestCase;
	}
	
	private static User getCurrentUser(Http.Request request)
	{
		String authorizationData = request.getHeader(AUTHORIZATION);

        /*
        * Parse client request
        */   
		 HashMap<String,String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);
	        
		return User.findByEmail(clientRequest.get("username"));
	}
	
	
	private static TestRunContext createTestRun(GitbJob job, User user)
	{
		TestRun testRun = new TestRun();		
		testRun.date = new Date();				    
		testRun.job = job;
		
		UserHistory userHistory = new UserHistory();				    
		userHistory.email = user.email;				    
		userHistory.operationType = new TOperationType();
		userHistory.operationType.name = OperationType.RUN_TEST_CASE;
				    
		userHistory.operationType.save();

		testRun.history = userHistory;				    
		testRun.runner = user;
				    
		return new TestRunContext(testRun);
	}

}
