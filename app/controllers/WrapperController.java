package controllers;

import editormodels.WrapperEditorModel;
import global.Util;
import models.User;
import models.Wrapper;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.testDeveloper.wrapper.wrapperEditor;
import views.html.testDeveloper.wrapper.wrapperEditor2;
import views.html.testDeveloper.wrapper.wrapperLister;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class WrapperController extends Controller {
  public static final Form<WrapperEditorModel> WRAPPER_FORM = form(WrapperEditorModel.class);

  @Security.Authenticated(Secured.class)
  public static Result doCreateWrapper() {
    final Form<WrapperEditorModel> filledForm = WRAPPER_FORM.bindFromRequest();
    final User localUser = Authentication.getLocalUser();
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(wrapperEditor.render(filledForm));
    } else {
      WrapperEditorModel model = filledForm.get();

      Wrapper wrapper = Wrapper.findByName(model.name);
      if (wrapper != null) {
        filledForm.reject("The wrapper with name [" + wrapper.name
            + "] already exists");
        return badRequest(wrapperEditor.render(filledForm));
      }

      wrapper = new Wrapper();

      wrapper.user = localUser;
      wrapper.name = model.name;
      wrapper.shortDescription = model.shortDescription;

      wrapper.save();

      return ok(wrapperLister.render());
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result createNewWrapperForm() {
    return ok(wrapperEditor.render(WRAPPER_FORM));
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteWrapper(Long id) {
    System.out.println("Wrapper id:" + id);
    Wrapper wr = Wrapper.findById(id);
    if (wr == null) {
      // it does not exist. errort
      return badRequest("Wrapper with id " + id + " does not exist.");
    }

    try {
      System.out.println("Wrapper delete");
      wr.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    return ok(wrapperLister.render());
  }

  @Security.Authenticated(Secured.class)
  public static Result editWrapperForm(Long id) {
    Wrapper wr = Wrapper.find.byId(id);
    if (wr == null) {
      return badRequest("Wrapper with id [" + id + "] not found!");
    } else {
      WrapperEditorModel wrModel = new WrapperEditorModel();
      wrModel.id = id;
      wrModel.name = wr.name;
      wrModel.shortDescription = wr.shortDescription;

      Form<WrapperEditorModel> bind = WRAPPER_FORM
          .fill(wrModel);
      return ok(wrapperEditor2.render(bind));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doEditWrapper() {
    final Form<WrapperEditorModel> filledForm = WRAPPER_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest("");//wrapperEditor2.render(filledForm, null));
    } else {
      WrapperEditorModel model = filledForm.get();
      Wrapper wr = Wrapper.find.byId(model.id);
      wr.shortDescription = model.shortDescription;
      wr.update();

      Logger.info("Done updating wrapper " + model.name);
      return ok(wrapperLister.render());
    }
  }
}
