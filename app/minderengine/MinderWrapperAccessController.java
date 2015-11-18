package minderengine;

import models.*;
import models.Wrapper;
import org.interop.xoola.tcpcom.connmanager.server.ClientAccessController;
import play.Logger;

import java.util.HashSet;
import java.util.List;

/**
 * This class implements Xoola ClientAccessController and is fed to xoola through application.config file
 * <p>
 * Through this we will make sure that only registered wrappers will be
 * able to connect to our server.
 * Created by yerlibilgin on 04/12/14.
 */
public class MinderWrapperAccessController implements ClientAccessController {
  @Override
  public boolean clientIsAllowed(String identifierString) {
    Logger.info("A client with name [" + identifierString + "] is trying to connect...");

    AdapterIdentifier identifier = AdapterIdentifier.parse(identifierString);

    if (identifier.getVersion() == null){
      Logger.error("An adapter MUST declare a version");
      Logger.error("DENY [" + identifierString + "]");
      return false;
    }

    Wrapper wrp = Wrapper.findByName(identifier.getName());
    if (wrp == null) {
      Logger.error(identifierString + " is not registered");
      Logger.error("DENY [" + identifierString + "]");
      return false;
    }

    return true;
  }
}
