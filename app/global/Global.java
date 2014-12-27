package global;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import controllers.routes;
import minderengine.BuiltInWrapperRegistry;
import minderengine.ReflectionUtils;
import minderengine.XoolaServer;
import models.SecurityRole;
import models.TestCaseCategory;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.api.Play;
import play.mvc.Call;

import java.util.Arrays;
import java.util.List;

public class Global extends GlobalSettings {

  public void onStart(Application app) {
    ReflectionUtils.defaultClassLoader = Play.classloader(Play.current());
    BuiltInWrapperRegistry.get().initiate();
    XoolaServer.get().start();

    PlayAuthenticate.setResolver(new Resolver() {
      @Override
      public Call login() {
        // Your login page
        return routes.Application.login();
      }

      @Override
      public Call afterAuth() {
        // The user will be redirected to this page after authentication
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

    User user =  User.find.findUnique();

    if (user != null){
      System.out.println(user);
      System.out.println(user.email);
    } else{
      System.out.println("User null");
    }
  }
}
