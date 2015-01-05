package controllers;

import models.TestAssertion;
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

    public Long groupId;

    @Constraints.Required
    public String taId = "TAID";

    @Constraints.Required
    public String normativeSource = "NS";

    @Constraints.Required
    public String target = "TARGET";

    public String prerequisites;

    @Constraints.Required
    public String predicate = "PREDICATE";

    public String variables;
  }


  public class TestCaseEditorModel {
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
    final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM.bindFromRequest();
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

      TestGroup group = TestGroup.findByName(model.name);
      if (group != null) {
        filledForm.reject("The group with name [" + group.name + "] already exists");
        return badRequest(testGroupEditor.render(localUser, filledForm));
      }

      group = new TestGroup();

      group.owner = localUser;
      group.shortDescription = model.shortDescription;
      group.description = model.description;
      group.name = model.name;

      group.save();

      return ok(testGroupLister.render(localUser));
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

  public static Result doDeleteGroup(Long id) {
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


  public static Result doEditGroup() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    final Form<TestGroupEditorModel> filledForm = TEST_GROUP_FORM.bindFromRequest();
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
      return ok(testGroupLister.render(localUser));
    }
  }


  public static Result createAssertionForm(Long groupId) {
    System.out.println("GROUP ID " + groupId);
    return ok(testAssertionEditor.render(Application.getLocalUser(session()), TEST_ASSERTION_FORM, groupId));
  }

  public static Result doCreateAssertion() {

    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestAssertionEditorModel> filledForm = TEST_ASSERTION_FORM.bindFromRequest();
    final User localUser = Application.getLocalUser(session());

    if (filledForm.hasErrors()) {
      Map<String, List<ValidationError>> errors = filledForm.errors();
      Set<String> set = errors.keySet();
      for (String key : set) {
        System.out.println(key + ":" + errors.get(key));
      }
      System.out.println("Hata var");
      return badRequest(testAssertionEditor.render(localUser, filledForm, 1L));
    } else {

      TestAssertionEditorModel model = filledForm.get();

      TestAssertion ta = TestAssertion.findByTaId(model.taId);
      if (ta != null) {
        filledForm.reject("The test assertion with ID [" + ta.taId + "] already exists");
        return badRequest(testAssertionEditor.render(localUser, filledForm, model.groupId));
      }

      TestGroup tg = TestGroup.findById(model.groupId);

      if (tg == null) {
        filledForm.reject("No group found with id [" + tg.id + "]");
        return badRequest(testAssertionEditor.render(localUser, filledForm, model.groupId));
      }

      ta = new TestAssertion();
      ta.taId = model.taId;
      ta.normativeSource = model.normativeSource;
      ta.predicate = model.predicate;
      ta.prerequisites = model.prerequisites;
      ta.target = model.target;
      ta.testGroup = tg;

      ta.save();
      return ok(testAssertionEditor.render(localUser, filledForm, model.groupId));
    }

  }

  public static Result editAssertionForm(Long id) {
    return ok();
  }

  public static Result doEditAssertion() {
    return ok();
  }

  public static Result doDeleteAssertion(Long id) {
    return ok();
  }

  public static Result createCaseForm(Long assertionId) {
    return ok();
  }

  public static Result doCreateCase() {
    return ok();
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
}
