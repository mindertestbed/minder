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
  public void hello(String uid, Set<MethodContainer> methodSet) {
    //jpa uzerinden wrapper ile ilgili (uid primarykey) tablolari GUNCELLE
    //
    // iki hello arasinda bir degisiklik yoksa, dbye mudahale etmeye gerek yok
    // ama degisiklik varsa, yeni bir versiyon olusturulur,

    System.out.println(uid);
    for (MethodContainer mc : methodSet) {
      //never use mc.method here because it is transient and we didn't receive it.
      System.out.println("\t" + (mc.isSignal ? "Signal " : "Slot ") + mc.methodKey);
    }
  }

  /**
   * Provide information on the Test Designer who has a sessionId
   * @param sessionId the http session id of the web user.
   * @return
   */
  @Override
  public UserDTO getUserInfo(String sessionId) {
    User user = SessionMap.getObject(sessionId, "user");
    if (user == null)
      throw new IllegalArgumentException("No user defined for session " + sessionId);

    return new UserDTO(user.name, user.firstName, user.lastName, user.email);
  }

  @Override
  public Object signalEmitted(String sessionId, String uid, String signature, SignalData signalData) {
    MinderSignalRegistry me = SessionMap.getObject(sessionId, "minderSignalRegistry");
    if (me == null)
      throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + sessionId);

    me.enqueueSignal(uid, signature, signalData);
    return null;
  }
}
