package minderengine;

import mtdl.DefaultTDLClassLoader;
import org.interop.xoola.tcpcom.connmanager.server.IClassLoaderProvider;
import play.api.Play;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author: yerlibilgin
 * @date: 11/08/15.
 */
public class MinderClassLoaderProvider implements IClassLoaderProvider {

  public MinderClassLoaderProvider() {
    System.out.println("MinderClassLoaderProvider initialized");
  }

  @Override
  public ClassLoader getClassLoader() {
    return Play.current().classloader();//new MyClassLoader(urls);
  }
}
