package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import editormodels.JobEditorModel;
import global.Util;
import minderengine.XoolaServer;
import models.*;
import org.interop.xoola.core.XoolaProperty;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.jobDetailView;
import views.html.jobEditor;
import views.html.jobLister;
import views.html.testRunViewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class SettingsController extends Controller {


  public static class SettingsModel{
    public long timeout;
  }

  public static Result updateSettings() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    Form<SettingsModel> form = form(SettingsModel.class).bindFromRequest();
    SettingsModel model = form.get();
    System.out.println(model.timeout);

    XoolaServer.properties.setProperty(XoolaProperty.NETWORK_RESPONSE_TIMEOUT, model.timeout +"");
    return redirect(routes.Application.root("settings"));
  }
}
