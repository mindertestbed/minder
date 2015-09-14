package minderengine;

import com.avaje.ebean.Ebean;
import models.*;
import models.Wrapper;
import mtdl.*;
import play.Logger;
import scala.actors.threadpool.Arrays;

import java.util.*;

/**
 * This class holds the active state of the wrappers. When a wrapper connects to Minder,
 * this wrapper keeps a DB mirror for two reasons:
 * <br>
 * <ol>
 * <li>fast access</li>
 * <li>determining whether a wrapper is online or not.</li>
 * </ol>
 * Created by yerlibilgin on 13/12/14.
 */
public class MinderWrapperRegistry extends Observable implements ISignalSlotInfoProvider {
  private static MinderWrapperRegistry instance;
  private HashMap<String, HashMap<String, SignalSlot>> wrapperMap;
  private HashSet<Long> onlineWrappers;

  public static MinderWrapperRegistry get() {
    if (instance == null) {
      instance = new MinderWrapperRegistry();
    }
    return instance;
  }

  private MinderWrapperRegistry() {
    wrapperMap = new HashMap<>();
    onlineWrappers = new HashSet<>();
  }


  /**
   * Creates a new wrapper if it doesn't exist, updates an existing one
   *
   * @param identifier
   * @param methodSet
   */
  public void updateWrapper(String identifier, Set<MethodContainer> methodSet) {
    Logger.info("Wrapper " + identifier + " connected. Updating signatures");

    String versionString = "NA";
    String label = identifier;
    if (identifier.contains("|")) {
      String[] tmp = identifier.split("\\|");
      label = tmp[0];
      versionString = tmp[1];
    }
    //update the database for possible changes in the signatures
    Wrapper wrapper = Wrapper.findByName(label);
    Logger.info(wrapper.name);
    WrapperVersion version = WrapperVersion.findWrapperAndVersion(wrapper, versionString);
    if (version == null) {
      version = new WrapperVersion();
      version.version = versionString;
      version.creationDate = new Date();
      version.wrapper = wrapper;
      version.save();
    }
    //MAPPING_METHODS:
    HashMap<String, SignalSlot> methodMap = new HashMap<>();
    for (MethodContainer mc : methodSet) {
      SignalSlot ss;
      if (mc.isSignal) {
        ss = new SignalImpl(label, mc.methodKey);
      } else {
        ss = new SlotImpl(label, mc.methodKey);
      }
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

    if (versionString.equals("NA")) {
      wrapperMap.put(label, methodMap);
    } else {
      wrapperMap.put(label + "|" + versionString, methodMap);
    }
    onlineWrappers.add(version.id);
    wrapperMap.put(label, methodMap);
  }

  public List<String> getAllLabels() {
    return Arrays.asList(wrapperMap.keySet().toArray());
  }

  public Collection<SignalSlot> getAllMethods(String label) {
    if (!wrapperMap.containsKey(label))
      throw new IllegalArgumentException("A wrapper with label " + label + " is not available.");
    return wrapperMap.get(label).values();
  }

  /**
   * @param searchKey either the wrapperName or wrapperName|version
   * @param signature
   * @return
   */
  @Override
  public SignalSlot getSignalSlot(String searchKey, String signature) {
    signature = signature.replaceAll("\\s", "");
    if (!wrapperMap.containsKey(searchKey))
      throw new IllegalArgumentException("No wrapper with name [" + searchKey + "]");

    SignalSlot ss = wrapperMap.get(searchKey).get(signature);

    if (ss == null) {
      throw new IllegalArgumentException("No such signal or slot: [" + searchKey + "." + signature + "]");
    }

    return ss;
  }

  public boolean isWrapperAvailable(WrapperVersion wrapperVersion) {
    return onlineWrappers.contains(wrapperVersion.id);
  }

  public void setWrapperAvailable(String identifier, boolean available) {
    int index = identifier.indexOf('|');
    String name = identifier;
    String versionString = "NA";
    if (index > 0) {
      name = identifier.substring(0, index);
      versionString = identifier.substring(index + 1);
    }
    Wrapper wrapper = Wrapper.findByName(name);
    WrapperVersion version = WrapperVersion.findWrapperAndVersion(wrapper, versionString);

    if (version == null) {
      version = new WrapperVersion();
      version.version = versionString;
      version.creationDate = new Date();
      version.wrapper = wrapper;
      version.save();
    }

    if (available)
      onlineWrappers.add(version.id);
    else
      onlineWrappers.remove(version.id);
    setChanged();
    notifyObservers(version.id);
  }
}
