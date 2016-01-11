package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.enumeration.Utils;
import editormodels.UtilClassEditorModel;
import global.Util;
import models.TestGroup;
import models.User;
import models.UtilClass;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.utilClass.*;

import static play.data.Form.form;


/**
 * Created by yerlibilgin on 03/05/15.
 */
public class UtilClassController extends Controller {
  public static final Form<UtilClassEditorModel> UTIL_CLASS_FORM = form(UtilClassEditorModel.class);

  @Security.Authenticated(Secured.class)
  public static Result getCreateUtilClassEditorView(Long groupId) {
    TestGroup testGroup = TestGroup.findById(groupId);
    if (testGroup == null) {
      return badRequest("Test group with id [" + groupId
          + "] not found!");
    } else {
      UtilClassEditorModel utilClassEditorModel = new UtilClassEditorModel();
      utilClassEditorModel.groupId = groupId;

      Form<UtilClassEditorModel> bind = UTIL_CLASS_FORM
          .fill(utilClassEditorModel);
      return ok(utilClassEditor.render(bind, false));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doCreateUtilClass() {
    final Form<UtilClassEditorModel> filledForm = UTIL_CLASS_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(utilClassEditor.render(filledForm, false));
    } else {

      UtilClassEditorModel model = filledForm.get();
      Logger.debug("Creating util class " + model.name);

      UtilClass utilClass = UtilClass.findByGroupIdAndName(model.groupId, model.name);
      if (utilClass != null) {
        Logger.error("A util class with the given name already exists");

        filledForm.reject("A util class with name [" + utilClass.name
            + "] already exists");
        return badRequest(utilClassEditor.render(filledForm, false));
      }

      TestGroup tg = TestGroup.findById(model.groupId);
      if (tg == null) {
        filledForm.reject("No test group found with id [" + tg.id + "]");
        return badRequest(utilClassEditor.render(filledForm, false));
      }

      final User localUser = Authentication.getLocalUser();

      utilClass = new UtilClass();
      utilClass.name = model.name;
      utilClass.source = model.tdl;
      utilClass.shortDescription = model.shortDescription;
      utilClass.testGroup = tg;
      utilClass.owner = localUser;
      try {
        utilClass.save();
      } catch (Exception ex) {
        filledForm.reject("Compilation Failed [" + ex.getMessage() + "]");
        Logger.error(ex.getMessage(), ex);
        return badRequest(utilClassEditor.render(filledForm, false));
      }

      utilClass = UtilClass.findByGroupIdAndName(model.groupId, utilClass.name);

      Logger.info("Util class with name " + utilClass.id + ":" + utilClass.name
          + " was created");
      return redirect(routes.GroupController.getGroupDetailView(tg.id, "utils"));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result getEditCaseEditorView(Long id) {
    UtilClass utilClass = UtilClass.findById(id);
    if (utilClass == null) {
      return badRequest("A utility class with id [" + id + "] was not found!");
    } else {
      UtilClassEditorModel ucModel = new UtilClassEditorModel();
      ucModel.id = id;
      ucModel.groupId = utilClass.testGroup.id;
      ucModel.name = utilClass.name;
      ucModel.shortDescription = utilClass.shortDescription;
      ucModel.tdl = utilClass.source;

      Form<UtilClassEditorModel> bind = UTIL_CLASS_FORM.fill(ucModel);
      return ok(utilClassEditor.render(bind, true));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditUtilClass() {
    final Form<UtilClassEditorModel> filledForm = UTIL_CLASS_FORM
        .bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(utilClassEditor.render(filledForm, true));
    } else {
      UtilClassEditorModel model = filledForm.get();

      UtilClass uc = UtilClass.findById(model.id);
      if (uc == null) {
        filledForm.reject("A util class with ID [" + model.id
            + "] does not exist");
        return badRequest(utilClassEditor.render(filledForm, true));
      }

      uc.name = model.name;
      uc.shortDescription = model.shortDescription;
      uc.source = model.tdl;

      // check if the name is not duplicate
      UtilClass tmp = UtilClass.findByGroupIdAndName(model.groupId, model.name);

      if (tmp == null || tmp.id == uc.id) {
        // either no such name or it is already this object. so update
        try {
          uc.update();
        }catch(Exception ex){
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(utilClassEditor.render(filledForm, true));
        }
        Logger.info("Test Util Class " + uc.id + ":" + uc.name
            + " was updated");
        return redirect(routes.UtilClassController.viewUtilClass(uc.id));
      } else {
        filledForm.reject("The Name [" + model.name
            + "] is used by another test case");
        return badRequest(utilClassEditor.render(filledForm, true));
      }

    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteUtilClass(Long id) {
    UtilClass uc = UtilClass.findById(id);
    if (uc == null) {
      // it does not exist. error
      return badRequest("A Util Class with id " + id + " does not exist.");
    }

    try {
      uc.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }
    return redirect(routes.GroupController.getGroupDetailView(uc.testGroup.id, "utils"));
  }

  @Security.Authenticated(Secured.class)
  public static Result viewUtilClass(long id) {
    UtilClass uc = UtilClass.findById(id);
    if (uc == null) {
      return badRequest("No util class id " + id + ".");
    }
    return ok(utilClassView.render(uc));
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditUtilClassField() {
    JsonNode jsonNode = request().body().asJson();

    Result res = Utils.doEditField(UtilClassEditorModel.class, UtilClass.class, jsonNode,Authentication.getLocalUser());

    if (res.toScala().header().status() == BAD_REQUEST) {
      return res;
    } else {
      long id = jsonNode.findPath("id").asInt();
      UtilClass uc = UtilClass.findById(id);
      //just trigger recompile and stuff
      try {
        uc.save();
        return res;
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        return badRequest("Compilation Failed [" + ex.getMessage() + "]");
      }
    }
  }
}
