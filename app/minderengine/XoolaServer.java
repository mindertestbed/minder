package minderengine;

import org.interop.xoola.core.*;
import org.interop.xoola.tcpcom.connmanager.server.ServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class XoolaServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(XoolaServer.class);

  @Inject
  private MinderServer minderServer;

  @Inject
  Environment environment;

  private Xoola server;
  private boolean started = false;

  public static Properties properties;
  /**
   * Use this lock to ensure globally that only one thread tries to start the server
   */
  private static Object startLock = new Object();

  public void start() {
    if (started) {
      LOGGER.debug("Server already started");
      return;
    }

    started = true;
    synchronized (startLock) {
      try {
        LOGGER.debug("Try to load xoola server properties from \"conf/application.conf\"");
        properties = new java.util.Properties();
        properties.load(new FileInputStream("conf/application.conf"));
        properties.setProperty(XoolaProperty.MODE, XoolaTierMode.SERVER);

        ServerRegistry.classLoader = environment.classLoader();
        server = Xoola.init(properties);
        LOGGER.debug(properties.getProperty("PORT"));
        LOGGER.debug("Created xoola server");
        server.registerObject("minderServer", minderServer);
        LOGGER.debug("Registered minderServer object");

        server.addConnectionListener(new XoolaConnectionListener() {
          @Override
          public synchronized void connected(XoolaInvocationHandler xoolaInvocationHandler, XoolaChannelState xoolaChannelState) {
            LOGGER.debug("CLIENT: " + xoolaChannelState.remoteId + " connected");
            MinderAdapterRegistry.get().setAdapterAvailable(xoolaChannelState.remoteId, true);
          }

          @Override
          public void disconnected(XoolaInvocationHandler xoolaInvocationHandler, XoolaChannelState xoolaChannelState) {
            LOGGER.debug("CLIENT: " + xoolaChannelState.remoteId + " disconnected");
            MinderAdapterRegistry.get().setAdapterAvailable(xoolaChannelState.remoteId, false);
          }
        });
        LOGGER.debug("Starting server");
        server.start();

        LOGGER.debug("Started server");
        started = true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public IMinderClient getClient(AdapterIdentifier adapterIdentifier) {
    return server.get(IMinderClient.class, adapterIdentifier.toString(), "minderClient");
  }
}
