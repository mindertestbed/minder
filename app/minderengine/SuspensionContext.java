package minderengine;

import controllers.TestRunContext;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: yerlibilgin
 * @date: 07/03/16.
 */
public class SuspensionContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(SuspensionContext.class);
  private static SuspensionContext instance;

  public static SuspensionContext get() {
    if (instance == null) {
      instance = new SuspensionContext();
    }

    return instance;
  }


  public HashMap<TestSession, TestRunContext> getTestContextMap() {
    return testContextMap;
  }

  private final HashMap<TestSession, TestRunContext> testContextMap = new LinkedHashMap<>();


  /**
   * Resolves and removes the value from the map
   */
  public TestRunContext findAndPurge(TestSession session) {
    try {
      return testContextMap.get(session);
    } finally {
      remove(session);
    }
  }


  public TestRunContext getContext(TestSession session) {
    return testContextMap.get(session);
  }

  public boolean contains(TestSession session) {
    return testContextMap.containsKey(session);
  }

  public void addTestContext(TestRunContext context) {
    if (testContextMap.containsKey(context.session())) {
      throw new IllegalArgumentException("Key already exists in the map");
    }

    testContextMap.put(context.session(), context);
  }


  public void remove(TestSession session) {
    testContextMap.remove(session);
  }


  public boolean removeTestRunByNumber(int number) {
    LOGGER.debug("Try to remove test run with number " + number + " from the suspension context");

    TestSession targetKey = null;

    for (TestSession key : testContextMap.keySet()) {
      int targetNumber = testContextMap.get(key).testRun().number;

      if (targetNumber == number) {
        targetKey = key;

        break;
      }
    }

    if (targetKey != null) {
      LOGGER.debug("Test run number " + number + " corresponds to " + targetKey.getSession() + ". Removing it from the suspension queue");
      testContextMap.remove(targetKey);
      return true;
    }

    LOGGER.debug("Test run number " + number + " not found in the suspension queue");
    return false;
  }

}
