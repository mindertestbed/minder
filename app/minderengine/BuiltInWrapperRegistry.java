package minderengine;

import builtin.BuiltInWrapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * A special map that holds the built-in wrappers as local IMinderClient Objects
 * Created by yerlibilgin on 11/12/14.
 */
public class BuiltInWrapperRegistry extends HashMap<String, BuiltInWrapper>{
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

  public boolean containsWrapper(String label){
    return containsKey(label);
  }

  public BuiltInWrapper getWrapper(String label){
    return get(label);
  }

  /**
   * Registers a new built-in wrapper into the system.
   *
   * @param label
   * @param builtInWrapper
   */
  public void registerWrapper(String label, BuiltInWrapper builtInWrapper){
    this.put(label, builtInWrapper);
    //register the signals-slots to the MinderWrapperRegistry
    //send the information to the server
    HashSet<MethodContainer> keys = new HashSet();
    for (Method m : builtInWrapper.getClass().getDeclaredMethods()){
      if( m.getAnnotation(Signal.class) != null || m.getAnnotation(Slot.class) != null){
        MethodContainer mc = new MethodContainer(m);
        keys.add(mc);
      }
    }
    MinderWrapperRegistry.get().updateWrapper(label, label, keys);
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
        Class<BuiltInWrapper> clz = (Class<BuiltInWrapper>) Class.forName(value);
        BuiltInWrapper wrapper = clz.newInstance();
        registerWrapper(name, wrapper);
      } catch (Exception e) {
        throw new IllegalArgumentException(name + "=" + value + " is an illegal built-in wrapper.");
      }
    }
  }
}
