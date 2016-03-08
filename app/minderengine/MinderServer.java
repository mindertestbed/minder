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
   * @param adapterIdentifier
   * @param methodSet
   */
  @Override
  public void hello(AdapterIdentifier adapterIdentifier, Set<MethodContainer> methodSet) {
    MinderWrapperRegistry.get().updateWrapper(adapterIdentifier, methodSet);
  }


  /**
   * Provide information on the Test Designer who has a sessionId
   *
   * @param testSession the session id of the web owner.
   * @return
   */
  @Override
  public UserDTO getUserInfo(TestSession testSession) {
    User user = HttpSession.getObject(testSession.getSession(), "owner");
    if (user == null)
      throw new IllegalArgumentException("No owner defined for session " + testSession);

    return new UserDTO(user.name, user.name, null, user.email);
  }

  @Override
  public Object signalEmitted(TestSession testSession, AdapterIdentifier adapterIdentifier, String signature, SignalData signalData) {
    Logger.debug("Signal emitted [" + testSession + "." + adapterIdentifier.getName() + "." + signature + "]");

    MinderSignalRegistry me = HttpSession.getObject(testSession.getSession(), "signalRegistry");
    if (me == null)
      throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + testSession);

    me.enqueueSignal(testSession, adapterIdentifier, signature, signalData);
    return null;
  }
}
