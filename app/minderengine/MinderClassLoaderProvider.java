package minderengine;

import gov.tubitak.xoola.tcpcom.connmanager.server.IClassLoaderProvider;
import play.api.Play;

/**
 * @author: yerlibilgin
 * @date: 11/08/15.
 */
public class MinderClassLoaderProvider implements IClassLoaderProvider {

  public MinderClassLoaderProvider() {
  }

  @Override
  public ClassLoader getClassLoader() {
    return Play.current().classloader();//new MyClassLoader(urls);
  }
}
