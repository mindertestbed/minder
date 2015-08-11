package minderengine;

import mtdl.DefaultTDLClassLoader;
import org.interop.xoola.tcpcom.connmanager.server.IClassLoaderProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author: yerlibilgin
 * @date: 11/08/15.
 */
public class MinderClassLoaderProvider implements IClassLoaderProvider {

   public MinderClassLoaderProvider(){
     System.out.println("MinderClassLoaderProvider initialized");
   }
  @Override
  public ClassLoader getClassLoader() {
    URL []urls = new URL[1];
    try {
      urls[0] = new URL("file:///");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return new MyClassLoader(urls);
  }


  private class MyClassLoader extends  URLClassLoader{
    public MyClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    public MyClassLoader(URL[] urls) {
      super(urls);
    }

    public MyClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
      super(urls, parent, factory);
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      ClassLoader lds = TestEngine.getCurrentMTDLClassLoader();
      if (lds instanceof  DefaultTDLClassLoader){
        DefaultTDLClassLoader ldss = (DefaultTDLClassLoader) lds;
        return ldss.loadClass(name, resolve);
      }

      return lds.loadClass(name);
    }
  }
}
