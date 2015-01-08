package controllers;

import builtin.Entry;
import models.*;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.*;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class InventoryController extends Controller {
  public static final Form<TestGroupEditorModel> TEST_GROUP_FORM = form(TestGroupEditorModel.class);
  public static final Form<TestAssertionEditorModel> TEST_ASSERTION_FORM = form(TestAssertionEditorModel.class);
  public static final Form<TestCaseEditorModel> TEST_CASE_FORM = form(TestCaseEditorModel.class);
  public static final Form<RunConfigurationEditorModel> RUN_CONFIGURATION_FORM = form(RunConfigurationEditorModel.class);


  public static class TestGroupEditorModel {
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.MinLength(10)
    @Constraints.MaxLength(50)
    public String shortDescription;

    public String description;
  }

  public static class TestAssertionEditorModel {
    public Long id;

    public Long groupId;

    @Constraints.Required
    public String taId;

    @Constraints.Required
    public String normativeSource;

    @Constraints.Required
    public String target;

    public String prerequisites;

    @Constraints.Required
    public String predicate;

    public String variables;

    public String tag;

    public String description;

    public String prescriptionLevel;
  }


  public static class TestCaseEditorModel {
    public Long id;

    public Long assertionId;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.MinLength(10)
    @Constraints.MaxLength(50)
    public String shortDescription;

    @Constraints.Required
    public String tdl;

    public String description;
  }

  public static class RunConfigurationEditorModel{
    public Long id;

    @Constraints.Required
    public String name;

    public Long testCaseId;

    public boolean obsolete;

    public String tdl;

    public List<Entry> mappedWrappers;
  }

  public static Result doCreateTestGroup() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM.bindFromRequest();
    final User localUser = Application.getLocalUser(session());
    if (filledForm.hasErrors()) {
      printFormErrors(filledForm);
      return badRequest(testGroupEditor.render(filledForm, null));
    } else {
      TestGroupEditorModel model = filledForm.get();

      TestGroup group = TestGroup.findByName(model.name);
      if (group != null) {
        filledForm.reject("The group with name [" + group.name + "] already exists");
        return badRequest(testGroupEditor.render(filledForm, null));
      }

      group = new TestGroup();

      group.owner = localUser;
      group.shortDescription = model.shortDescription;
      group.description = model.description;
      group.name = model.name;

      group.save();

      return ok(testGroupLister.render(Application.getLocalUser(session())));
    }
  }

  public static Result createNewGroupForm() {
    return ok(testGroupEditor.render(TEST_GROUP_FORM, null));
  }

  public static Result editGroupForm(Long id) {
    final User localUser = Application.getLocalUser(session());
    TestGroup tg = TestGroup.find.byId(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {
      TestGroupEditorModel tgem = new TestGroupEditorModel();
      tgem.id = tg.id;
      tgem.name = tg.name;
      tgem.shortDescription = tg.shortDescription;
      tgem.description = tg.description;
      Form<TestGroupEditorModel> bind = TEST_GROUP_FORM.fill(tgem);

      return ok(testGroupEditor.render(bind, null));
    }
  }

  public static Result doEditGroup() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      printFormErrors(filledForm);
      return badRequest(testGroupEditor.render(filledForm, null));
    } else {
      TestGroupEditorModel model = filledForm.get();
      TestGroup tg = TestGroup.find.byId(model.id);
      tg.name = model.name;
      tg.shortDescription = model.shortDescription;
      tg.description = model.description;
      tg.update();

      Logger.info("Done updating group " + tg.name + ":" + tg.id);
      return ok(testGroupLister.render(Application.getLocalUser(session())));
    }
  }

  public static Result doDeleteGroup(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    TestGroup tg = TestGroup.find.byId(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {
      try {
        tg.delete();
      } catch (Exception ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }
      return ok(testGroupLister.render(Application.getLocalUser(session())));
    }
  }

  /*
  Test Asertion CRUD
   */
  public static Result createAssertionForm(Long groupId) {
    TestGroup tg = TestGroup.find.byId(groupId);
    if (tg == null) {
      return badRequest("Test group with id [" + groupId + "] not found!");
    } else {
      TestAssertionEditorModel testAssertionEditorModel = new TestAssertionEditorModel();
      testAssertionEditorModel.groupId = groupId;
      Form<TestAssertionEditorModel> bind = TEST_ASSERTION_FORM.fill(testAssertionEditorModel);
      return ok(testAssertionEditor.render(bind, null));
    }
  }

  public static Result doCreateAssertion() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestAssertionEditorModel> filledForm = TEST_ASSERTION_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      printFormErrors(filledForm);
      return badRequest(testAssertionEditor.render(filledForm, null));
    } else {
      TestAssertionEditorModel model = filledForm.get();

      TestAssertion ta = TestAssertion.findByTaId(model.taId);
      if (ta != null) {
        filledForm.reject("The test assertion with ID [" + ta.taId + "] already exists");
        return badRequest(testAssertionEditor.render(filledForm, null));
      }

      TestGroup tg = TestGroup.findById(model.groupId);

      if (tg == null) {
        filledForm.reject("No group found with id [" + tg.id + "]");
        return badRequest(testAssertionEditor.render(filledForm, null));
      }

      ta = new TestAssertion();
      ta.taId = model.taId;
      ta.normativeSource = model.normativeSource;
      ta.predicate = model.predicate;
      ta.prerequisites = model.prerequisites;
      ta.target = model.target;
      ta.variables = model.variables;
      ta.tag = model.tag;
      ta.description = model.description;
      ta.testGroup = tg;
      ta.prescriptionLevel = PrescriptionLevel.valueOf(model.prescriptionLevel);

      ta.save();

      ta = TestAssertion.findByTaId(ta.taId);

      Logger.info("Assertion with id " + ta.id + ":" + ta.taId + " was created");
      return ok(testAssertionLister.render(tg, null));
    }
  }

  public static Result editAssertionForm(Long id) {
    TestAssertion ta = TestAssertion.find.byId(id);
    if (ta == null) {
      return badRequest("Test assertion with id [" + id + "] not found!");
    } else {
      TestAssertionEditorModel taModel = new TestAssertionEditorModel();
      taModel.id = id;
      taModel.taId = ta.taId;
      taModel.normativeSource = ta.normativeSource;
      taModel.target = ta.target;
      taModel.predicate = ta.predicate;
      taModel.prerequisites = ta.prerequisites;
      taModel.variables = ta.variables;
      taModel.tag = ta.tag;
      taModel.description = ta.description;
      taModel.groupId = ta.testGroup.id;
      taModel.prescriptionLevel =ta.prescriptionLevel.name();

      Form<TestAssertionEditorModel> bind = TEST_ASSERTION_FORM.fill(taModel);
      return ok(testAssertionEditor.render(bind, null));
    }
  }

  public static Result doEditAssertion() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestAssertionEditorModel> filledForm = TEST_ASSERTION_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      printFormErrors(filledForm);
      return badRequest(testAssertionEditor.render(filledForm, null));
    } else {
      TestAssertionEditorModel model = filledForm.get();

      TestAssertion ta = TestAssertion.findById(model.id);
      if (ta == null) {
        filledForm.reject("The test assertion with ID [" + model.id + "] does not exist");
        return badRequest(testAssertionEditor.render(filledForm, null));
      }

      ta.taId = model.taId;
      ta.normativeSource = model.normativeSource;
      ta.predicate = model.predicate;
      ta.prerequisites = model.prerequisites;
      ta.target = model.target;
      ta.variables = model.variables;
      ta.tag = model.tag;
      ta.description = model.description;
      ta.prescriptionLevel = PrescriptionLevel.valueOf(model.prescriptionLevel);


      //check if the name is not duplicate
      TestAssertion tmp = TestAssertion.findByTaId(model.taId);

      if (tmp == null || tmp.id == ta.id) {
        //either no such taId or it is already this object. so update
        ta.update();
        Logger.info("Assertion with id " + ta.id + ":" + ta.taId + " was updated");
        return ok(testAssertionLister.render(ta.testGroup, null));
      } else{
        filledForm.reject("The ID [" + model.taId + "] is used by another test assertion");
        return badRequest(testAssertionEditor.render(filledForm, null));
      }

    }
  }

  public static Result doDeleteAssertion(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    TestAssertion ta = TestAssertion.findById(id);
    if (ta == null) {
      //it does not exist. error
      return badRequest("Test assertion with id " + id + " does not exist.");
    }

    try {
      ta.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return ok(testAssertionLister.render(ta.testGroup, null));
  }

  /*
  test case CRUD
   */

  private static void printFormErrors(Form<?> filledForm) {
    Map<String, List<ValidationError>> errors = filledForm.errors();
    Set<String> set = errors.keySet();
    for (String key : set) {
      Logger.error("KEY");
      for (ValidationError ve : errors.get(key)) {
        Logger.error("\t" + ve.key() + ": " + ve.message());
      }
    }
  }

	public static Result createCaseForm(Long assertionId) {
		final User localUser = Application.getLocalUser(session());
		final List<Wrapper> allWrappers = Wrapper.getAll();
		return ok(testCaseEditor.render(localUser, TEST_CASE_FORM,assertionId,allWrappers));
	}

	public static Result doCreateCase() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
				.bindFromRequest();
		final User localUser = Application.getLocalUser(session());

		if (filledForm.hasErrors()) {
			printFormErrors(filledForm);
			return badRequest(testCaseEditor.render(localUser, filledForm, null,null));
		} else {
			TestCaseEditorModel model = filledForm.get();
			TestCase tc = new TestCase();
			tc.name = model.name;
			tc.shortDescription = model.shortDescription;
			tc.description = model.description;
			tc.tdl = model.tdl;
			tc.testAssertion = TestAssertion.findById(model.assertionId);

			TestCase tc2 = TestCase.findByName(model.name);
			if (tc2 == null) {
				tc.save();
				return ok(testCaseLister.render(tc.testAssertion, localUser));
			} else {
				filledForm.reject("The group with name [" + tc.name
						+ "] already exists");
				return badRequest(testCaseEditor.render(localUser, filledForm, model.assertionId,null));
			}
		}
	}

	public static Result editCaseForm(Long id) {
		return ok();
	}

	public static Result doEditCase() {
		return ok();
	}

	public static Result doDeleteCase(Long id) {
		return ok();
	}

  public static Result doDeleteRunConfiguration(Long id){
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    RunConfiguration rc = RunConfiguration.findById(id);
    if (rc == null) {
      //it does not exist. error
      return badRequest("Test assertion with id " + id + " does not exist.");
    }

    try {
      rc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    TestCase tc = TestCase.findById(rc.testCase.id);
    return ok(runConfigurationLister.render(tc, null));
  }

  public static Result editRunConfigurationForm(Long id){
    RunConfiguration rc = RunConfiguration.findById(id);
    if (rc == null) {
      //it does not exist. error
      return badRequest("RunConfiguration with id " + id + " does not exist.");
    }

    RunConfigurationEditorModel rome =  new RunConfigurationEditorModel();

    rome.id = rc.id;
    rome.name = rc.name;
    rome.tdl = rc.tdl;
    rome.testCaseId = rc.testCase.id;
    rome.mappedWrappers = new ArrayList<>();

    for (MappedWrapper mappedWrapper : rc.mappedWrappers) {
      rome.mappedWrappers.add(new Entry(mappedWrapper.parameter.name, mappedWrapper.wrapper.name));
    }

    Form<?> fill = RUN_CONFIGURATION_FORM.fill(rome);
    return ok(runConfigurationEditor.render(fill, null));
  }


  public static Result doEditRunConfiguration(){
    Form<RunConfigurationEditorModel>  frm = RUN_CONFIGURATION_FORM.bindFromRequest();


   //

    Map<String, String> data = frm.data();
    //for (Entry mappedWrapper : model.mappedWrappers) {
    //  System.out.println(mappedWrapper.getKey() + ": " + mappedWrapper.getValue());
    //}

    RunConfigurationEditorModel model = new RunConfigurationEditorModel();
    model.id = Long.parseLong(data.get("id"));
    model.testCaseId = Long.parseLong(data.get("testCaseId"));
    model.tdl  = data.get("tdl");
    model.mappedWrappers = new ArrayList<>();
    model.name = data.get("name");

    for (String s : data.keySet()) {
      System.out.println(s + " " + data.get(s));

      if(s.startsWith("$")){
        model.mappedWrappers.add(new Entry(s, data.get(s)));
      }
    }

    frm.fill(model);
    return ok(runConfigurationEditor.render(frm, null));
  }

  public static Result doRunRunconfiguration(Long id){
    /*if(id not startet){
      run(id)
      return ok(runningRIvetLister.render(id))
    }

    if(id not finished){
      return ok(runningRIvetLister.render(id))
    }

   */ return ok("TESTFINISH");
  }
}
