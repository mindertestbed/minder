package minderengine;

import models.*;
import models.Wrapper;
import org.interop.xoola.tcpcom.connmanager.server.ClientAccessController;
import play.Logger;

import java.util.HashSet;
import java.util.List;

/**
 * This class implements Xoola ClientAccessController and is fed to xoola through application.config file
 *
 * Through this we will make sure that only registered wrappers will be
 * able to connect to our server.
 * Created by yerlibilgin on 04/12/14.
 */
public class MinderWrapperAccessController implements ClientAccessController {
  private HashSet<String> allowedClients;

  public MinderWrapperAccessController() {

  }

  @Override
  public boolean clientIsAllowed(String identifier) {
    Logger.info("A client with name [" + identifier + "] is trying to connect...");

    String label = identifier;
    if (identifier.contains("|")){
      String []tmp = identifier.split("\\|");
      label = tmp[0];
    }

    Wrapper wrp = Wrapper.findByName(label);
    if (wrp != null) {
      Logger.info("ALLOW [" + identifier + "]");
      return true;
    }
    Logger.info("DENY [" + identifier + "]");
    return false;
  }
}
