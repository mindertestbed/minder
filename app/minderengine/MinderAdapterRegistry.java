package minderengine;

import com.avaje.ebean.Ebean;
import models.*;
import models.Adapter;
import mtdl.*;
import play.Logger;
import scala.actors.threadpool.Arrays;

import java.util.*;

/**
 * This class holds the active state of the adapters. When a adapter connects to Minder,
 * this adapter keeps a DB mirror for two reasons:
 * <br>
 * <ol>
 * <li>fast access</li>
 * <li>determining whether a adapter is online or not.</li>
 * </ol>
 * Created by yerlibilgin on 13/12/14.
 */
public class MinderAdapterRegistry extends Observable {
  private static MinderAdapterRegistry instance;
  private HashMap<AdapterIdentifier, HashMap<String, AdapterFunction>> adapterMap;
  private HashSet<Long> onlineAdapters;

  public static MinderAdapterRegistry get() {
    if (instance == null) {
      instance = new MinderAdapterRegistry();
    }
    return instance;
  }

  private MinderAdapterRegistry() {
    adapterMap = new HashMap<>();
    onlineAdapters = new HashSet<>();
  }


  /**
   * Creates a new adapter if it doesn't exist, updates an existing one
   *
   * @param identifier
   * @param methodSet
   */
  public void updateAdapter(AdapterIdentifier identifier, Set<MethodContainer> methodSet) {
    final String name = identifier.getName();
    Logger.info("Adapter " + name + " connected. Updating signatures");
    //update the database for possible changes in the signatures
    Adapter adapter = Adapter.findByName(name);
    Logger.info(adapter.name);
    AdapterVersion version = AdapterVersion.findAdapterAndVersion(adapter, identifier.getVersion());
    if (version == null) {
      version = new AdapterVersion();
      version.version = identifier.getVersion();
      version.creationDate = new Date();
      version.adapter = adapter;
      version.save();
    }
    //MAPPING_METHODS:
    HashMap<String, AdapterFunction> methodMap = new HashMap<>();
    for (MethodContainer mc : methodSet) {
      AdapterFunction ss = new AdapterFunction(name, mc.methodKey);
      methodMap.put(mc.methodKey, ss);
    }

    try {
      Ebean.beginTransaction();
      TSignal.deleteByVersion(version);
      TSlot.deleteByVersion(version);

      for (MethodContainer mc : methodSet) {
        if (mc.methodKey.startsWith("getCurrentTestUserInfo")) {
          //ignore this, it is meant for use only at client side.
          continue;
        }
        Logger.debug("\t" + (mc.isSignal ? "Signal " : "Slot ") + mc.methodKey);

        if (mc.isSignal) {
          TSignal.createNew(version, mc.methodKey);
        } else {
          TSlot.createNew(version, mc.methodKey);
        }
      }
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();
    }

    onlineAdapters.add(version.id);
    adapterMap.put(identifier, methodMap);
  }

  public List<String> getAllLabels() {
    return Arrays.asList(adapterMap.keySet().toArray());
  }

  public Collection<AdapterFunction> getAllMethods(String label) {
    if (!adapterMap.containsKey(label))
      throw new IllegalArgumentException("A adapter with label " + label + " is not available.");
    return adapterMap.get(label).values();
  }

  public boolean isAdapterAvailable(AdapterVersion adapterVersion) {
    return onlineAdapters.contains(adapterVersion.id);
  }

  public void setAdapterAvailable(String identifier, boolean available) {
    int index = identifier.indexOf('|');
    String name = identifier;
    String versionString = "NA";
    if (index > 0) {
      name = identifier.substring(0, index);
      versionString = identifier.substring(index + 1);
    }
    Adapter adapter = Adapter.findByName(name);
    AdapterVersion version = AdapterVersion.findAdapterAndVersion(adapter, versionString);

    if (version == null) {
      version = new AdapterVersion();
      version.version = versionString;
      version.creationDate = new Date();
      version.adapter = adapter;
      version.save();
    }

    if (available)
      onlineAdapters.add(version.id);
    else
      onlineAdapters.remove(version.id);
    setChanged();
    notifyObservers(version.id);
  }
}
