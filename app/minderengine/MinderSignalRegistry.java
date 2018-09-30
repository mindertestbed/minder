package minderengine;

import com.yerlibilgin.ValueChecker;
import gov.tubitak.xoola.core.XoolaProperty;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has instances per session. It holds the values of signal calls that have occurred on the adapters' side.
 * <p>
 * Created by yerlibilgin on 02/12/14.
 */
public class MinderSignalRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(MinderSignalRegistry.class);
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
   */
  public void enqueueSignal(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature, SignalData signalData) {
    SignalQueue signalQueue = findRelatedQueue(testSession, adapterIdentifier, signature);
    LOGGER.debug("Found signal queue for " + testSession + " to enque " + signature);
    signalQueue.offer(signalData);
    LOGGER.debug("New size " + signalQueue.hashCode() + ": " + signalQueue.size());
  }

  /**
   * If the signal is not yet emitted, we still have to settle down
   * and wait on a queue. That is why, we have to call findRelatedQueue method here too
   *
   * @param timeout
   *     maximum timeout to wait for the deque operation. 0 means get the default (XoolaProperty.NETWORK_RESPONSE_TIMEOUT)
   *     from app.conf.
   */
  public SignalData dequeueSignal(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature, long timeout) {
    SignalQueue signalQueue = findRelatedQueue(testSession, adapterIdentifier, signature);

    if (timeout == 0) {

      timeout = (Integer) XoolaServer.properties.get(XoolaProperty.NETWORK_RESPONSE_TIMEOUT);
    }

    try {
      SignalData result = signalQueue.poll(timeout, TimeUnit.MILLISECONDS);
      if (result == null) {
        throw new IllegalStateException("Signal Timeout Expired (" + timeout + ")");
      }

      return result;
    } catch (InterruptedException e) {
      throw new IllegalStateException("Signal dequeue operation cancelled");
    }
  }

  /**
   * Try to find a signal for the given test session, adapter and related method signature.
   * If a signal has been queued, then deuque it immediately. Otherwise, do not wait and return null.
   *
   * @param testSession
   *     not null
   * @param adapterIdentifier
   *     not null
   * @param signature
   *     not null
   * @return the enqueued signal or null
   */
  public SignalData dequeueSignalImmediately(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature) {
    ValueChecker.notNull(testSession, "testSession");
    ValueChecker.notNull(adapterIdentifier, "adapterIdentifier");
    ValueChecker.notNull(signature, "signature");

    LOGGER.debug("Check signal for " + testSession.getSession());
    if (!testSessionMap.containsKey(testSession)) {
      LOGGER.warn(testSession.getSession() + " is not in the map");
    }
    SignalQueue signalQueue = findRelatedQueue(testSession, adapterIdentifier, signature);
    LOGGER.debug("Is signal queue empty? " + signalQueue.isEmpty());

    LOGGER.debug("signal queue size " + signalQueue.hashCode() + ": " + signalQueue.size());

    if (!signalQueue.isEmpty()) {
      return signalQueue.poll();
    } else {
      return null;
    }
  }

  /**
   * Adding an incoming signal into the related queue.
   */
  private SignalQueue findRelatedQueue(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature) {
    AdapterSignalMap adapterMap;
    if (!testSessionMap.containsKey(testSession)) {
      LOGGER.debug("No session for " + testSession.getSession() + ". Create one");
      adapterMap = new AdapterSignalMap();
      testSessionMap.put(testSession, adapterMap);
    } else {
      adapterMap = testSessionMap.get(testSession);
    }

    SignalMap signalMap;
    if (!adapterMap.containsKey(adapterIdentifier)) {
      LOGGER.debug("No signal map for " + adapterIdentifier + ". Create one");
      signalMap = new SignalMap();
      adapterMap.put(adapterIdentifier, signalMap);
    } else {
      signalMap = adapterMap.get(adapterIdentifier);
    }

    SignalQueue signalQueue;
    if (!signalMap.containsKey(signature)) {
      LOGGER.debug("No signal queue for " + signature + ". Create one");
      signalQueue = new SignalQueue();
      signalMap.put(signature, signalQueue);
    } else {
      signalQueue = signalMap.get(signature);
    }
    return signalQueue;
  }

  /**
   * Initialize the necessary data structures for a test session
   */
  public void initTestSession(TestSession testSession) {
    LOGGER.debug("Initialize test session for " + testSession.getSession());
    testSessionMap.put(testSession, new AdapterSignalMap());
  }

  /**
   * Remove the map for that specific test session
   */
  public void purgeTestSession(TestSession testSession) {
    testSessionMap.remove(testSession);
  }

  public boolean hasSession(TestSession testSession) {
    return testSessionMap.containsKey(testSession);
  }

}
