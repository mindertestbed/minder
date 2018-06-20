package controllers;

import static play.data.Form.form;

import gov.tubitak.xoola.core.XoolaProperty;
import java.io.FileOutputStream;
import java.io.IOException;
import minderengine.XoolaServer;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class SettingsController extends Controller {

  public static class SettingsModel{
    public long timeout;
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result updateSettings() {
    Form<SettingsModel> form = form(SettingsModel.class).bindFromRequest();
    SettingsModel model = form.get();
    System.out.println(model.timeout);

    XoolaServer.properties.setProperty(XoolaProperty.NETWORK_RESPONSE_TIMEOUT, model.timeout +"");
    java.io.OutputStream out= null;
    try {
      out = new FileOutputStream("XoolaServer.properties");
      XoolaServer.properties.store(out, null);
    }
    catch(IOException io){
      io.printStackTrace();
    }
    return redirect(routes.Application.root("settings"));
  }
}
