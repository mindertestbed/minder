package builtin;

import minderengine.*;
import models.User;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by yerlibilgin on 11/12/14.
 */
public abstract class BuiltInWrapper extends Wrapper implements IMinderClient, ISignalHandler {

  private final HashMap<String, MethodContainer> slots = new HashMap<>();
  private TestSession session;
  private SUTIdentifier identifier = new SUTIdentifier();

  public BuiltInWrapper() {
    identifier.setSutName("");
    //lets resolve methods first
    for (Method method : this.getClass().getMethods()) {
      if (method.isAnnotationPresent(Slot.class)) {
        MethodContainer mc = new MethodContainer(method);
        this.slots.put(mc.methodKey, mc);
      }
    }
  }

  @Override
  public Object callSlot(TestSession session, String slotName, Object[] args) {
    if (slots.containsKey(slotName)) {
      try {
        return slots.get(slotName).method.invoke(this, args);
      } catch (Exception e) {
        throw new IllegalStateException(e.getMessage(), e);
      }
    }
    throw new IllegalArgumentException("Slot named [" + slotName + "] not found on [" + this.getShortDescription() + "]");
  }

  @Override
  public void startTest(StartTestObject startTestObject) {
    this.session = startTestObject.getSession();
  }

  @Override
  public UserDTO getCurrentTestUserInfo() {
    if (session == null)
      throw new IllegalArgumentException("No active owner session found");

    User user = HttpSession.getObject(session.getSession(), "owner");
    if (user == null) {
      throw new IllegalArgumentException("No owner defined for session " + session);
    }

    return new UserDTO(user.name, user.name, null, user.email);
  }

  @Override
  public void finishTest() {
    session = null;
  }

  /**
   * Built in wrappers won't fire signals. Instead, their results will be got through slot calls.
   *
   * @param obj
   * @param signalMethod
   * @param args
   * @return
   */
  @Override
  public Object handleSignal(Object obj, Method signalMethod, Object[] args) {
    return null;
  }

  /**
   * A ready to use method that creates a successful result
   *
   * @return
   */
  public static final Result success() {
    return new Result();
  }

  /**
   * A ready to use method that creates a failure result
   *
   * @return
   */
  public static final Result failure(String message) {
    return new Result(false, message);
  }

  public abstract String getShortDescription();

  @Override
  public SUTIdentifiers getSUTIdentifiers() {
    return null;
  }
}
