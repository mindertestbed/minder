package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import editormodels.AssertionEditorModel;
import global.Util;
import models.ModelConstants;
import models.PrescriptionLevel;
import models.TestAssertion;
import models.TestGroup;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.assertionDetailView;
import views.html.testAssertionEditor;
import views.html.testAssertionLister;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class AssertionController extends Controller {
  public static final Form<AssertionEditorModel> TEST_ASSERTION_FORM = form(AssertionEditorModel.class);

  /*
   * Test Asertion CRUD
   */
  public static Result getCreateAssertionEditorView(Long groupId) {
    TestGroup tg = TestGroup.find.byId(groupId);
    if (tg == null) {
      return badRequest("Test group with id [" + groupId + "] not found!");
    } else {
      AssertionEditorModel testAssertionEditorModel = new AssertionEditorModel();
      testAssertionEditorModel.groupId = groupId;
      Form<AssertionEditorModel> bind = TEST_ASSERTION_FORM
          .fill(testAssertionEditorModel);
      return ok(testAssertionEditor.render(bind, null));
    }
  }

  public static Result doCreateAssertion() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<AssertionEditorModel> filledForm = TEST_ASSERTION_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testAssertionEditor.render(filledForm, null));
    } else {
      AssertionEditorModel model = filledForm.get();

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
      ta.shortDescription = model.shortDescription;
      ta.testGroup = tg;
      ta.prescriptionLevel = PrescriptionLevel
          .valueOf(model.prescriptionLevel);
      ta.owner=Application.getLocalUser(session());

      ta.save();

      ta = TestAssertion.findByTaId(ta.taId);

      Logger.info("Assertion with id " + ta.id + ":" + ta.taId
          + " was created");
      return redirect(controllers.routes.GroupController.getGroupDetailView(tg.id));
    }
  }

  public static Result editAssertionForm(Long id) {
    TestAssertion ta = TestAssertion.find.byId(id);
    if (ta == null) {
      return badRequest("Test assertion with id [" + id + "] not found!");
    } else {
      AssertionEditorModel taModel = new AssertionEditorModel();
      taModel.id = id;
      taModel.taId = ta.taId;
      taModel.normativeSource = ta.normativeSource;
      taModel.target = ta.target;
      taModel.predicate = ta.predicate;
      taModel.prerequisites = ta.prerequisites;
      taModel.variables = ta.variables;
      taModel.tag = ta.tag;
      taModel.description = ta.description;
      taModel.shortDescription = ta.shortDescription;
      taModel.groupId = ta.testGroup.id;
      taModel.prescriptionLevel = ta.prescriptionLevel.name();

      Form<AssertionEditorModel> bind = TEST_ASSERTION_FORM
          .fill(taModel);
      return ok(testAssertionEditor.render(bind, null));
    }
  }

  public static Result doEditAssertion() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<AssertionEditorModel> filledForm = TEST_ASSERTION_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testAssertionEditor.render(filledForm, null));
    } else {
      AssertionEditorModel model = filledForm.get();

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
      ta.shortDescription = model.shortDescription;
      ta.prescriptionLevel = PrescriptionLevel
          .valueOf(model.prescriptionLevel);

      // check if the name is not duplicate
      TestAssertion tmp = TestAssertion.findByTaId(model.taId);

      if (tmp == null || tmp.id == ta.id) {
        // either no such taId or it is already this object. so update
        ta.update();
        Logger.info("Assertion with id " + ta.id + ":" + ta.taId
            + " was updated");
        return redirect(controllers.routes.GroupController.getGroupDetailView(ta.testGroup.id));
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
    return redirect(controllers.routes.GroupController.getGroupDetailView(ta.testGroup.id));
  }

  public static Result getAssertionDetailView(Long id, String display){
    TestAssertion ta = TestAssertion.findById(id);
    if (ta == null){
      return badRequest("No test assertion with id " + id + ".");
    }
    return ok(assertionDetailView.render(ta, Application.getLocalUser(session()), display));
  }


  public static Result doEditAssertionField() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    JsonNode jsonNode = request().body().asJson();

    return GroupController.doEditField(AssertionEditorModel.class, TestAssertion.class, jsonNode);
  }


}
