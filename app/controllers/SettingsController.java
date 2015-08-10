package controllers;

import minderengine.XoolaServer;
import org.interop.xoola.core.XoolaProperty;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

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
