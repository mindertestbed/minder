package global;

import com.avaje.ebean.Model;
import controllers.TestCaseController;
import minderengine.BuiltInWrapperRegistry;
import minderengine.XoolaServer;
import models.*;
import mtdl.TDLClassLoaderProvider;
import org.beybunproject.xmlContentVerifier.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.Play;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import rest.controllers.common.RestUtils;
import rest.controllers.routes;
import scala.io.BufferedSource;
import scala.io.Source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Global extends GlobalSettings {

    /**
     * Call to create the root Action of a request for a Java application.
     * The request and actionMethod values are passed for information.
     *
     * @param method  The action method containing the user code for this Action.
     * @param request The HTTP Request
     * @return The default implementation returns a raw Action calling the method.
     */
    @SuppressWarnings("rawtypes")
    public play.mvc.Action onRequest(final Http.Request request, Method method) {
        if ((!request.path().contains("/rest/")) || (request.path().equals("/rest/login"))) {
            return super.onRequest(request, method);
        }


        if(RestUtils.verifyAuthentication(request)) {
            return super.onRequest(request, method);
        }


        return new Action.Simple() {
            public F.Promise<Result> call(Http.Context ctx) throws Throwable {
                System.out.println("Calling action for " + ctx);
                return F.Promise.pure(redirect(routes.LoginController.login()));
            }
        };


    }

    public void onStart(Application app) {
        TDLClassLoaderProvider.appendExternalClassLoader(Play.classloader(Play.current()));
        TDLClassLoaderProvider.appendExternalClassLoader(ClassLoader.getSystemClassLoader());

        initialData();

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
