package controllers;

import play.Configuration;
import play.Environment;
import utils.Util;
import models.ModelConstants;
import models.TestAsset;
import models.TestGroup;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;
import views.html.group.testAssetEditor;
import views.html.group.childViews.testAssetList;

import javax.inject.Inject;
import java.io.*;
import java.util.List;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class TestAssetController extends Controller {

  private final String ASSETS_DIR;
  Authentication authentication;

  public final Form<TestAssetModel> TEST_ASSET_FORM;

  @Inject
  public TestAssetController(FormFactory formFactory, Authentication authentication, Configuration configuration) {
    this.authentication = authentication;
    TEST_ASSET_FORM = formFactory.form(TestAssetModel.class);
    ASSETS_DIR = configuration.getString("minder.assets.dir", "./data/assets");
  }

  public static class TestAssetModel {

    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.MinLength(ModelConstants.MIN_DESC_LENGTH)
    @Constraints.MaxLength(ModelConstants.SHORT_DESC_LENGTH)
    public String shortDescription;

    public String description;

    public TestAssetModel() {

    }
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doCreateTestAsset(Long testGroupId) {
    final Form<TestAssetModel> filledForm = TEST_ASSET_FORM.bindFromRequest();

    TestGroup group = TestGroup.findById(testGroupId);

    if (group == null) {
      return badRequest("Test Group with [" + testGroupId + "] not found");
    }

    User localUser = authentication.getLocalUser();
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

        return ok(testAssetList.render(group, authentication));
      }
    } else {
      return badRequest("You can't use this resource");
    }
  }


  @AllowedRoles({Role.TEST_DESIGNER})
  public Result createNewAssetForm() {
    return ok(testAssetEditor.render(TEST_ASSET_FORM, null));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result editAssetForm(Long id) {
    User localUser = authentication.getLocalUser();

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


  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doEditAsset() {
    final Form<TestAssetModel> filledForm = TEST_ASSET_FORM.bindFromRequest();

    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testAssetEditor.render(filledForm, null));
    } else {
      TestAssetModel model = filledForm.get();
      TestAsset ta = TestAsset.findById(model.id);
      User localUser = authentication.getLocalUser();
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

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result doDeleteAsset(Long id) {
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
      User user = authentication.getLocalUser();

      new File(ASSETS_DIR + "/_" + ta.testGroup.id + "/" + ta.name).delete();

      return redirect(controllers.routes.GroupController.getGroupDetailView(ta.testGroup.id, "assets"));
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER, Role.TEST_DEVELOPER})
  public Result downloadAsset(Long id) {
    TestAsset ta = TestAsset.findById(id);
    if (ta == null) {
      return badRequest("Test asset with id [" + id + "] not found!");
    } else {
      response().setContentType("application/x-download");
      response().setHeader("Content-disposition", "attachment; filename=" + ta.name);
      return ok(new File(ASSETS_DIR + "/_" + ta.testGroup.id + "/" + ta.name));
    }
  }


  /**
   * Opens the asset identified by the user <code>email</code> and asset name <code>name</code>
   * as an input stream. Its the callers' responsibility to close the stream.
   */
  public InputStream getAssetAsStream(long tgId, String name) {
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
   */
  public byte[] getAssetAsByteArray(long groupId, String name) {
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
   */
  private void handleFileUpload(long groupId, String name) {
    Http.MultipartFormData body = request().body().asMultipartFormData();

    if (body == null) {
      throw new RuntimeException("The form type is not correct");
    }

    File asset = null;

    List<Http.MultipartFormData.FilePart> files = body.getFiles();
    if (files != null && files.size() > 0) {
      asset = (File) files.get(0).getFile();
    }

    new File(ASSETS_DIR + "/_" + groupId).mkdirs();
    File fl = new File(ASSETS_DIR + "/_" + groupId + "/" + name);
    if (asset == null) {
      //no asset defined check if a file already exists? (edit mode)
      if (fl.exists()) {
        return; //its ok.
      }
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
