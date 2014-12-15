package minderengine;

import builtin.Entry;
import mtdl.*;
import scala.actors.threadpool.Arrays;

import java.util.*;

/**
 * Uses two maps for mapping the wrappers for easy access.
 * The first map uses keys based on the UID of the wrappers
 *
 * The second map uses labels of the wrappers to map to the uids.
 *
 * If two wrappers exist having the same label, then their uid is used as the tie breaker.
 * The the first 3 characters of the uid is appended to the label.
 * Created by yerlibilgin on 13/12/14.
 */
public class MinderWrapperRegistry implements ISignalSlotInfoProvider {
  private static MinderWrapperRegistry instance;
  private HashMap<String, HashMap<String, SignalSlot>> wrapperUidMap;
  private HashMap<String, String> wrapperLabelUidMap;

  public static MinderWrapperRegistry get() {
    if (instance == null) {
      instance = new MinderWrapperRegistry();
    }
    return instance;
  }

  private MinderWrapperRegistry() {
    wrapperUidMap = new HashMap<>();
    wrapperLabelUidMap = new HashMap<>();
  }


  /**
   * Creates a new wrapper if it doesn't exist, updates an existing one
   *
   * @param uid
   * @param label
   * @param methodSet
   */
  public void updateWrapper(String uid, String label, Set<MethodContainer> methodSet) {
    //jpa uzerinden wrapper ile ilgili (uid primarykey) tablolari GUNCELLE
    //
    // iki hello arasinda bir degisiklik yoksa, dbye mudahale etmeye gerek yok
    // ama degisiklik varsa, yeni bir versiyon olusturulur,

    String newLabel = label;
    //MAPPING_LABEL:
    if (wrapperLabelUidMap.containsKey(label)) {
      //check if the uid of the existing wrapper matches this one, meaning that they are the same
      if (!uid.equals(wrapperLabelUidMap.get(label))) {
        //a wrapper with the same label already exists.
        //assign a new shiny name to this one.

        int l = uid.length();
        newLabel = label + "-" + uid.substring(0, l > 3 ? 3 : l);

        wrapperLabelUidMap.put(newLabel, uid);
      }
      //else no need to do anything. The methods were already mapped in the MAPPING_METHODS label.
    } else {
      wrapperLabelUidMap.put(label, uid);
    }

    System.out.println(newLabel);
    for (MethodContainer mc : methodSet) {
      //never use mc.method here because it is transient and we didn't receive it.
      System.out.println("\t" + (mc.isSignal ? "Signal " : "Slot ") + mc.methodKey);
    }

    //MAPPING_METHODS:
    HashMap<String, SignalSlot> methodMap = new HashMap<>();
    for (MethodContainer mc : methodSet) {
      SignalSlot ss;
      if (mc.isSignal) {
        ss = new SignalImpl(newLabel, mc.methodKey);
      } else {
        ss = new SlotImpl(newLabel, mc.methodKey);
      }
      methodMap.put(mc.methodKey, ss);
    }

    wrapperUidMap.put(uid, methodMap);

  }

  public List<String> getAllLabels() {
    return Arrays.asList(wrapperLabelUidMap.keySet().toArray());
  }

  public List<String> getAllUids() {
    return Arrays.asList(wrapperUidMap.keySet().toArray());
  }

  public Collection<SignalSlot> getMethodsFromUid(String uid) {
    if (!wrapperUidMap.containsKey(uid)) throw new IllegalArgumentException("No wrapper with uid " + uid + " exists.");
    return wrapperUidMap.get(uid).values();
  }

  public Collection<SignalSlot> getMethodsFromLabel(String label) {
    if (!wrapperLabelUidMap.containsKey(label))
      throw new IllegalArgumentException("No wrapper with name " + label + " exists.");
    String uid = wrapperLabelUidMap.get(label);
    return getMethodsFromUid(uid);
  }

  @Override
  public SignalSlot getSignalSlot(String wrapperId, String signature) {
    if (!wrapperLabelUidMap.containsKey(wrapperId))
      throw new IllegalArgumentException("No wrapper with name [" + wrapperId + "]");

    System.out.println("Requested signal or slot " + signature);

    String uid = wrapperLabelUidMap.get(wrapperId);

    if (!wrapperUidMap.containsKey(uid))
      throw new IllegalArgumentException("No wrapper with uid [" + uid + "]");

    SignalSlot ss = wrapperUidMap.get(uid).get(signature);

    if (ss == null){
      throw new IllegalArgumentException("No such signal or slot: [" + wrapperId + "." + signature + "]");
    }

    return ss;
  }

  public String getUidForLabel(String label) {
    if (!wrapperLabelUidMap.containsKey(label))
      throw new IllegalArgumentException("No wrapper with name " + label + " exists.");

    return wrapperLabelUidMap.get(label);
  }
}
