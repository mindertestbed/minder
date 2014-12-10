package minderengine;

import org.apache.log4j.PropertyConfigurator;
import org.interop.xoola.core.*;
import org.interop.xoola.tcpcom.connmanager.server.ServerRegistry;
import play.api.Play;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by yerlibilgin on 02/12/14.
 */
public class XoolaServer {
  private static XoolaServer xoolaServer;
  private Xoola server;
  private MinderServer minderServer = new MinderServer();
  private boolean started = false;
  /**
   * Use this lock to ensure globally that only one thread tries to start the server
   */
  private static Object startLock = new Object();

  public static XoolaServer get() {
    if (xoolaServer == null) {
      xoolaServer = new XoolaServer();
    }

    return xoolaServer;
  }

  public void start() {
    if (started) {
      System.out.println("Server already started");
      return;
    }

    synchronized (startLock) {
      try {
        //load the xoola properties from resource "xoola.properties"
        Properties properties = new java.util.Properties();
        properties.load(this.getClass().getResourceAsStream("/application.conf"));
        properties.setProperty(XoolaProperty.MODE, XoolaTierMode.SERVER);

        try {
          PropertyConfigurator.configure(this.getClass().getResource("/logging.properties"));
        } catch (Throwable ex) {
          System.err.println("WARN " + ex.getMessage());
        }

        server = Xoola.init(properties);
        System.out.println(properties.getProperty("PORT"));
        System.out.println("Created xoola server");
        server.registerObject("minderServer", minderServer);
        System.out.println("Registered minderServer object");

        server.addConnectionListener(new XoolaConnectionListener() {
          @Override
          public void connected(XoolaInvocationHandler xoolaInvocationHandler, XoolaChannelState xoolaChannelState) {
            System.out.println("CLIENT: " + xoolaChannelState.remoteId + " connected");
          }

          @Override
          public void disconnected(XoolaInvocationHandler xoolaInvocationHandler, XoolaChannelState xoolaChannelState) {

          }
        });
        server.start();
        started = true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public IMinderClient getClient(String wrapperId){
    return server.get(IMinderClient.class, wrapperId, "minderClient");
  }
}
