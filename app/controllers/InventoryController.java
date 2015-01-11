package controllers;

import com.avaje.ebean.Ebean;
import models.*;
import mtdl.SignalSlot;
import mtdl.TdlCompiler;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

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
		
	}

	public static class RunConfigurationEditorModel {
		public Long id;

		@Constraints.Required
		public String name;

		public Long testCaseId;

		public boolean obsolete;

		public String tdl;

		public List<MappedWrapperModel> mappedWrappers;
	}

	public static Result doCreateTestGroup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM
				.bindFromRequest();
		final User localUser = Application.getLocalUser(session());
		if (filledForm.hasErrors()) {
			printFormErrors(filledForm);
			return badRequest(testGroupEditor.render(filledForm, null));
		} else {
			TestGroupEditorModel model = filledForm.get();

			TestGroup group = TestGroup.findByName(model.name);
			if (group != null) {
				filledForm.reject("The group with name [" + group.name
						+ "] already exists");
				return badRequest(testGroupEditor.render(filledForm, null));
			}

			group = new TestGroup();

			group.owner = localUser;
			group.shortDescription = model.shortDescription;
			group.description = model.description;
			group.name = model.name;

			group.save();

			return ok(testGroupLister.render(Application
					.getLocalUser(session())));
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

		final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM
				.bindFromRequest();

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
			return ok(testGroupLister.render(Application
					.getLocalUser(session())));
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
			return ok(testGroupLister.render(Application
					.getLocalUser(session())));
		}
	}

	/*
	 * Test Asertion CRUD
	 */
	public static Result createAssertionForm(Long groupId) {
		TestGroup tg = TestGroup.find.byId(groupId);
		if (tg == null) {
			return badRequest("Test group with id [" + groupId + "] not found!");
		} else {
			TestAssertionEditorModel testAssertionEditorModel = new TestAssertionEditorModel();
			testAssertionEditorModel.groupId = groupId;
			Form<TestAssertionEditorModel> bind = TEST_ASSERTION_FORM
					.fill(testAssertionEditorModel);
			return ok(testAssertionEditor.render(bind, null));
		}
	}

	public static Result doCreateAssertion() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestAssertionEditorModel> filledForm = TEST_ASSERTION_FORM
				.bindFromRequest();

		if (filledForm.hasErrors()) {
			printFormErrors(filledForm);
			return badRequest(testAssertionEditor.render(filledForm, null));
		} else {
			TestAssertionEditorModel model = filledForm.get();

			TestAssertion ta = TestAssertion.findByTaId(model.taId);
			if (ta != null) {
				filledForm.reject("The test assertion with ID [" + ta.taId
						+ "] already exists");
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
			ta.prescriptionLevel = PrescriptionLevel
					.valueOf(model.prescriptionLevel);

			ta.save();

			ta = TestAssertion.findByTaId(ta.taId);

			Logger.info("Assertion with id " + ta.id + ":" + ta.taId
					+ " was created");
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
			taModel.prescriptionLevel = ta.prescriptionLevel.name();

			Form<TestAssertionEditorModel> bind = TEST_ASSERTION_FORM
					.fill(taModel);
			return ok(testAssertionEditor.render(bind, null));
		}
	}

	public static Result doEditAssertion() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestAssertionEditorModel> filledForm = TEST_ASSERTION_FORM
				.bindFromRequest();

		if (filledForm.hasErrors()) {
			printFormErrors(filledForm);
			return badRequest(testAssertionEditor.render(filledForm, null));
		} else {
			TestAssertionEditorModel model = filledForm.get();

			TestAssertion ta = TestAssertion.findById(model.id);
			if (ta == null) {
				filledForm.reject("The test assertion with ID [" + model.id
						+ "] does not exist");
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
			ta.prescriptionLevel = PrescriptionLevel
					.valueOf(model.prescriptionLevel);

			// check if the name is not duplicate
			TestAssertion tmp = TestAssertion.findByTaId(model.taId);

			if (tmp == null || tmp.id == ta.id) {
				// either no such taId or it is already this object. so update
				ta.update();
				Logger.info("Assertion with id " + ta.id + ":" + ta.taId
						+ " was updated");
				return ok(testAssertionLister.render(ta.testGroup, null));
			} else {
				filledForm.reject("The ID [" + model.taId
						+ "] is used by another test assertion");
				return badRequest(testAssertionEditor.render(filledForm, null));
			}

		}
	}

	public static Result doDeleteAssertion(Long id) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		TestAssertion ta = TestAssertion.findById(id);
		if (ta == null) {
			// it does not exist. error
			return badRequest("Test assertion with id " + id
					+ " does not exist.");
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
	 * test case CRUD
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
		TestAssertion ta = TestAssertion.find.byId(assertionId);
		if (ta == null) {
			return badRequest("Test assertion with id [" + assertionId
					+ "] not found!");
		} else {
			TestCaseEditorModel testCaseEditorModel = new TestCaseEditorModel();
			testCaseEditorModel.assertionId = assertionId;

			Form<TestCaseEditorModel> bind = TEST_CASE_FORM
					.fill(testCaseEditorModel);
			return ok(testCaseEditor.render(bind, null));
		}
	}

	public static Result doCreateCase() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
				.bindFromRequest();

		if (filledForm.hasErrors()) {
			printFormErrors(filledForm);
			return badRequest(testCaseEditor.render(filledForm, null));
		} else {
			TestCaseEditorModel model = filledForm.get();

			TestCase tc = TestCase.findByName(model.name);
			if (tc != null) {
				filledForm.reject("The test case with name [" + tc.name
						+ "] already exists");
				return badRequest(testCaseEditor.render(filledForm, null));
			}

			TestAssertion ta = TestAssertion.findById(model.assertionId);
			if (ta == null) {
				filledForm.reject("No assertion found with id [" + ta.id + "]");
				return badRequest(testCaseEditor.render(filledForm, null));
			}

			final User localUser = Application.getLocalUser(session());

			tc = new TestCase();
			tc.name = model.name;
			tc.tdl = model.tdl;
			tc.shortDescription = model.shortDescription;
			tc.testAssertion = ta;

			tc.save();

			tc = TestCase.findByName(tc.name);

			Logger.info("Test Case with name " + tc.id + ":" + tc.name
					+ " was created");
			return ok(testCaseLister.render(ta, null));
		}
	}

	public static Result editCaseForm(Long id) {
		TestCase tc = TestCase.find.byId(id);
		if (tc == null) {
			return badRequest("Test case with id [" + id + "] not found!");
		} else {
			TestCaseEditorModel tcModel = new TestCaseEditorModel();
			tcModel.id = id;
			tcModel.assertionId = tc.testAssertion.id;
			tcModel.name = tc.name;
			tcModel.shortDescription = tc.shortDescription;
			tcModel.tdl = tc.tdl;
			tcModel.description = tc.description;

			Form<TestCaseEditorModel> bind = TEST_CASE_FORM.fill(tcModel);
			return ok(testCaseEditor.render(bind, null));
		}
	}

	public static Result doEditCase() {
		System.out.println("doEditCase\n");
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM.bindFromRequest();

		if (filledForm.hasErrors()) {
			printFormErrors(filledForm);
			return badRequest(testCaseEditor.render(filledForm, null));
		} else {
			TestCaseEditorModel model = filledForm.get();

			TestCase tc = TestCase.findById(model.id);
			if (tc == null) {
				filledForm.reject("The test case with ID [" + model.id
						+ "] does not exist");
				return badRequest(testCaseEditor.render(filledForm, null));
			}

			tc.description = model.description;
			tc.name = model.name;
			tc.shortDescription = model.shortDescription;
			tc.setTdl(model.tdl);
			

			// check if the name is not duplicate
			TestCase tmp = TestCase.findByName(model.name);

			if (tmp == null || tmp.id == tc.id) {
				// either no such name or it is already this object. so update
				tc.update();
				Logger.info("Test Case with id " + tc.id + ":" + tc.name
						+ " was updated");
				return ok(testCaseLister.render(tc.testAssertion, null));
			} else {
				filledForm.reject("The ID [" + model.name
						+ "] is used by another test case");
				return badRequest(testCaseEditor.render(filledForm, null));
			}

		}
	}

	public static Result doDeleteCase(Long id) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		TestCase tc = TestCase.findById(id);
		if (tc == null) {
			// it does not exist. error
			return badRequest("Test case with id " + id
					+ " does not exist.");
		}
			tc.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.error(ex.getMessage(), ex);
			return badRequest(ex.getMessage());
		}
		return ok(testCaseLister.render(tc.testAssertion, null));
	}
  public static Result createRunConfigurationForm(Long testCaseId) {
    System.out.println("Request run configuration editor form");
    TestCase testCase = TestCase.findById(testCaseId);


    System.out.println("testCaseNull " + (testCase == null));

    if (testCase == null)
      return badRequest("Test case with id " + testCaseId + " couldn't be found");

    int max = 0;

    List<RunConfiguration> list = RunConfiguration.findByTestCase(testCase);
    if (list != null) {
      for (RunConfiguration runConfiguration : list) {
        if (runConfiguration.name.matches(testCase.name + "\\(\\d+\\)$")) {
          int val = Integer.parseInt(runConfiguration.name.substring(runConfiguration.name.lastIndexOf('(') + 1,
              runConfiguration.name.lastIndexOf(')')));

          if (max < val)
            max = val;
        }
      }
    }

    RunConfigurationEditorModel model = new RunConfigurationEditorModel();
    model.testCaseId = testCaseId;
    model.name = testCase.name + "(" + (max + 1) + ")";


    //
    model.mappedWrappers = new ArrayList<>();

    System.out.println("Test Case Parameters. length " + testCase.parameters.size());
    for (WrapperParam parameter : testCase.parameters) {
      System.out.println(parameter.name);
      model.mappedWrappers.add(new MappedWrapperModel(null, parameter.id, parameter.name, ""));
    }
    return ok(runConfigurationEditor.render(RUN_CONFIGURATION_FORM.fill(model), null));
  }

  public static Result doCreateRunConfiguration() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    Form<RunConfigurationEditorModel> form = RUN_CONFIGURATION_FORM.bindFromRequest();

    if (form.hasErrors()) {
      printFormErrors(form);
      return badRequest(runConfigurationEditor.render(form, null));
    }

    RunConfigurationEditorModel model = form.get();

    //check if we have a repetition
    TestCase testCase = TestCase.findById(model.testCaseId);
    RunConfiguration existing = RunConfiguration.findByTestCaseAndName(testCase, model.name);

    if (existing != null) {
      form.reject("A run configuration with the name [" + model.name + "] already exists");
      return badRequest(runConfigurationEditor.render(form, null));
    }

    //check the parameters.
    for (MappedWrapperModel mappedWrapper : model.mappedWrappers) {
      if (mappedWrapper.value == null || mappedWrapper.value.equals("")) {
        form.reject("You have to fill all parameters");
        return badRequest(runConfigurationEditor.render(form, null));
      }
    }

    //everything is tip-top. So save
    RunConfiguration rc = new RunConfiguration();
    rc.name = model.name;
    rc.testCase = testCase;
    rc.obsolete = false;
    rc.tdl = testCase.tdl;

    try {
      Ebean.beginTransaction();
      List<MappedWrapper> mappedWrappers = new ArrayList<>();
      for (MappedWrapperModel mappedWrapper : model.mappedWrappers) {
        MappedWrapper mw = new MappedWrapper();
        mw.parameter = WrapperParam.findByTestCaseAndName(testCase, mappedWrapper.name);
        mw.runConfiguration = rc;
        mw.wrapper = Wrapper.findByName(mappedWrapper.value);
        mw.save();
      }

      rc.mappedWrappers = mappedWrappers;
      rc.save();
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();

      form.reject(ex.getMessage());
      return badRequest(runConfigurationEditor.render(form, null));
    }

    return ok(runConfigurationLister.render(testCase, null));
  }

	public static Result doDeleteRunConfiguration(Long id) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		RunConfiguration rc = RunConfiguration.findById(id);
		if (rc == null) {
			// it does not exist. error
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

	public static Result editRunConfigurationForm(Long id) {
		RunConfiguration rc = RunConfiguration.findById(id);
		if (rc == null) {
			// it does not exist. error
      return badRequest("RunConfiguration with id " + id + " does not exist.");
		}

		RunConfigurationEditorModel rome = new RunConfigurationEditorModel();
		rome.id = rc.id;
		rome.name = rc.name;
		rome.tdl = rc.tdl;
		rome.testCaseId = rc.testCase.id;
		rome.obsolete = rc.obsolete;
		rome.mappedWrappers = new ArrayList<>();

		for (MappedWrapper mappedWrapper : rc.mappedWrappers) {
      rome.mappedWrappers.add(new MappedWrapperModel(mappedWrapper.id, mappedWrapper.parameter.id, mappedWrapper.parameter.name, mappedWrapper.wrapper.name));
		}

		Form<?> fill = RUN_CONFIGURATION_FORM.fill(rome);

		return ok(runConfigurationEditor.render(fill, null));
	}

	public static Result doEditRunConfiguration() {
    Form<RunConfigurationEditorModel> form = RUN_CONFIGURATION_FORM.bindFromRequest();

    if (form.hasErrors()) {
      return badRequest(runConfigurationEditor.render(form, null));
		}

    RunConfigurationEditorModel model = form.get();

		RunConfiguration rc = RunConfiguration.findById(model.id);
    if (rc == null){
      return badRequest("The run configuration " + rc.id + " is not found.");
    }
    rc.name = model.name;
    rc.obsolete = false;
    rc.tdl = rc.testCase.tdl;

    try {
      Ebean.beginTransaction();
      List<MappedWrapper> mappedWrappers = new ArrayList<>();
      for (MappedWrapperModel mappedWrapper : model.mappedWrappers) {
        MappedWrapper mw = new MappedWrapper();
        mw.parameter = WrapperParam.findByTestCaseAndName(rc.testCase, mappedWrapper.name);
        mw.runConfiguration = rc;
        mw.wrapper = Wrapper.findByName(mappedWrapper.value);
        mw.save();
  }

      rc.mappedWrappers = mappedWrappers;
      rc.save();
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();

      form.reject(ex.getMessage());
      return badRequest(runConfigurationEditor.render(form, null));
    }
      run(id)
    return ok(runConfigurationLister.render(rc.testCase, null));
    }
  }
    if(id not finished){
      return ok(runningRIvetLister.render(id))
  public static Result displayRunConfiguration(Long id){
    RunConfiguration rc = RunConfiguration.findById(id);
    }
    if (rc == null){
      return badRequest("A run configuration with id [" + id + "] was not found");
    }

    return ok(runConfigurationDetailView.render(rc));
	}

  private static final HashMap<Long, List<String>> optionCache = new HashMap<>();

	public static List<String> listOptions(MappedWrapperModel mappedWrapperModel) {

    //the below query is not beautiful. so we are caching data to be faster.
    if (optionCache.containsKey(mappedWrapperModel.wrapperParamId)) {
      System.out.println("Get from cache");
      return optionCache.get(mappedWrapperModel.wrapperParamId);
    }

    System.out.println("Read from db for " + mappedWrapperModel.name);

		// get MappedParam
    //get the wrapperparam
    WrapperParam wp = WrapperParam.findById(mappedWrapperModel.wrapperParamId);
    //get signatures supported by this wp.
    List<ParamSignature> psList = ParamSignature.getByWrapperParam(wp);

    //create the return list.
    List<String> listOptions = new ArrayList<>();
    //List<SignalSlot> TdlCompiler.getSignatures(testCase.tdl, mappedWrapperModel.name);
    //we have to list the wrappers that cover all these signatures (might be more but we don't care)
    //not an optiomal solution for a huuuuge database. But there won't be more than 100 wrappers :-)
    List<Wrapper> all = Wrapper.getAll();
    System.out.println("ALL SIZE "+ all.size());
    out:
    for (Wrapper wrapper : all) {
      System.out.println("Wrapper " + wrapper.name);
      //check if all the signatures are covered by the signals or slots of this wrapper.
      for (ParamSignature ps : psList) {
        System.out.print("\t" + ps.signature);
        boolean included = false;
        for (TSignal signal : wrapper.signals) {
          if (ps.signature.equals(signal.signature))
            included = true;
          break;
        }

        if (!included) {
          for (TSlot slot : wrapper.slots) {
            if (ps.signature.equals(slot.signature))
              included = true;
            break;
          }
        }

        if (included) System.out.println(" included");
        else System.out.println("NOT included");
        if (!included)
          continue out;
      }
      //if we are here, then this wrapper contains all.
      //so add it to the list.
      listOptions.add(wrapper.name);
    }

    optionCache.put(mappedWrapperModel.wrapperParamId, listOptions);
    return listOptions;
	}
}
