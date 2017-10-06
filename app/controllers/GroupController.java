package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.server.lib.util.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;
import controllers.common.Utils;
import dependencyutils.DependencyClassLoaderCache;
import editormodels.GroupEditorModel;
import utils.Util;
import models.TestGroup;
import models.User;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.eclipse.aether.resolution.DependencyResolutionException;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import rest.controllers.TestGroupImportExportController;
import rest.controllers.common.RestUtils;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.models.RestTestGroup;
import security.AllowedRoles;
import security.Role;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

import views.html.group.childViews.*;
import views.html.group.*;

import javax.inject.Inject;

import static play.data.Form.form;

/**
 * Created by yerlibilgin on 03/05/15.
 */
public class GroupController extends Controller {
  Authentication authentication;
  TestGroupImportExportController testGroupImportExportController;

  public final Form<GroupEditorModel> TEST_GROUP_FORM;
  public final Form<GroupImportModel> TEST_GROUP_IMPORT_FORM;

  @Inject
  public GroupController(FormFactory formFactory, Authentication authentication, TestGroupImportExportController testGroupImportExportController) {
    this.authentication = authentication;
    this.testGroupImportExportController = testGroupImportExportController;

    TEST_GROUP_FORM = formFactory.form(GroupEditorModel.class);
    TEST_GROUP_IMPORT_FORM = formFactory.form(GroupImportModel.class);
  }

