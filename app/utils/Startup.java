package utils;

import com.avaje.ebean.Model;
import controllers.MappedWrapperModel;
import controllers.TestCaseController;
import minderengine.BuiltInWrapperRegistry;
import minderengine.XoolaServer;
import models.*;
import mtdl.TDLClassLoaderProvider;
import org.beybunproject.xmlContentVerifier.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import play.Logger;
import play.api.Environment;
import play.api.Play;
import play.data.format.Formatters;
import scala.io.BufferedSource;
import scala.io.Source;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
public class Startup {
  private final Environment environment;

  @Inject
  public Startup(Environment environment, XoolaServer xoolaServer, Formatters formatters) {
    this.environment = environment;
    System.out.println(xoolaServer);

    TDLClassLoaderProvider.appendExternalClassLoader(environment.classLoader());
    TDLClassLoaderProvider.appendExternalClassLoader(ClassLoader.getSystemClassLoader());

    initialData();

    BuiltInWrapperRegistry.get().initiate();
    xoolaServer.start();

    //TODO: Bad place for this code. Where to put?
    formatters.register(MappedWrapperModel.class, new Formatters.SimpleFormatter<MappedWrapperModel>() {
      @Override
      public MappedWrapperModel parse(String jsonString, Locale arg1) throws ParseException {
        return MappedWrapperModel.parse(jsonString);
      }

      @Override
      public String print(MappedWrapperModel mappedWrapperModel, Locale arg1) {
        return mappedWrapperModel.toJson();
      }
    });
  }

  private void initialData() {
    if (User.findRowCount() == 0) {
      System.out.println("Adding sample data");
      try {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(environment.classLoader()));

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

              System.out.println("ID: " + group.id);
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

              for (TestAsset asset : group.testAssets) {
                FileInputStream fis = new FileInputStream("conf/initialdata/" + asset.name);
                byte[] assetBytes = Utils.readStream(fis);
                fis.close();
                final String groupAssetRoot = "assets/_" + group.id + "/";
                new File(groupAssetRoot).mkdirs();
                FileOutputStream fos = new FileOutputStream(groupAssetRoot + asset.name);
                fos.write(assetBytes);
                fos.close();
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
