package minderengine;

import builtin.BuiltInWrapper;
import models.*;
import models.Wrapper;

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
public class BuiltInWrapperRegistry extends HashMap<AdapterIdentifier, BuiltInWrapper> {
  /**
   * A singleton instance that holds the currently registered built-in wrappers
   */
  private static BuiltInWrapperRegistry builtInWrapperRegistry;

  public static BuiltInWrapperRegistry get() {

    if (builtInWrapperRegistry == null) {
      builtInWrapperRegistry = new BuiltInWrapperRegistry();
    }

    return builtInWrapperRegistry;
  }

  public boolean containsWrapper(AdapterIdentifier identifier) {
    return containsKey(identifier);
  }

  public BuiltInWrapper getWrapper(AdapterIdentifier identifier) {
    return get(identifier);
  }

  public boolean containsWrapperByName(String label) {
    //ignoring versions, find the adapter that matches the name
    for (AdapterIdentifier key : keySet()) {
      if (key.getName().equals(label))
        return true;
    }
    return false;
  }

  public BuiltInWrapper getWrapperByName(String label) {
    //ignoring versions, find the adapter that matches the name
    for (AdapterIdentifier key : keySet()) {
      if (key.getName().equals(label))
        return get(key);
    }
    return null;

  }

  /**
   * Registers a new built-in wrapper into the system.
   *
   * @param property       label|version
   * @param builtInWrapper
   */
  public void registerWrapper(String property, BuiltInWrapper builtInWrapper) {

    //register the signals-slots to the MinderWrapperRegistry
    //send the information to the server
    HashSet<MethodContainer> keys = new HashSet();
    for (Method m : builtInWrapper.getClass().getDeclaredMethods()) {
      if (m.getAnnotation(Signal.class) != null || m.getAnnotation(Slot.class) != null) {
        MethodContainer mc = new MethodContainer(m);
        keys.add(mc);
      }
    }

    //check the database and create the wrapper if it does not exist.
    //MinderWrapperRegistry does the job of writing signal-slots

    AdapterIdentifier identifier = AdapterIdentifier.parse(property);
    if (identifier.getVersion() == null)
      throw new RuntimeException("A built-in wrapper has to have a version [" + property + "] is invalid");

    this.put(identifier, builtInWrapper);
    models.Wrapper wr = Wrapper.findByName(identifier.getName());
    if (wr == null) {
      wr = new Wrapper();
      wr.shortDescription = builtInWrapper.getShortDescription();
      wr.name = identifier.getName();
      //the user is system
      wr.user = User.findByEmail("root@minder");
      wr.save();
      //we don't register version, as the actual Minder Wrapper Registry will do
    }

    MinderWrapperRegistry.get().updateWrapper(identifier, keys);
  }

  public void initiate() {
    //Register our built-in wrappers. Read the definitions from built-in.properties file
    Properties p = new Properties();
    try {
      p.load(this.getClass().getResourceAsStream("/built-in-wrappers.properties"));
    } catch (IOException e) {
      throw new IllegalArgumentException("Couldn't load built-in-wrappers.properties file");
    }

    for (Object obj : Collections.list(p.propertyNames())) {
      String name = obj.toString();
      String value = p.getProperty(name);
      try {
        Class<BuiltInWrapper> clz = (Class<BuiltInWrapper>) Class.forName(value);
        BuiltInWrapper wrapper = clz.newInstance();
        registerWrapper(name, wrapper);
      } catch (Exception e) {
        e.printStackTrace();
        throw new IllegalArgumentException(name + "=" + value + " is an illegal built-in wrapper.", e);
      }
    }
  }
}
