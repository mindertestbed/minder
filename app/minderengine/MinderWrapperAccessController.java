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
  public boolean clientIsAllowed(String label) {
    Logger.info("A client with name [" + label + "] is trying to connect...");
    Wrapper wrp = Wrapper.findByName(label);
    if (wrp != null) {
      Logger.info("ALLOW [" + label + "]");
      return true;
    }
    Logger.info("DENY [" + label + "]");
    return false;
  }
}
