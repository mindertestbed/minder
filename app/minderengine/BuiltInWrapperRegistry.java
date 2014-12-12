package minderengine;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

/**
 * A special map that holds the built-in wrappers as local IMinderClient Objects
 * Created by yerlibilgin on 11/12/14.
 */
public class BuiltInWrapperRegistry extends HashMap<String, IMinderClient>{
  /**
   * A singleton instance that holds the currently registered built-in wrappers
   */
  private static BuiltInWrapperRegistry builtInWrapperRegistry;

  public static BuiltInWrapperRegistry get() {

    if (builtInWrapperRegistry == null){
      builtInWrapperRegistry = new BuiltInWrapperRegistry();
    }

    return builtInWrapperRegistry;
  }

  public IMinderClient getBuiltInWrapper(String wrapperId){
    return get(wrapperId);
  }

  /**
   * Registers a new built-in wrapper into the system.
   *
   * @param wrapperId
   * @param builtInWrapper
   */
  public void registerWrapper(String wrapperId, IMinderClient builtInWrapper){
    this.put(wrapperId, builtInWrapper);
  }

  public void initiate() {
    //Register our built-in wrappers. Read the definitions from built-in.properties file
    Properties p = new Properties();
    try {
      p.load(this.getClass().getResourceAsStream("/built-in-wrappers.properties"));
    } catch (IOException e) {
      throw new IllegalArgumentException("Couldn't load built-in-wrappers.properties file");
    }

    for(Object obj : Collections.list(p.propertyNames())){
      String name = obj.toString();
      String value = p.getProperty(name);

      try {
        Class<IMinderClient> clz = (Class<IMinderClient>) Class.forName(value);
        registerWrapper(name, clz.newInstance());
      } catch (Exception e) {
        throw new IllegalArgumentException(name + "=" + value + " is an illegal built-in wrapper.");
      }
    }
  }
}
