package minderengine;

import org.interop.xoola.tcpcom.connmanager.server.ClientAccessController;

/**
 * This class implements Xoola ClientAccessController and is fed to xoola through application.config file
 *
 * Through this we will make sure that only registered wrappers will be
 * able to connect to our server.
 * Created by yerlibilgin on 04/12/14.
 */
public class MinderWrapperAccessController implements ClientAccessController{
  public MinderWrapperAccessController(){
    System.err.println("Create the ACCESS Controller");
  }
  //currently allow this GUID. But we should check from DB
  //1d87d345-1ad3-42cc-903a-4a0d400b27cb
  @Override
  public boolean clientIsAllowed(String s) {
    if (s.equals("1d87d345-1ad3-42cc-903a-4a0d400b27cb")){
      System.out.println("ALLOW " + s);
      return true;
    }
    System.out.println("DENY " + s);
    return false;
  }
}
