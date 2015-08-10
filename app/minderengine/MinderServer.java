package minderengine;

import models.User;
import play.Logger;

import java.util.Set;

/**
 * This is the default implementation of the minder server that will run on the server side and
 * respond to Xoola remote requests.
 * Created by yerlibilgin on 02/12/14.
 */
public class MinderServer implements IMinderServer {

  /**
   * The client calls this method after a sucessfull handshake.
   *
   * @param identifier
   * Wrapper|Version
   * @param methodSet
   */
  @Override
  public void hello(String identifier, Set<MethodContainer> methodSet) {
    MinderWrapperRegistry.get().updateWrapper(identifier, methodSet);
  }

  /**
   * Provide information on the Test Designer who has a sessionId
   *
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
  public Object signalEmitted(String sessionId, String label, String signature, SignalData signalData) {
    Logger.debug("Signal emitted [" + sessionId + "." + label + "." + signature + "]");
    MinderSignalRegistry me = SessionMap.getObject(sessionId, "signalRegistry");
    if (me == null)
      throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + sessionId);

    me.enqueueSignal(label, signature, signalData);
    return null;
  }
}
