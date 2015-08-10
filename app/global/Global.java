package global;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import minderengine.BuiltInWrapperRegistry;
import mtdl.ReflectionUtils;
import minderengine.XoolaServer;
import models.*;
import mtdl.TDLClassLoaderProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.Play;
import play.db.ebean.Model;
import play.mvc.Call;
import scala.io.BufferedSource;
import scala.io.Source;

import java.io.*;
import java.util.*;

import controllers.*;

public class Global extends GlobalSettings {
  public void onStart(Application app) {
    PlayAuthenticate.setResolver(new Resolver() {
      @Override
      public Call login() {
        // Your login page
        return routes.Application.login();
      }

      @Override
      public Call afterAuth() {
        // The owner will be redirected to this page after authentication
        // if no original URL was saved
        return routes.Application.index();
      }

      @Override
      public Call afterLogout() {
        return routes.Application.index();
      }

      @Override
      public Call auth(final String provider) {
        // You can provide your own authentication implementation,
        // however the default should be sufficient for most cases
        return com.feth.play.module.pa.controllers.routes.Authenticate
            .authenticate(provider);
      }

      @Override
      public Call askMerge() {
        return routes.Account.askMerge();
      }

      @Override
      public Call askLink() {
        return routes.Account.askLink();
      }

      @Override
      public Call onException(final AuthException e) {
        if (e instanceof AccessDeniedException) {
          return routes.Signup
              .oAuthDenied(((AccessDeniedException) e)
                  .getProviderKey());
        }

        // more custom problem handling here...
        return super.onException(e);
      }
    });

    initialData();
    TDLClassLoaderProvider.appendExternalClassLoader(Play.classloader(Play.current()));
    TDLClassLoaderProvider.appendExternalClassLoader(ClassLoader.getSystemClassLoader());
    BuiltInWrapperRegistry.get().initiate();
    XoolaServer.get().start();
  }

  private void initialData() {
    if (User.find.findRowCount() == 0) {
      System.out.println("Adding sample data");
      try {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(Play.classloader(Play.current())));

        File currentDir = new File(".");
        Logger.debug("Current Directory:" + currentDir.getAbsolutePath());

        File yml = new File(currentDir.getAbsoluteFile() + "/conf/initial-data.yml");
        Logger.debug("Yml file: " + yml.getAbsolutePath() + " exists? " + yml.exists());

        Map<String, List<Model>> all = (Map<String, List<Model>>) yaml.load(new FileInputStream(yml));
        for (String key : all.keySet()) {
          for (Model model : all.get(key)) {
            System.out.println(model);
            model.save();

            if (model instanceof TestGroup) {
              TestGroup group = (TestGroup) model;
              group.save();
              for (TestAssertion assertion : group.testAssertions) {
                assertion.save();
                for (TestCase tcase : assertion.testCases) {
                  tcase.save();
                  for (Tdl tdl : tcase.tdls) {
                    BufferedSource file = Source.fromFile(tdl.tdl, "utf-8");
                    tdl.creationDate = new Date();
                    tdl.tdl = file.mkString();
                    tdl.testCase = tcase;
                    tdl.save();
                    TestCaseController.detectAndSaveParameters(tdl);
                  }
                }
              }
            }
          }
        }
      } catch (Throwable th) {
        th.printStackTrace();
      }
    }
  }
}
