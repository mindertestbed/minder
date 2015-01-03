package controllers;

import models.TestGroup;
import models.User;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.createNewTestGroupForm;
import views.html.index;
import views.html.restrictedTestDesigner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class InventoryController extends Controller {
  public static final Form<TestGroupEditor> CREATE_NEW_TEST_GROUP = form(TestGroupEditor.class);
  public static final Form<TestGroupEditor> EDIT_TEST_GROUP = form(TestGroupEditor.class);


  public static class TestGroupEditor {
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.MinLength(10)
    @Constraints.MaxLength(50)
    public String shortDescription;

    public String description;
  }

  public static Result doCreateTestGroup() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestGroupEditor> filledForm = CREATE_NEW_TEST_GROUP.bindFromRequest();
    final User localUser = Application.getLocalUser(session());
    if (filledForm.hasErrors()) {
      Map<String, List<ValidationError>> errors = filledForm.errors();
      Set<String> set = errors.keySet();
      for (String key : set) {
        System.out.println(key + ":" + errors.get(key));
      }
      System.out.println("Hata var");
      return badRequest(createNewTestGroupForm.render(localUser, filledForm));
    } else {
      TestGroupEditor testGroupEditor = filledForm.get();
      TestGroup tg = new TestGroup();
      tg.owner = localUser;
      tg.shortDescription = testGroupEditor.shortDescription;
      tg.description = testGroupEditor.description;
      tg.name = testGroupEditor.name;

      TestGroup gr2 = TestGroup.findByName(tg.name);
      if (gr2 == null) {
        tg.save();
        return ok(createNewTestGroupForm.render(localUser, filledForm));
      } else {
        filledForm.reject("The group with name [" + tg.name + "] already exists");
        return badRequest(createNewTestGroupForm.render(localUser, filledForm));
      }
    }
  }

  public static Result createNewGroupForm() {
    final User localUser = Application.getLocalUser(session());
    return ok(createNewTestGroupForm.render(localUser, CREATE_NEW_TEST_GROUP));
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
      Form<TestGroupEditor> bind = CREATE_NEW_TEST_GROUP.bind(data);

      return ok(createNewTestGroupForm.render(localUser, bind));
    }
  }

  public static Result deleteGroup(Long id) {
    final User localUser = Application.getLocalUser(session());
    TestGroup tg = TestGroup.find.byId(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {
      try {
        tg.delete();
      }catch (Exception ex){
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }
      return ok();
    }
  }


  public static Result doEditGroup() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    final Form<TestGroupEditor> filledForm = CREATE_NEW_TEST_GROUP.bindFromRequest();
    final User localUser = Application.getLocalUser(session());


    if (filledForm.hasErrors()) {
      Map<String, List<ValidationError>> errors = filledForm.errors();
      Set<String> set = errors.keySet();
      for (String key : set) {
        System.out.println(key + ":" + errors.get(key));
      }
      System.out.println("Hata var");
      return badRequest(createNewTestGroupForm.render(localUser, filledForm));
    } else {
      TestGroupEditor testGroupEditor = filledForm.get();
      TestGroup tg = TestGroup.find.byId(testGroupEditor.id);
      tg.shortDescription = testGroupEditor.shortDescription;
      tg.description = testGroupEditor.description;
      tg.name = testGroupEditor.name;
      tg.update();
      return ok(createNewTestGroupForm.render(localUser, filledForm));
    }
  }
}
