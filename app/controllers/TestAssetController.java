package controllers;

import models.TestAsset;
import models.TestGroup;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;

import java.io.*;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class TestAssetController extends Controller {
  public static final Form<TestAssetModel> TEST_ASSET_FORM = form(TestAssetModel.class);


  public static class TestAssetModel {
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.MinLength(10)
    @Constraints.MaxLength(50)
    public String shortDescription;

    public String description;
  }

  public static Result doCreateTestAsset() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final Form<TestAssetModel> filledForm = TEST_ASSET_FORM.bindFromRequest();

    User localuser = Application.getLocalUser(session());

    if (filledForm.hasErrors()) {
      InventoryController.printFormErrors(filledForm);
      return badRequest(testAssetEditor.render(filledForm, null));
    } else {
      TestAssetModel model = filledForm.get();

      TestAsset asset = TestAsset.findByUserAndName(localuser, model.name);
      if (asset != null) {
        filledForm.reject("An asset with name [" + asset.name + "] already exists");
        return badRequest(testAssetEditor.render(filledForm, null));
      }

      //file upload part
      try {
        handleFileUpload(localuser.email, model.name);
      } catch (Exception ex) {
        Logger.error(ex.getMessage(), ex);
        filledForm.reject("Unknown error");
        return badRequest(testAssetEditor.render(filledForm, null));
      }

      asset = new TestAsset();
      asset.owner = localuser;
      asset.shortDescription = model.shortDescription;
      asset.description = model.description;
      asset.name = model.name;

      asset.save();

      return ok(testAssetLister.render(localuser));
    }
  }

  public static Result createNewAssetForm() {
    return ok(testAssetEditor.render(TEST_ASSET_FORM, null));
  }


  public static Result editAssetForm(Long id) {
    final User localUser = Application.getLocalUser(session());
    TestAsset ta = TestAsset.find.byId(id);
    if (ta == null) {
      return badRequest("Test asset with id [" + id + "] not found!");
    } else {
      TestAssetModel tgem = new TestAssetModel();
      tgem.id = ta.id;
      tgem.name = ta.name;
      tgem.shortDescription = ta.shortDescription;
      tgem.description = ta.description;

      Form<TestAssetModel> bind = TEST_ASSET_FORM.fill(tgem);

      return ok(testAssetEditor.render(bind, null));
    }
  }

  public static Result doEditAsset() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    final Form<TestAssetModel> filledForm = TEST_ASSET_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      InventoryController.printFormErrors(filledForm);
      return badRequest(testAssetEditor.render(filledForm, null));
    } else {
      TestAssetModel model = filledForm.get();

      //file upload part
      User localUser = Application.getLocalUser(session());
      try {
        handleFileUpload(localUser.email, model.name);
      } catch (Exception ex) {
        filledForm.reject(ex.getMessage());
        return badRequest(testAssetEditor.render(filledForm, null));
      }

      TestAsset ta = TestAsset.find.byId(model.id);
      ta.name = model.name;
      ta.shortDescription = model.shortDescription;
      ta.description = model.description;
      ta.update();

      Logger.info("Done updating test asset " + ta.name + ":" + ta.id);
      return ok(testAssetLister.render(localUser));
    }
  }

  public static Result doDeleteAsset(Long id) {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    TestAsset ta = TestAsset.find.byId(id);
    if (ta == null) {
      return badRequest("Test asset with id [" + id + "] not found!");
    } else {
      try {
        ta.delete();
      } catch (Exception ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }
      User user = Application.getLocalUser(session());

      new File("assets/" + formatMail(user.email) + "/" + ta.name).delete();
      return ok(testAssetLister.render(user));
    }
  }

  public static Result downloadAsset(Long id){
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    TestAsset ta = TestAsset.find.byId(id);
    if (ta == null) {
      return badRequest("Test asset with id [" + id + "] not found!");
    } else {
      User user = Application.getLocalUser(session());
      response().setContentType("application/x-download");
      response().setHeader("Content-disposition","attachment; filename=" + ta.name);
      return ok(new File("assets/" + formatMail(user.email) + "/" + ta.name));
    }
  }


  /**
   * Opens the asset identified by the user <code>email</code> and asset name <code>name</code>
   * as an input stream. Its the callers' responsibility to close the stream.
   *
   * @param email
   * @param name
   * @return
   */
  public static InputStream getAssetAsStream(String email, String name) {
    try {
      FileInputStream fis = new FileInputStream("assets/" + formatMail(email) + "/" + name);
      return fis;
    } catch (Exception ex) {
      throw new RuntimeException("an asset file [" + name + "] was not found for user [" + email + "]");
    }
  }

  /**
   * Reads the asset identified by the user <code>email</code> and asset name <code>name</code>
   * and returns it in a byte array.
   *
   * @param email
   * @param name
   * @return
   */
  public static byte[] getAssetAsByteArray(String email, String name) {
    int read = -1;
    byte[] bulk = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    InputStream is = getAssetAsStream(email, name);
    try {
      while ((read = is.read(bulk)) != -1) {
        baos.write(bulk, 0, read);
      }
      is.close();
      return baos.toByteArray();
    } catch (Exception ex) {
      Logger.error(ex.getMessage(), ex);
      throw new RuntimeException("Couldn't read asset [" + email + "/" + name + "]");
    }
  }

  /**
   * Reads the file uploaded with the current request (if any)
   *
   * @param email
   * @param name
   */
  private static void handleFileUpload(String email, String name) {
    Http.MultipartFormData body = request().body().asMultipartFormData();

    if (body == null)
      throw new RuntimeException("The form type is not correct");

    File asset = null;

    if (body.getFiles() != null && body.getFiles().size() > 0)
      asset = body.getFiles().get(0).getFile();

    String formattedMail = formatMail(email);
    new File("assets/" + formattedMail).mkdirs();
    File fl = new File("assets/" + formattedMail + "/" + name);
    if (asset == null) {
      //no asset defined check if a file already exists? (edit mode)
      if (fl.exists())
        return; //its ok.
      throw new RuntimeException("No asset file was specified!");
    } else {
      FileOutputStream fos;
      FileInputStream fis;
      try {
        fos = new FileOutputStream(fl);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("Asset for [" + email + "/" + name + "] couldn't be created");
      }
      try {
        fis = new FileInputStream(asset);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("File " + asset.getPath() + " was not found");
      }
      try {
        int r = -1;
        byte[] bulk = new byte[1024];

        while ((r = fis.read(bulk)) != -1) {
          Thread.sleep(5);
          System.out.println(r);
          fos.write(bulk, 0, r);
        }
        fis.close();
        fos.close();
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }
  }

  private static String formatMail(String email) {
    return email.replaceAll("(@|\\.|\\-)", "_");
  }
}
