package rest.controllers;

import com.avaje.ebean.Ebean;
import com.gitb.core.v1.*;
import com.gitb.tbs.v1.*;
import com.gitb.tpl.v1.*;
import com.gitb.tr.v1.TestResultType;
import controllers.TestQueueController;
import minderengine.Visibility;
import models.*;
import models.TestCase;
import mtdl.MinderTdl;
import mtdl.Rivet;
import mtdl.TdlCompiler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.controllers.restbodyprocessor.XMLContentProcessor;
import rest.models.GetTestCaseDefinitions;
import scala.collection.JavaConversions;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GitbTestbedController extends Controller {

  TestQueueController testQueueController;

  @Inject
  public GitbTestbedController(TestQueueController testQueueController) {
    this.testQueueController = testQueueController;
  }

  public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  public Result getTestCaseDefinition() {
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

  public Result getActorDefinition() {

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

    //TODO: check if getting adapter lazy
    AdapterVersion adapterVersion = AdapterVersion.findById(Long.parseLong(actorDefinitionRequest.getActorId()));
    Adapter adapter = Adapter.findById(adapterVersion.adapter.id);

    GetActorDefinitionResponse serviceResponse = new GetActorDefinitionResponse();
    Actor actor = new Actor();
    actor.setId(adapterVersion.id.toString());
    actor.setName(adapter.name + "_" + adapterVersion.version);

    List<GitbEndpoint> gitbEndpoints = adapterVersion.gitbEndpoints;
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


  public Result initiate() {

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

    if (tdl == null)
      return internalServerError("Gitb test case not existed.");

    TestCase minderTestCase = TestCase.findById(tdl.testCase.id);

    GitbJob gitbJob = GitbJob.findByTdl(tdl);

    //if job is not existed, create one
    if (gitbJob == null) {
      gitbJob = new GitbJob();
      gitbJob.name = minderTestCase.name + "_" + tdl.version + "_job";
      gitbJob.tdl = tdl;
      gitbJob.owner = getCurrentUser(request());

      try {
        Ebean.beginTransaction();
        List<MappedAdapter> mappedAdapters = new ArrayList<>();
        gitbJob.mappedAdapters = mappedAdapters;
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

  public Result configure() {

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

  public Result start() {
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

    User currentUser = getCurrentUser(request());
    String replyToUrlAddress = request().getHeader(Constants.REPLY_TO_URL_ADDRESS);

    if (replyToUrlAddress == null || replyToUrlAddress.isEmpty())
      return internalServerError(Constants.REPLY_TO_URL_ADDRESS + " header tag and value should be set.");

    testQueueController.enqueueJobWithUser(Long.valueOf(basicCommand.getTcInstanceId()), currentUser, replyToUrlAddress, Visibility.PUBLIC);

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

  public Result stop() {

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

    User currentUser = getCurrentUser(request());
    testQueueController.cancelGitbJob(Long.valueOf(basicCommand.getTcInstanceId()), currentUser);

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

  public Result getGetTestCaseDefinitions() {

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

    if (tdls.size() == 0)
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

  private com.gitb.tpl.v1.TestCase getGitbTestCase(Tdl tdl) throws Exception {
    TestCase minderTestCase = TestCase.findById(tdl.testCase.id);

    //checks $adapter existence. these wappers are not applicable with gitb
    List<AdapterParam> adapterParams = AdapterParam.findByTestCase(tdl);
    if (adapterParams != null && !adapterParams.isEmpty())
      throw new Exception("This testcase is not compatible with GITB");

    com.gitb.tpl.v1.TestCase gitbTestCase = new com.gitb.tpl.v1.TestCase();

    //sets metadata
    Metadata metadata = new Metadata();
    metadata.setName(minderTestCase.name + "_" + tdl.version);
    metadata.setVersion(tdl.version);
    metadata.setAuthors(minderTestCase.owner.name);
    metadata.setDescription("");
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
      minderTdl = minderConstructor.newInstance(java.lang.Boolean.FALSE);
      if (minderTdl == null)
        throw new Exception("Cannot get adapterdefs.");
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
      throw new Exception(e1.toString());
    }

    //get adapters from tdl. minder adapter is equal to gitb actor
    Set<String> adapterDefs = JavaConversions.setAsJavaSet(minderTdl.adapterDefs());
    Roles actors = new Roles();

    //get adapters
    for (String adapterDef : adapterDefs) {
      if (!adapterDef.contains("$") && !adapterDef.equals("NULLADAPTER")) {
        Adapter adapter = Adapter.findByName(adapterDef);
        List<AdapterVersion> adapterVersions = AdapterVersion.getAllByAdapter(adapter);
        //set all adapter versions as actor
        for (AdapterVersion adapterVersion : adapterVersions) {
          TestRole testRole = new TestRole();
          testRole.setId(adapterVersion.id.toString());
          testRole.setName(adapter.name + "_" + adapterVersion.version);
          testRole.setRole(TestRoleEnumeration.SUT);

          List<GitbEndpoint> gitbEndpoints = adapterVersion.gitbEndpoints;
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

  private User getCurrentUser(Http.Request request) {
    String authorizationData = request.getHeader(AUTHORIZATION);

        /*
        * Parse client request
        */
    HashMap<String, String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);

    return User.findByEmail(clientRequest.get("username"));
  }

  public void performUpdateStatusOperation(String replyToUrlAddress, Long jobId, StepStatus stepStatus, Long stepId, String log) {
    try {

      TestStepStatus status = new TestStepStatus();
      status.setTcInstanceId(jobId.toString());
      status.setStepId(stepId.toString());
      status.setStatus(stepStatus);

      com.gitb.tr.v1.TAR report = new com.gitb.tr.v1.TAR();

      com.gitb.core.v1.AnyContent context = new com.gitb.core.v1.AnyContent();

      // set context
      context.setEmbeddingMethod(ValueEmbeddingEnumeration.STRING);
      context.setName("Rivet_Log");
      context.setValue(log);
      report.setContext(context);

      report.setDate(getNowAsXmlGregorianCalendar());
      if (stepStatus == StepStatus.ERROR)
        report.setResult(TestResultType.FAILURE);
      else
        report.setResult(TestResultType.SUCCESS);

      status.setReport(report);

      XMLContentProcessor contentProcessor = new XMLContentProcessor();
      String innerBody = contentProcessor.prepareResponse(
          TestStepStatus.class.getName(), status);

      innerBody = innerBody.substring(innerBody.indexOf('\n') + 1);

      String body = "";
      body += "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>";
      body += innerBody;
      body += "</soap:Body>";
      body += "</soap:Envelope>";

      HttpPost httppost = new HttpPost(replyToUrlAddress);

      // Request parameters and other properties.
      StringEntity stringentity = new StringEntity(body, "UTF-8");
      stringentity.setChunked(true);
      httppost.setEntity(stringentity);
      httppost.addHeader("Content-Type", "text/xml");
      httppost.addHeader("Accept", "*/*");
      httppost.addHeader("SOAPAction",
          "http://www.gitb.com/tbs/v1/TestbedClient/updateStatusRequest");
      // Execute and get the response.
      HttpClient httpclient = new DefaultHttpClient();
      HttpResponse response = httpclient.execute(httppost);
      HttpEntity entity = response.getEntity();

      String strresponse = null;
      if (entity != null) {
        strresponse = EntityUtils.toString(entity);
      }
      System.out.println(strresponse);

    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  private XMLGregorianCalendar getNowAsXmlGregorianCalendar() throws DatatypeConfigurationException {
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(new Date());
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
  }
}
