package minderengine;

import java.util.HashMap;

/**
 * Created by yerlibilgin on 03/12/14.
 */
public class SessionMap {

  private static final HashMap<String, HashMap<String, Object>> sessionMap = new HashMap<>();

  public static void registerObject(String session, String key, Object o) {
    HashMap<String, Object> objectMap;
    if (sessionMap.containsKey(session)) {
      objectMap = sessionMap.get(session);
    } else {
      objectMap = new HashMap<>();
      sessionMap.put(session, objectMap);
    }

    objectMap.put(key, o);
  }

  public static <T> T getObject(String sessionId, String key) {
    if (sessionMap.containsKey(sessionId)) {
      HashMap<String, Object> objectMap = sessionMap.get(sessionId);
      if (objectMap.containsKey(key)) {
        return (T) objectMap.get(key);
      }
      throw new IllegalArgumentException("No object with key=[" + key + "] was found in the session [" + sessionId + "]");
    }
    throw new IllegalArgumentException("No session with key=[" + sessionId + "] was found");
  }
}
