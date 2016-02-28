package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.enumeration.Utils;
import editormodels.TestSuiteEditorModel;
import global.Util;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.testSuite.mainView;
import views.html.testSuite.childViews.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class TestSuiteController extends Controller {
  public static final Form<TestSuiteEditorModel> TEST_SUITE_FORM = form(TestSuiteEditorModel.class);

  /*
   * Test Asertion CRUD
   */
  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result getCreateTestSuiteView(Long groupId) {
    TestGroup tg = TestGroup.findById(groupId);
    if (tg == null) {
      return badRequest("Test group with id [" + groupId + "] not found!");
    } else {
      TestSuiteEditorModel testSuiteEditorModel = new TestSuiteEditorModel();
      testSuiteEditorModel.groupId = groupId;
      Form<TestSuiteEditorModel> bind = TEST_SUITE_FORM
          .fill(testSuiteEditorModel);
      return ok(creator.render(bind));
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doCreateTestSuite() {
    final Form<TestSuiteEditorModel> filledForm = TEST_SUITE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testSuiteForm.render(filledForm, false));
    } else {
      TestSuiteEditorModel model = filledForm.get();
      TestGroup tg = TestGroup.findById(model.groupId);

      if (tg == null) {
        filledForm.reject("No group found with id [" + tg.id + "]");
        return badRequest(testSuiteForm.render(filledForm, false));
      }
      if (TestSuite.findByGroupAndName(tg, model.name) != null) {
        filledForm.reject("A test suite with the same name already exists");
        return badRequest(testSuiteForm.render(filledForm, false));
      }

      try {
        Ebean.beginTransaction();
        TestSuite testSuite = new TestSuite();
        testSuite.name = model.name;
        testSuite.shortDescription = model.shortDescription;
        testSuite.testGroup = tg;
        testSuite.visibility = model.visibility;
        testSuite.mtdlParameters = model.mtdlParameters;
        testSuite.preemptionPolicy = model.preemptionPolicy;
        testSuite.owner = Authentication.getLocalUser();
        testSuite.save();

        JsonNode tdlArray = Json.parse(model.tdlArray);
        JsonNode selectedCandidateMap = Json.parse(model.selectedCandidateMap);
        System.out.println(tdlArray);
        System.out.println(selectedCandidateMap);

        updateSuiteJobs(tdlArray, selectedCandidateMap, testSuite);

        Logger.debug("TestSuite with id " + testSuite.id + " was created");
        Ebean.commitTransaction();
        return ok("");
      } finally {
        Ebean.endTransaction();
      }
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditTestSuite() {
    final Form<TestSuiteEditorModel> filledForm = TEST_SUITE_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testSuiteForm.render(filledForm, true));
    } else {
      TestSuiteEditorModel model = filledForm.get();

      TestSuite testSuite = TestSuite.findById(model.id);
      if (testSuite == null) {
        filledForm.reject("The test TestSuite with ID [" + model.id
            + "] does not exist");
        return badRequest(testSuiteForm.render(filledForm, true));
      }

      if (!Util.canAccess(Authentication.getLocalUser(), testSuite.owner))
        return unauthorized("You don't have permission to modify this resource");

      try {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        Ebean.beginTransaction();
        testSuite.name = model.name;
        testSuite.shortDescription = model.shortDescription;
        testSuite.visibility = model.visibility;
        testSuite.mtdlParameters = model.mtdlParameters;
        testSuite.preemptionPolicy = model.preemptionPolicy;
        testSuite.owner = Authentication.getLocalUser();
        testSuite.save();

        JsonNode tdlArray = Json.parse(model.tdlArray);
        JsonNode selectedCandidateMap = Json.parse(model.selectedCandidateMap);
        System.out.println(tdlArray);
        System.out.println(selectedCandidateMap);

        updateSuiteJobs(tdlArray, selectedCandidateMap, testSuite);

        Logger.debug("TestSuite with id " + testSuite.id + " was updated");
        Ebean.commitTransaction();
        return ok("");
      } finally {
        Ebean.endTransaction();
      }

    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result renderTestRunListView(Long testSuiteId) {
    return ok("atas");
  }

  public static Form<TestSuiteEditorModel> fillEditorModelForm(TestSuite ts) {
    TestSuiteEditorModel tsModel = new TestSuiteEditorModel();
    tsModel.id = ts.id;
    tsModel.name = ts.name;
    tsModel.shortDescription = ts.shortDescription;
    tsModel.groupId = ts.testGroup.id;
    tsModel.mtdlParameters = ts.mtdlParameters;
    tsModel.visibility = ts.visibility;
    tsModel.preemptionPolicy = ts.preemptionPolicy;

    //now prepare the tdl array and mapped parameters.
    //a sample tdlarray
    //    [3,4,1]
    //a sample candidate map
    //    {"$as4-adapter":4,"$corner1":5,"$corner4":5,"$generator":10,"$initiator":11}

    List<SuiteJob> jobs = SuiteJob.getAllByTestSuite(ts);
    StringBuilder tdlArrayBuilder = new StringBuilder("[");
    StringBuilder candidateMapBuilder = new StringBuilder("{");
    jobs.forEach(job -> {
      tdlArrayBuilder.append(job.tdl.id).append(',');
      MappedWrapper.findByJob(job).forEach(mappedWrapper -> {
        candidateMapBuilder.append('\"').append(mappedWrapper.parameter.name).append("\":").append(mappedWrapper.wrapperVersion.id).append(',');
      });
    });
    if (tdlArrayBuilder.length() > 1) {
      tdlArrayBuilder.deleteCharAt(tdlArrayBuilder.length() - 1); //remove the extra comma
    }
    if (candidateMapBuilder.length() > 1) {
      candidateMapBuilder.deleteCharAt(candidateMapBuilder.length() - 1); //remove the extra comma
    }
    tdlArrayBuilder.append(']');
    candidateMapBuilder.append('}');
    tsModel.tdlArray = tdlArrayBuilder.toString();
    tsModel.selectedCandidateMap = candidateMapBuilder.toString();

    return TEST_SUITE_FORM.fill(tsModel);
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doDeleteTestSuite(Long id) {
    TestSuite ta = TestSuite.findById(id);
    if (ta == null) {
      // it does not exist. error
      return badRequest("Test TestSuite with id " + id
          + " does not exist.");
    }

    if (!Util.canAccess(Authentication.getLocalUser(), ta.owner))
      return badRequest("You don't have permission to modify this resource");

    try {
      ta.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return redirect(routes.GroupController.getGroupDetailView(ta.testGroup.id, "suites"));
  }

  private static void updateSuiteJobs(JsonNode tdlArray, JsonNode selectedCandidateMap, TestSuite testSuite) {
    //find and remove all suite jobs for this test suite.
    SuiteJob.getAllByTestSuite(testSuite).forEach(suiteJob -> {
      if (TestRun.countRunsForJob(suiteJob) > 0) {
        suiteJob.isObsolete = true;
        suiteJob.save();
      } else {
        MappedWrapper.deleteByJob(suiteJob);
        AbstractJob.deleteById(suiteJob.id);
      }
    });

    tdlArray.forEach(node -> {
      Tdl tdl = Tdl.findById(node.asLong());
      SuiteJob sj = new SuiteJob();
      sj.testSuite = testSuite;
      sj.mtdlParameters = testSuite.mtdlParameters;
      sj.name = testSuite.name + "-" + tdl.testCase.name;
      sj.visibility = testSuite.visibility;
      sj.tdl = tdl;
      sj.save();
      sj.owner = testSuite.owner;
      tdl.parameters.forEach(param -> {
        MappedWrapper mw = new MappedWrapper();
        mw.job = sj;
        mw.parameter = param;
        long wrapperVersionId = selectedCandidateMap.findPath(param.name).asLong();
        mw.wrapperVersion = WrapperVersion.findById(wrapperVersionId);
        mw.save();
      });
    });
  }

  public static Result getTestSuiteDetailView(Long id, String display) {
    TestSuite testSuite = TestSuite.findById(id);
    if (testSuite == null) {
      return badRequest("No test TestSuite with id " + id + ".");
    }
    return ok(mainView.render(testSuite, display));
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result doEditTestSuiteField() {
    JsonNode jsonNode = request().body().asJson();

    return Utils.doEditField(TestSuiteEditorModel.class, TestSuite.class, jsonNode, Authentication.getLocalUser());
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result renderDetailView(long id) {
    return ok(views.html.testSuite.childViews.details.render(TestSuite.findById(id)));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public static Result getNamesAndAdaptersForTdls() {
    System.out.println(request().body().asJson());
    JsonNode json = request().body().asJson();
    System.out.println(json);
    System.out.println(json.isArray());

    List<Tdl> tdls = new ArrayList<>();
    if (json.isArray()) {
      json.forEach(value -> {
        tdls.add(Tdl.findById(value.asLong()));
      });
    } else {
      long tdlId = json.asLong();
      Tdl tdl = Tdl.findById(tdlId);
      tdls.add(tdl);
    }

    Map<String, Set<WrapperVersion>> wrapperParamListHashMap = Util.listCandidateAdapters(tdls);

    return ok(Json.toJson(wrapperParamListHashMap));
  }

  public static List<TestAssertion> listAllAssertionsWithTDLS(TestGroup group) {
    List<TestAssertion> list = TestAssertion.findByGroup(group);

    for (TestAssertion ta : list) {
      ta.testCases = TestCase.listByTestAssertion(ta);
      for (TestCase tc : ta.testCases) {
        tc.tdls = Tdl.findByTestCase(tc);
      }
    }
    return list;
  }
}
