package minderengine;

import controllers.TestRunContext;

import java.util.HashMap;

/**
 * @author: yerlibilgin
 * @date: 07/03/16.
 */
public class ContextContainer {
  private static ContextContainer instance;

  public static ContextContainer get() {
    if (instance == null) {
      instance = new ContextContainer();
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

  public void addTestContext(TestSession session, TestRunContext context) {
    if (testContextMap.containsKey(session)) {
      throw new IllegalArgumentException("Key already exists in the map");
    }

    testContextMap.put(session, context);
  }


  public void remove(TestSession session) {
    testContextMap.remove(session);
  }

}
