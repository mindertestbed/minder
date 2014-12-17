package minderengine;

import org.interop.xoola.tcpcom.connmanager.server.ClientAccessController;

import java.util.HashSet;

/**
 * This class implements Xoola ClientAccessController and is fed to xoola through application.config file
 *
 * Through this we will make sure that only registered wrappers will be
 * able to connect to our server.
 * Created by yerlibilgin on 04/12/14.
 */
public class MinderWrapperAccessController implements ClientAccessController{
  public static void main(String[] args) {
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
    System.out.println(java.util.UUID.randomUUID().toString());
  }
  //dummy solution for now
  private HashSet<String> allowedGuids = new HashSet<>();

  public MinderWrapperAccessController(){
    System.err.println("Create the ACCESS Controller");
  }

  @Override
  public boolean clientIsAllowed(String s) {
    if (s.equals("1d87d345-1ad3-42cc-903a-4a0d400b27cb")){
      System.out.println("ALLOW " + s);
      return true;
    }
    System.out.println("ALLOW " + s);
    return true;
  }
}
