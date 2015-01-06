package controllers;

import models.TestAssertion;
import models.TestCase;
import models.TestGroup;
import models.User;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class InventoryController extends Controller {
	public static final Form<TestGroupEditorModel> TEST_GROUP_FORM = form(TestGroupEditorModel.class);
	public static final Form<TestAssertionEditorModel> TEST_ASSERTION_FORM = form(TestAssertionEditorModel.class);
	public static final Form<TestCaseEditorModel> TEST_CASE_FORM = form(TestCaseEditorModel.class);

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

		@Constraints.Required
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

	public static Result doCreateTestGroup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM
				.bindFromRequest();
		final User localUser = Application.getLocalUser(session());
		if (filledForm.hasErrors()) {
			Map<String, List<ValidationError>> errors = filledForm.errors();
			Set<String> set = errors.keySet();
			for (String key : set) {
				System.out.println(key + ":" + errors.get(key));
			}
			System.out.println("Hata var");
			return badRequest(testGroupEditor.render(localUser, filledForm));
		} else {
			TestGroupEditorModel model = filledForm.get();
			TestGroup tg = new TestGroup();
			tg.owner = localUser;
			tg.shortDescription = model.shortDescription;
			tg.description = model.description;
			tg.name = model.name;

			TestGroup gr2 = TestGroup.findByName(tg.name);
			if (gr2 == null) {
				tg.save();
				return ok(testGroupEditor.render(localUser, filledForm));
			} else {
				filledForm.reject("The group with name [" + tg.name
						+ "] already exists");
				return badRequest(testGroupEditor.render(localUser, filledForm));
			}
		}
	}

	public static Result createNewGroupForm() {
		final User localUser = Application.getLocalUser(session());
		return ok(testGroupEditor.render(localUser, TEST_GROUP_FORM));
	}

	public static Result editGroupForm(Long id) {
		final User localUser = Application.getLocalUser(session());
		TestGroup tg = TestGroup.find.byId(id);
		if (tg == null) {
			return badRequest("Test group with id [" + id + "] not found!");
		} else {

			Map<String, String> data = new HashMap<>();
			data.put("id", tg.id + "");
			data.put("name", tg.name);
			data.put("shortDescription", tg.shortDescription);
			data.put("description", tg.description);
			Form<TestGroupEditorModel> bind = TEST_GROUP_FORM.bind(data);

			return ok(testGroupEditor.render(localUser, bind));
		}
	}

	public static Result deleteGroup(Long id) {
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
			return ok();
		}
	}

	public static Result doEditGroup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());

		final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM
				.bindFromRequest();
		final User localUser = Application.getLocalUser(session());

		if (filledForm.hasErrors()) {
			Map<String, List<ValidationError>> errors = filledForm.errors();
			Set<String> set = errors.keySet();
			for (String key : set) {
				System.out.println(key + ":" + errors.get(key));
			}
			System.out.println("Hata var");
			return badRequest(testGroupEditor.render(localUser, filledForm));
		} else {
			TestGroupEditorModel model = filledForm.get();
			TestGroup tg = TestGroup.find.byId(model.id);
			tg.shortDescription = model.shortDescription;
			tg.description = model.description;
			tg.name = model.name;
			tg.update();
			return ok(testGroupEditor.render(localUser, filledForm));
		}
	}

	public static Result createAssertionForm(Long groupId) {
		return ok(testAssertionEditor.render(
				Application.getLocalUser(session()), TEST_ASSERTION_FORM,
				groupId));
	}

	public static Result doCreateAssertion() {
		return ok();
	}

	public static Result editAssertionForm(Long id) {
		return ok();
	}

	public static Result doEditAssertion() {
		return ok();
	}

	public static Result deleteAssertion(Long id) {
		return ok();
	}

	public static Result createCaseForm(Long assertionId) {
		final User localUser = Application.getLocalUser(session());
		return ok(testCaseEditor.render(localUser, TEST_CASE_FORM,assertionId));
	}

	public static Result doCreateCase() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<TestCaseEditorModel> filledForm = TEST_CASE_FORM
				.bindFromRequest();
		final User localUser = Application.getLocalUser(session());
		if (filledForm.hasErrors()) {
			Map<String, List<ValidationError>> errors = filledForm.errors();
			Set<String> set = errors.keySet();
			for (String key : set) {
				System.out.println(key + ":" + errors.get(key));
			}
			System.out.println("Hata var");
			return badRequest(testGroupEditor.render(localUser, filledForm));
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
				return ok(testCaseEditor.render(localUser, filledForm,tc.testAssertion.id));
			} else {
				filledForm.reject("The group with name [" + tc.name
						+ "] already exists");
				return badRequest(testGroupEditor.render(localUser, filledForm));
			}
		}
	}

	public static Result editCaseForm(Long id) {
		return ok();
	}

	public static Result doEditCase() {
		return ok();
	}

	public static Result deleteCase(Long id) {
		return ok();
	}
}
