package global;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import controllers.routes;
import minderengine.BuiltInWrapperRegistry;
import minderengine.ReflectionUtils;
import minderengine.XoolaServer;
import models.*;
import mtdl.MinderTdl;
import mtdl.TdlCompiler;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import play.Application;
import play.GlobalSettings;
import play.api.Play;
import play.db.ebean.Model;
import play.mvc.Call;
import scala.io.BufferedSource;
import scala.io.Source;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    ReflectionUtils.defaultClassLoader = Play.classloader(Play.current());
    BuiltInWrapperRegistry.get().initiate();
    XoolaServer.get().start();
  }

  private void initialData() {
    if (SecurityRole.find.findRowCount() == 0) {
      String rolesList[] = new String[]{controllers.Application.OBSERVER_ROLE, controllers.Application.TEST_DESIGNER_ROLE, controllers.Application.TEST_DEVELOPER_ROLE};
      for (final String roleName : Arrays
          .asList(rolesList)) {
        final SecurityRole role = new SecurityRole();
        role.roleName = roleName;
        role.save();
      }
    }

    if (User.find.findRowCount() == 0) {
      System.out.println("Adding sample data");
      try {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(Play.classloader(Play.current())));

        Map<String, List<Model>> all = (Map<String, List<Model>>) yaml.load(new FileInputStream("conf/initial-data.yml"));

        for (String key : all.keySet()) {
          for (Model model : all.get(key)) {
            model.save();

            if (model instanceof TestGroup){
              TestGroup group = (TestGroup) model;
              for(TestAssertion assertion : group.testAssertions){
                for (TestCase tcase : assertion.testCases){
                  BufferedSource file = Source.fromFile(tcase.tdl, "utf-8");
                  System.out.println("TDL: " + tcase.tdl);
                  tcase.setTdl(file.mkString());
                  tcase.save();
                }
              }
            }
            if (model instanceof  RunConfiguration){
              RunConfiguration rc = (RunConfiguration) model;

              for (MappedWrapper mappedWrapper : rc.mappedWrappers) {
              }
            }
          }
        }
      } catch (Throwable th){
        th.printStackTrace();
      }
    }
  }
}
