package minderengine;

import models.User;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * This is the default implementation of the minder server that will run on the server side and
 * respond to Xoola remote requests.
 * Created by yerlibilgin on 02/12/14.
 */
public class MinderServer implements IMinderServer{

  /**
   * The client calls this method after a sucessfull handshake. We need to save the
   * method list into the database.
   * @param methodSet
   */
  @Override
  public void hello(String uid, String label, Set<MethodContainer> methodSet) {
    MinderWrapperRegistry.get().updateWrapper(uid, label, methodSet);
  }

  /**
   * Provide information on the Test Designer who has a sessionId
   * @param sessionId the http session id of the web owner.
   * @return
   */
  @Override
  public UserDTO getUserInfo(String sessionId) {
    User user = SessionMap.getObject(sessionId, "owner");
    if (user == null)
      throw new IllegalArgumentException("No owner defined for session " + sessionId);

    return new UserDTO(user.name, user.firstName, user.lastName, user.email);
  }

  @Override
  public Object signalEmitted(String sessionId, String uid, String signature, SignalData signalData) {
    System.out.println("Signal emitted " + sessionId + " " + uid + " " + signature);
    MinderSignalRegistry me = SessionMap.getObject(sessionId, "signalRegistry");
    if (me == null)
      throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + sessionId);

    me.enqueueSignal(uid, signature, signalData);
    return null;
  }
}
