package utils;

import com.avaje.ebean.Model;
import controllers.MappedAdapterModel;
import minderengine.BuiltInAdapterRegistry;
import minderengine.XoolaServer;
import models.*;
import mtdl.TDLClassLoaderProvider;
import org.beybunproject.xmlContentVerifier.utils.Utils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import play.Configuration;
import play.api.Environment;
import play.data.format.Formatters;
import scala.io.BufferedSource;
import scala.io.Source;

import javax.inject.Inject;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(Startup.class);
  private final Environment environment;
  private final Configuration configuration;

  @Inject
  public Startup(Environment environment, XoolaServer xoolaServer, Formatters formatters, Configuration configuration) {
    this.environment = environment;
    this.configuration = configuration;

    TDLClassLoaderProvider.appendExternalClassLoader(environment.classLoader());
    TDLClassLoaderProvider.appendExternalClassLoader(ClassLoader.getSystemClassLoader());

    initialData();

    BuiltInAdapterRegistry.get().initiate();
    xoolaServer.start();

    //TODO: Bad place for this code. Where to put?
    formatters.register(MappedAdapterModel.class, new Formatters.SimpleFormatter<MappedAdapterModel>() {
      @Override
      public MappedAdapterModel parse(String jsonString, Locale arg1) throws ParseException {
        return MappedAdapterModel.parse(jsonString);
      }

      @Override
      public String print(MappedAdapterModel mappedAdapterModel, Locale arg1) {
        return mappedAdapterModel.toJson();
      }
    });
  }

  private void initialData() {
    if (User.findRowCount() == 0) {

      final String assetsDir = configuration.getString("minder.data.dir", "./data") + "/assets";

      LOGGER.debug("Adding sample data");
      try {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(environment.classLoader()));

        File currentDir = new File(".");
        LOGGER.debug("Current Directory:" + currentDir.getAbsolutePath());

        File yml = new File(currentDir.getAbsoluteFile() + "/conf/initialdata/initial-data.yml");
        LOGGER.debug("Yml file: " + yml.getAbsolutePath() + " exists? " + yml.exists());

        Map<String, List<Model>> all = (Map<String, List<Model>>) yaml.load(new FileInputStream(yml));
        for (String key : all.keySet()) {
          for (Model model : all.get(key)) {
            LOGGER.debug(model.getClass().getSimpleName());
            model.save();

            if (model instanceof TestGroup) {
              TestGroup group = (TestGroup) model;

              LOGGER.debug("ID: " + group.id);
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
                    TdlUtils.detectAndSaveParameters(tdl);
                  }
                }
              }

              for (TestAsset asset : group.testAssets) {
                FileInputStream fis = new FileInputStream("conf/initialdata/assets/" + asset.name);
                byte[] assetBytes = Utils.readStream(fis);
                fis.close();
                final String groupAssetRoot = assetsDir + "/_" + group.id + "/";
                new File(groupAssetRoot).mkdirs();
                FileOutputStream fos = new FileOutputStream(groupAssetRoot + asset.name);
                fos.write(assetBytes);
                fos.close();
              }
            }
          }
        }
      } catch (Throwable th) {
        LOGGER.error(th.getMessage(), th);
      }
    }
  }

}
