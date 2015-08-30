package global;

import com.avaje.ebean.Model;
import controllers.TestCaseController;
import minderengine.BuiltInWrapperRegistry;
import minderengine.XoolaServer;
import models.*;
import mtdl.TDLClassLoaderProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.Play;
import play.mvc.Http;
import scala.io.BufferedSource;
import scala.io.Source;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Global extends GlobalSettings {
  public void onStart(Application app) {
    initialData();
    TDLClassLoaderProvider.appendExternalClassLoader(Play.classloader(Play.current()));
    TDLClassLoaderProvider.appendExternalClassLoader(ClassLoader.getSystemClassLoader());
    BuiltInWrapperRegistry.get().initiate();
    XoolaServer.get().start();
  }

  public static void main(String[] args) {
    System.out.println(Base64.getEncoder().encodeToString(Util.sha256("12345".getBytes())));
  }

  private void initialData() {
    if (User.findRowCount() == 0) {
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
