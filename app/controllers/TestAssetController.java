package controllers;

import global.Util;
import models.ModelConstants;
import models.TestAsset;
import models.TestGroup;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.group.testAssetEditor;
import views.html.group.childViews.testAssetList;

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
    @Constraints.MinLength(ModelConstants.MIN_DESC_LENGTH)
    @Constraints.MaxLength(ModelConstants.SHORT_DESC_LENGTH)
    public String shortDescription;

    public String description;
  }

  @Security.Authenticated(Secured.class)
  public static Result doCreateTestAsset(Long testGroupId) {
    final Form<TestAssetModel> filledForm = TEST_ASSET_FORM.bindFromRequest();

    TestGroup group = TestGroup.findById(testGroupId);

    if (group == null) {
      return badRequest("Test Group with [" + testGroupId + "] not found");
    }

    User localUser = Authentication.getLocalUser();
    if (localUser.email.equals("root@minder") || group.owner.email.equals(localUser.email)) {

      if (filledForm.hasErrors()) {
        Util.printFormErrors(filledForm);
        return badRequest(testAssetEditor.render(filledForm, null));
      } else {
        TestAssetModel model = filledForm.get();

        TestAsset asset = TestAsset.findByGroup(group, model.name);
        if (asset != null) {
          filledForm.reject("An asset with name [" + asset.name + "] already exists");
          return badRequest(testAssetEditor.render(filledForm, null));
        }

        //file upload part
        try {
          handleFileUpload(group.id, model.name);
        } catch (Exception ex) {
          Logger.error(ex.getMessage(), ex);
          filledForm.reject("Unknown error");
          return badRequest(testAssetEditor.render(filledForm, null));
        }

        asset = new TestAsset();
        asset.testGroup = group;
        asset.shortDescription = model.shortDescription;
        asset.description = model.description;
        asset.name = model.name;

        asset.save();

        return ok(testAssetList.render(group));
      }
    } else {
      return badRequest("You can't use this resource");
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result createNewAssetForm() {
    return ok(testAssetEditor.render(TEST_ASSET_FORM, null));
  }

  @Security.Authenticated(Secured.class)
  public static Result editAssetForm(Long id) {
    User localUser = Authentication.getLocalUser();

    TestAsset asset = TestAsset.findById(id);
    TestGroup group = asset.testGroup;
    if (localUser == null || (!localUser.email.equals("root@minder") && !group.owner.email.equals(localUser.email))) {
      return badRequest("You can't use this resource");
    }
    TestAsset ta = TestAsset.findById(id);
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

  @Security.Authenticated(Secured.class)
  public static Result doEditAsset() {
    final Form<TestAssetModel> filledForm = TEST_ASSET_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testAssetEditor.render(filledForm, null));
    } else {
      TestAssetModel model = filledForm.get();
      TestAsset ta = TestAsset.findById(model.id);
      User localUser = Authentication.getLocalUser();
      if (localUser == null || (!localUser.email.equals("root@minder") && !ta.testGroup.owner.email.equals(localUser.email))) {
        return badRequest("You cant use this resource");
      }
      try {
        handleFileUpload(ta.testGroup.id, model.name);
      } catch (Exception ex) {
        filledForm.reject(ex.getMessage());
        return badRequest(testAssetEditor.render(filledForm, null));
      }

      ta.name = model.name;
      ta.shortDescription = model.shortDescription;
      ta.description = model.description;
      ta.update();

      Logger.info("Done updating test asset " + ta.name + ":" + ta.id);

      return redirect(controllers.routes.GroupController.getGroupDetailView(ta.testGroup.id, "assets"));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result doDeleteAsset(Long id) {
    TestAsset ta = TestAsset.findById(id);
    if (ta == null) {
      return badRequest("Test asset with id [" + id + "] not found!");
    } else {
      try {
        ta.delete();
      } catch (Exception ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }
      User user = Authentication.getLocalUser();

      new File("assets/_" + ta.testGroup.id + "/" + ta.name).delete();

      return redirect(controllers.routes.GroupController.getGroupDetailView(ta.testGroup.id, "assets"));
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result downloadAsset(Long id) {
    TestAsset ta = TestAsset.findById(id);
    if (ta == null) {
      return badRequest("Test asset with id [" + id + "] not found!");
    } else {
      response().setContentType("application/x-download");
      response().setHeader("Content-disposition", "attachment; filename=" + ta.name);
      return ok(new File("assets/_" + ta.testGroup.id + "/" + ta.name));
    }
  }


  /**
   * Opens the asset identified by the user <code>email</code> and asset name <code>name</code>
   * as an input stream. Its the callers' responsibility to close the stream.
   *
   * @param tgId
   * @param name
   * @return
   */
  public static InputStream getAssetAsStream(long tgId, String name) {
    try {
      FileInputStream fis = new FileInputStream("assets/_" + tgId + "/" + name);
      return fis;
    } catch (Exception ex) {
      throw new RuntimeException("an asset file [" + name + "] was not found for test group with id [" + tgId + "]");
    }
  }

  /**
   * Reads the asset identified by the user <code>email</code> and asset name <code>name</code>
   * and returns it in a byte array.
   *
   * @param groupId
   * @param name
   * @return
   */
  public static byte[] getAssetAsByteArray(long groupId, String name) {
    int read = -1;
    byte[] bulk = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    InputStream is = getAssetAsStream(groupId, name);
    try {
      while ((read = is.read(bulk)) != -1) {
        baos.write(bulk, 0, read);
      }
      is.close();
      return baos.toByteArray();
    } catch (Exception ex) {
      Logger.error(ex.getMessage(), ex);
      throw new RuntimeException("Couldn't read asset [_" + groupId + "/" + name + "]");
    }
  }

  /**
   * Reads the file uploaded with the current request (if any)
   *
   * @param groupId
   * @param name
   */
  private static void handleFileUpload(long groupId, String name) {
    Http.MultipartFormData body = request().body().asMultipartFormData();

    if (body == null)
      throw new RuntimeException("The form type is not correct");

    File asset = null;

    if (body.getFiles() != null && body.getFiles().size() > 0)
      asset = body.getFiles().get(0).getFile();

    new File("assets/_" + groupId).mkdirs();
    File fl = new File("assets/_" + groupId + "/" + name);
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
        throw new RuntimeException("Asset for [_" + groupId + "/" + name + "] couldn't be created");
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
          fos.write(bulk, 0, r);
        }
        fis.close();
        fos.close();
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }
  }
}
