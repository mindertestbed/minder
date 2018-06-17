package minderengine;

import controllers.TestRunContext;

import java.util.HashMap;

/**
 *
 * @author: yerlibilgin
 * @date: 07/03/16.
 */
public class SuspensionContext {
  private static SuspensionContext instance;

  public static SuspensionContext get() {
    if (instance == null) {
      instance = new SuspensionContext();
    }

    return instance;
  }


  private static final HashMap<TestSession, TestRunContext> testContextMap = new HashMap<>();


  /**
   * Resolves and removes the value from the map
   *
   * @param session
   * @return
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

}
