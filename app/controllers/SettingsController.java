package controllers;

import minderengine.XoolaServer;
import org.interop.xoola.core.XoolaProperty;
import org.omg.CORBA.portable.OutputStream;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.AllowedRoles;
import security.Role;

import java.io.FileOutputStream;
import java.io.IOException;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class SettingsController extends Controller {


  public static class SettingsModel{
    public long timeout;
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public static Result updateSettings() {
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