  public static class GroupImportModel {
    public String useremail;
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result getCreateGroupEditorView() {
    return ok(testGroupEditor.render(TEST_GROUP_FORM, authentication));
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doCreateTestGroup() {
    final Form<GroupEditorModel> filledForm = TEST_GROUP_FORM
        .bindFromRequest();
    final User localUser = authentication.getLocalUser();
    if (filledForm.hasErrors()) {
      Util.printFormErrors(filledForm);
      return badRequest(testGroupEditor.render(filledForm, authentication));
    } else {
      GroupEditorModel model = filledForm.get();

      TestGroup group = TestGroup.findByName(model.name);
      if (group != null) {
        filledForm.reject("The group with name [" + group.name + "] already exists");
        return badRequest(testGroupEditor.render(filledForm, authentication));
      }

      group = new TestGroup();
      group.owner = localUser;
      group.shortDescription = model.shortDescription;
      group.description = model.description;
      group.name = model.name;
      group.dependencyString = "";

      group.save();
      return redirect(routes.Application.testGroups());
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result editGroupForm(Long id) {
    final User localUser = authentication.getLocalUser();
    TestGroup tg = TestGroup.findById(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {

      if (!Util.canAccess(authentication.getLocalUser(), tg.owner))
        return badRequest("You don't have permission to modify this resource");

      GroupEditorModel tgem = new GroupEditorModel();
      tgem.id = tg.id;
      tgem.name = tg.name;
      tgem.shortDescription = tg.shortDescription;
      tgem.description = tg.description;
      Form<GroupEditorModel> bind = TEST_GROUP_FORM.fill(tgem);

      return ok(testGroupEditor.render(bind, authentication));
    }
  }

  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doEditGroupField() {
    JsonNode jsonNode = request().body().asJson();

    Result result = ok();
    try {
      Ebean.beginTransaction();
      result = Utils.doEditField(GroupEditorModel.class, TestGroup.class, jsonNode, authentication.getLocalUser());

      String field = jsonNode.findPath("field").asText();

      if (field.equals("dependencyString")) {
        String dependencyString = jsonNode.findPath("newValue").asText();
        if (dependencyString != null)
          dependencyString = dependencyString.trim();
        if (dependencyString == null || dependencyString.length() == 0) {
          return result; // do nothing
        } else {
          try {
            DependencyClassLoaderCache.getDependencyClassLoader(dependencyString);
            Ebean.commitTransaction();
          } catch (DependencyResolutionException ex) {
            Logger.error(ex.getMessage(), ex);
            return badRequest(ex.getResult().getRoot().getArtifact() + " couldn't be resolved.");
          } catch (Exception ex) {
            Logger.error(ex.getMessage(), ex);
            return badRequest("There was a problem with the dependency string.<br /> \n" +
                "Please make sure that the dependencies are in format:<br />\n " +
                "groupId:artifactId[:extension[:classifier]]:version]]");
          }
        }
      } else {
        Ebean.commitTransaction();
      }
    } finally {
      Ebean.endTransaction();
    }
    return result;
  }


  @AllowedRoles(Role.TEST_DESIGNER)
  public Result doDeleteGroup(Long id) {
    TestGroup tg = TestGroup.findById(id);
    if (tg == null) {
      return badRequest("Test group with id [" + id + "] not found!");
    } else {
      if (!Util.canAccess(authentication.getLocalUser(), tg.owner))
        return badRequest("You don't have permission to modify this resource");

      try {
        tg.delete();
      } catch (Exception ex) {
        ex.printStackTrace();
        return badRequest(ex.getMessage());
      }
      return ok();
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result getGroupDetailView(Long id, String display) {
    TestGroup tg = TestGroup.findById(id);
    if (tg == null) {
      return badRequest("No job with id " + id + ".");
    }
    return ok(mainView.render(tg, display, authentication));
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderTestAssertionList(long id) {
    return ok(testAssertionList.render(TestGroup.findById(id), authentication));
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderTestSuites(long id) {
    return ok(testSuiteList.render(TestGroup.findById(id), authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderTestAssets(long id) {
    return ok(testAssetList.render(TestGroup.findById(id), authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderUtilClasses(long id) {
    return ok(utilClassList.render(TestGroup.findById(id), authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderDependencies(long id) {
    return ok(dependencies.render(TestGroup.findById(id), authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderDetails(long id) {
    return ok(details.render(TestGroup.findById(id), authentication));
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public Result createNewTestGroupImportForm() {
    return ok(testGroupImportEditor.render(form(GroupImportModel.class)));
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result exportTestGroup(Long id) {
    RestTestGroup responseRestTestGroup;
    try {
      responseRestTestGroup = testGroupImportExportController.exportTestGroupData(id);
    } catch (NotFoundException e) {
      return internalServerError(e.getMessage());
    } catch (IOException e) {
      return internalServerError(e.getMessage());
    }

    /*
    * Preparing response
    * */
    IRestContentProcessor contentProcessor = null;
    try {
      contentProcessor = RestUtils.createContentProcessor("text/json");
    } catch (IllegalArgumentException e) {
      return badRequest(e.getCause().toString());
    }

    String responseValue = null;
    try {
      responseValue = contentProcessor.prepareResponse(RestTestGroup.class.getName(), responseRestTestGroup);
    } catch (ParseException e) {
      return internalServerError(e.getMessage());
    }
    System.out.println("responseValue:" + responseValue);

    response().setContentType("application/x-download");
    response().setHeader("Content-disposition", "attachment; filename=" + responseRestTestGroup.getGroupName() + ".json");
    return ok(responseValue);
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result importTestGroup() {
    final Form<GroupImportModel> filledForm = TEST_GROUP_IMPORT_FORM.bindFromRequest();

    User localUser = authentication.getLocalUser();
    if (localUser.email.equals("root@minder")) {

      if (filledForm.hasErrors()) {
        Util.printFormErrors(filledForm);
        filledForm.reject("There is error in the form");
        return badRequest(testGroupImportEditor.render(filledForm));
      } else {
        GroupImportModel model = filledForm.get();

        User user = User.findByEmail(model.useremail);
        if (null == user) {
          filledForm.reject("You can't use this resource");
          return badRequest(testGroupImportEditor.render(filledForm));
        }

        //file upload part
        try {
          handleFileUpload(user);
        } catch (IllegalArgumentException ex) {
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(testGroupImportEditor.render(filledForm));
        } catch (IOException ex) {
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(testGroupImportEditor.render(filledForm));
        } catch (ParseException ex) {
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(testGroupImportEditor.render(filledForm));
        } catch (RuntimeException ex) {
          Logger.error(ex.getMessage(), ex);
          filledForm.reject(ex.getMessage());
          return badRequest(testGroupImportEditor.render(filledForm));
        }

        return ok(testGroupListView.render(authentication));
      }
    } else {
      filledForm.reject("Only root role may import a test group");
      return badRequest(testGroupImportEditor.render(filledForm));
    }
  }

  /**
   * Reads the file uploaded with the current request (if any)
   */
  private void handleFileUpload(User user) throws IllegalArgumentException, IOException, ParseException, RuntimeException {
    Http.MultipartFormData body = request().body().asMultipartFormData();

    if (body == null)
      throw new RuntimeException("The form type is not correct");

    File asset = null;

    if (body.getFiles() != null && body.getFiles().size() > 0)
      asset = ((FilePart) body.getFiles().get(0)).getFile();

    if (asset == null) {
      throw new RuntimeException("No asset file was specified!");
    } else {
      /*
      * Handling import request
      * */
      String bodyStr = null;
      try {
        bodyStr = Files.toString(asset, Charset.defaultCharset());

      } catch (IOException e) {
        throw new IOException(e.getCause().toString());
      }

      IRestContentProcessor contentProcessor = null;
      try {
        contentProcessor = RestUtils.createContentProcessor("text/json", bodyStr);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(e.getCause().toString());
      }

      RestTestGroup restTestGroup = null;
      try {
        restTestGroup = (RestTestGroup) contentProcessor.parseRequest(RestTestGroup.class.getName());
      } catch (ParseException e) {
        throw new ParseException(e.getCause().toString(), 0);
      }

      try {
        testGroupImportExportController.importTestGroupData(restTestGroup, user.email);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderJobTemplates(long id) {
    return ok(jobTemplatesList.render(TestGroup.findById(id), authentication));
  }


  @AllowedRoles({Role.TEST_DESIGNER, Role.TEST_OBSERVER})
  public Result renderReportTemplates(long id) {
    return ok(reportTemplatesList.render(TestGroup.findById(id), authentication));
  }
}







