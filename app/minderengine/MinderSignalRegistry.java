package minderengine;

import minderengine.SignalData;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class has instances per session. It holds the values of signal calls that have occurred on the wrappers' side.
 *
 * Created by yerlibilgin on 02/12/14.
 */
public class MinderSignalRegistry {
  private HashMap<String, HashMap<String, PriorityBlockingQueue<SignalData>>> wrapperMap = new HashMap<String, HashMap<String, PriorityBlockingQueue<SignalData>>>();

  /**
   * A signal was emitted on the wrapper side. We should put it into the queue until it gets taken by a rivet.
   * @param wrapperId
   * @param signature
   * @param signalData
   */
  public void enqueueSignal(String wrapperId, String signature, SignalData signalData) {
    PriorityBlockingQueue<SignalData> queue = initMap(wrapperId, signature);
    queue.offer(signalData);
  }

  /**
   * If the signal is not yet emitted, we still have to settle down
   * and wait on a queue. That is why, we have to call init-map method here too
   * @param wrapperId
   * @param signature
   * @return
   */
  public SignalData dequeueSignal(String wrapperId, String signature){
    PriorityBlockingQueue<SignalData> queue = initMap(wrapperId, signature);

    try {
      return queue.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private PriorityBlockingQueue<SignalData> initMap(String wrapperId, String signature) {
    HashMap<String, PriorityBlockingQueue<SignalData>> signalMap = null;
    if (!wrapperMap.containsKey(wrapperId)){
      signalMap = new HashMap<String, PriorityBlockingQueue<SignalData>>();
      wrapperMap.put(wrapperId, signalMap);
    } else{
      signalMap = wrapperMap.get(wrapperId);
    }

    PriorityBlockingQueue<SignalData> queue = null;
    if (!signalMap.containsKey(signature)){
      queue = new PriorityBlockingQueue<SignalData>();
      signalMap.put(signature, queue);
    } else{
      queue = signalMap.get(signature);
    }
    return queue;
  }
}
