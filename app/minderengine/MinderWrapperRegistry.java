package minderengine;

import builtin.Entry;
import com.avaje.ebean.Ebean;
import models.*;
import models.Wrapper;
import mtdl.*;
import play.Logger;
import scala.actors.threadpool.Arrays;

import java.util.*;

/**
 *
 * This class holds the active state of the wrappers. When a wrapper connects to Minder,
 * this wrapper keeps a DB mirror for two reasons:
 * <br>
 *   <ol>
 *  <li>fast access</li>
 *   <li>determining whether a wrapper is online or not.</li>
 * </ol>
 * Created by yerlibilgin on 13/12/14.
 */
public class MinderWrapperRegistry implements ISignalSlotInfoProvider {
  private static MinderWrapperRegistry instance;
  private HashMap<String, HashMap<String, SignalSlot>> wrapperMap;

  public static MinderWrapperRegistry get() {
    if (instance == null) {
      instance = new MinderWrapperRegistry();
    }
    return instance;
  }

  private MinderWrapperRegistry() {
    wrapperMap = new HashMap<>();
  }


  /**
   * Creates a new wrapper if it doesn't exist, updates an existing one
   *
   * @param label
   * @param methodSet
   */
  public void updateWrapper(String label, Set<MethodContainer> methodSet) {
    Logger.info("Wrapper " + label + " connected. Updating signatures");

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

    wrapperMap.put(label, methodMap);

    //update the database for possible changes in the signatures
    Wrapper wrapper = Wrapper.findByName(label);
    try {
      Ebean.beginTransaction();
      TSignal.deleteByWrapper(wrapper);
      TSlot.deleteByWrapper(wrapper);

      for (MethodContainer mc : methodSet) {
        Logger.debug("\t" + (mc.isSignal ? "Signal " : "Slot ") + mc.methodKey);

        if (mc.isSignal){
          TSignal.createNew(wrapper, mc.methodKey);
        } else{
          TSlot.createNew(wrapper, mc.methodKey);
        }
      }
      Ebean.commitTransaction();
    } catch (Exception ex) {
      ex.printStackTrace();
      Ebean.endTransaction();
    }
  }

  public List<String> getAllLabels() {
    return Arrays.asList(wrapperMap.keySet().toArray());
  }

  public Collection<SignalSlot> getAllMethods(String label) {
    if (!wrapperMap.containsKey(label)) throw new IllegalArgumentException("A wrapper with label " + label + " is not available.");
    return wrapperMap.get(label).values();
  }

  @Override
  public SignalSlot getSignalSlot(String label, String signature) {
    if (!wrapperMap.containsKey(label))
      throw new IllegalArgumentException("No wrapper with name [" + label + "]");

    System.out.println("Requested signal or slot " + signature);

    SignalSlot ss = wrapperMap.get(label).get(signature);

    if (ss == null) {
      throw new IllegalArgumentException("No such signal or slot: [" + label + "." + signature + "]");
    }

    return ss;
  }

  public boolean isWrapperAvailable(String label){
    return wrapperMap.containsKey(label);
  }

}
