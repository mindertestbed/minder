package minderengine;

import org.interop.xoola.core.*;
import org.interop.xoola.tcpcom.connmanager.server.ServerRegistry;
import play.api.Environment;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by yerlibilgin on 02/12/14.
 */
@Singleton
public class XoolaServer{

  @Inject
  private MinderServer minderServer;

  @Inject
  Environment environment;

  private static XoolaServer xoolaServer;

  private Xoola server;
  private boolean started = false;

  public static Properties properties;
  /**
   * Use this lock to ensure globally that only one thread tries to start the server
   */
  private static Object startLock = new Object();

  public void start() {
    if (started) {
      System.out.println("Server already started");
      return;
    }

    started = true;
    synchronized (startLock) {
      try {
        //load the xoola properties from resource "xoola.properties"
        properties = new java.util.Properties();
        properties.load(new FileInputStream("XoolaServer.properties"));
        properties.setProperty(XoolaProperty.MODE, XoolaTierMode.SERVER);

        ServerRegistry.classLoader = environment.classLoader();
        server = Xoola.init(properties);
        System.out.println(properties.getProperty("PORT"));
        System.out.println("Created xoola server");
        server.registerObject("minderServer", minderServer);
        System.out.println("Registered minderServer object");

        server.addConnectionListener(new XoolaConnectionListener() {
          @Override
          public synchronized void connected(XoolaInvocationHandler xoolaInvocationHandler, XoolaChannelState xoolaChannelState) {
            System.out.println("CLIENT: " + xoolaChannelState.remoteId + " connected");
            MinderAdapterRegistry.get().setAdapterAvailable(xoolaChannelState.remoteId, true);
          }

          @Override
          public void disconnected(XoolaInvocationHandler xoolaInvocationHandler, XoolaChannelState xoolaChannelState) {
            System.out.println("CLIENT: " + xoolaChannelState.remoteId + " disconnected");
            MinderAdapterRegistry.get().setAdapterAvailable(xoolaChannelState.remoteId, false);
          }
        });
        System.out.println("Starting server");
        server.start();

        System.out.println("Started server");
        started = true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public IMinderClient getClient(AdapterIdentifier adapterIdentifier){
    return server.get(IMinderClient.class, adapterIdentifier.toString(), "minderClient");
  }
}
