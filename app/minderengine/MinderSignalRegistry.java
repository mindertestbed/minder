package minderengine;

import org.interop.xoola.core.XoolaProperty;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * This class has instances per session. It holds the values of signal calls that have occurred on the adapters' side.
 * <p>
 * Created by yerlibilgin on 02/12/14.
 */
public class MinderSignalRegistry {

  private static MinderSignalRegistry instance;

  public static MinderSignalRegistry get() {
    if (instance == null) {
      instance = new MinderSignalRegistry();
    }

    return instance;
  }

  /**
   * This structure maps the test sessions to the adapters-signals queues
   */
  private HashMap<TestSession, AdapterSignalMap> testSessionMap = new HashMap<TestSession, AdapterSignalMap>();

  /**
   * A signal was emitted on the adapter side. We should put it into the queue until it gets taken by a rivet.
   *
   * @param adapterIdentifier
   * @param signature
   * @param signalData
   */
  public void enqueueSignal(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature, SignalData signalData) {
    SignalQueue signalQueue = findRelatedQueue(testSession, adapterIdentifier, signature);
    signalQueue.offer(signalData);
  }

  /**
   * If the signal is not yet emitted, we still have to settle down
   * and wait on a queue. That is why, we have to call findRelatedQueue method here too
   *
   * @param adapterIdentifier
   * @param signature
   * @param timeout           maximum timeout to wait for the deque operation. 0 means get the default (XoolaProperty.NETWORK_RESPONSE_TIMEOUT)
   *                          from app.conf.
   * @return
   */
  public SignalData dequeueSignal(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature, long timeout) {
    SignalQueue signalQueue = findRelatedQueue(testSession, adapterIdentifier, signature);

    if (timeout == 0)
      timeout = Long.parseLong(XoolaServer.properties.getProperty(XoolaProperty.NETWORK_RESPONSE_TIMEOUT));

    try {
      SignalData result = signalQueue.poll(timeout, TimeUnit.MILLISECONDS);
      if (result == null) {
        throw new RuntimeException("Signal Timeout Expired (" + timeout + ")");
      }

      return result;
    } catch (InterruptedException e) {
      throw new RuntimeException("Signal dequeue operation cancelled");
    }
  }

  /*
  * Adding an incoming signal into the related queue.
  * */
  private SignalQueue findRelatedQueue(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature) {
    AdapterSignalMap adapterMap;
    if (!testSessionMap.containsKey(testSession)) {
      adapterMap = new AdapterSignalMap();
      testSessionMap.put(testSession, adapterMap);
    } else {
      adapterMap = testSessionMap.get(testSession);
    }

    SignalMap signalMap;
    if (!adapterMap.containsKey(adapterIdentifier)) {
      signalMap = new SignalMap();
      adapterMap.put(adapterIdentifier, signalMap);
    } else {
      signalMap = adapterMap.get(adapterIdentifier);
    }

    SignalQueue signalQueue;
    if (!signalMap.containsKey(signature)) {
      signalQueue = new SignalQueue();
      signalMap.put(signature, signalQueue);
    } else {
      signalQueue = signalMap.get(signature);
    }
    return signalQueue;
  }

  /**
   * Initialize the necessary data structures for a test session
   *
   * @param testSession
   */
  public void initTestSession(TestSession testSession) {
      testSessionMap.put(testSession, new AdapterSignalMap());
  }

  /**
   * Remove the map for that specific test session
   *
   * @param testSession
   */
  public void purgeTestSession(TestSession testSession) {
    testSessionMap.remove(testSession);
  }

  public boolean hasSession(TestSession testSession) {
    return testSessionMap.containsKey(testSession);
  }

}
