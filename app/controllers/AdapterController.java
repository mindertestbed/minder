package controllers;

import com.avaje.ebean.Ebean;
import editormodels.AdapterEditorModel;
import models.Adapter;
import models.AdapterVersion;
import utils.Util;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.adapters.adapterEditor;
import views.html.adapters.adapterLister;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class AdapterController extends Controller {
  Authentication authentication;
  public final Form<AdapterEditorModel> ADAPTER_FORM;

  @Inject
  public AdapterController(FormFactory formFactory, Authentication authentication) {
    this.authentication = authentication;
    ADAPTER_FORM = formFactory.form(AdapterEditorModel.class);
  }

  @AllowedRoles({Role.TEST_DEVELOPER})
  public Result doCreateAdapter() {
    final Form<AdapterEditorModel> filledForm = ADAPTER_FORM.bindFromRequest();
    final User localUser = authentication.getLocalUser();
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(adapterEditor.render(filledForm, false));
    } else {
      AdapterEditorModel model = filledForm.get();

      Adapter adapter = Adapter.findByName(model.name);
      if (adapter != null) {
        filledForm.reject("The adapter with name [" + adapter.name
            + "] already exists");
        return badRequest(adapterEditor.render(filledForm, false));
      }

      adapter = new Adapter();

      adapter.user = localUser;
      adapter.name = model.name;
      adapter.shortDescription = model.shortDescription;

      adapter.save();

      return ok(adapterLister.render());
    }
  }

  @AllowedRoles({Role.TEST_DEVELOPER})
  public Result createNewAdapterForm() {
    return ok(adapterEditor.render(ADAPTER_FORM, false));
  }


  @AllowedRoles({Role.TEST_DEVELOPER})
  public Result doDeleteAdapterVersion(Long id) {
    try {
      Ebean.beginTransaction();
      System.out.println("Adapter version id:" + id);
      AdapterVersion adapterVersion = AdapterVersion.findById(id);
      if (adapterVersion == null) {
        // it does not exist. error
        return badRequest("An adapter version with id " + id + " does not exist.");
      }

      adapterVersion.delete();

      Adapter adapter = Adapter.findById(adapterVersion.adapter.id);
      List<AdapterVersion> allByAdapter = AdapterVersion.getAllByAdapter(adapter);

      if (allByAdapter == null || allByAdapter.size() == 0) {
        Logger.info("No more adapter versions for the adapter " + adapter.name + ". Deleting the adapter.");
        adapter.delete();
      }

      Ebean.commitTransaction();
      return ok(adapterLister.render());
    } catch (Exception ex) {
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    } finally {
      Ebean.endTransaction();
    }
  }

  @AllowedRoles({Role.TEST_DEVELOPER})
  public Result doDeleteAdapter(Long id) {
    System.out.println("Adapter id:" + id);
    Adapter adapter = Adapter.findById(id);
    if (adapter == null) {
      // it does not exist. errort
      return badRequest("An adapter with id " + id + " does not exist.");
    }

    try {
      System.out.println("Adapter delete");
      adapter.delete();
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.error(ex.getMessage(), ex);
      return badRequest(ex.getMessage());
    }

    return ok(adapterLister.render());
  }

  @AllowedRoles({Role.TEST_DEVELOPER})
  public Result editAdapterForm(Long id) {
    Adapter adapter = Adapter.find.byId(id);
    if (adapter == null) {
      return badRequest("Adapter with id [" + id + "] not found!");
    } else {
      AdapterEditorModel adapterModel = new AdapterEditorModel();
      adapterModel.id = id;
      adapterModel.name = adapter.name;
      adapterModel.shortDescription = adapter.shortDescription;

      Form<AdapterEditorModel> bind = ADAPTER_FORM
          .fill(adapterModel);
      return ok(adapterEditor.render(bind, false));
    }
  }

  @AllowedRoles({Role.TEST_DEVELOPER})
  public Result doEditAdapter() {
    final Form<AdapterEditorModel> filledForm = ADAPTER_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest("");
    } else {
      AdapterEditorModel model = filledForm.get();
      Adapter adapter = Adapter.find.byId(model.id);
      adapter.shortDescription = model.shortDescription;
      adapter.name = model.name;
      adapter.update();

      Logger.info("Done updating adapter " + model.name);
      return ok(adapterLister.render());
    }
  }
}
