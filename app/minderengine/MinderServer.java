package minderengine;

import controllers.TestQueueController;
import controllers.TestRunContext;
import models.*;
import models.Wrapper;
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

    if (signature.equals("trigger(java.lang.Long,minderengine.Visibility)")) {
      Logger.debug("Received trigger signal");
      //this is a trigger signal
      models.Wrapper wrapper = Wrapper.findByName(adapterIdentifier.getName());
      if (wrapper == null) {
        throw new IllegalArgumentException("Unidentified adapter [" + adapterIdentifier.getName() + "]");
      }

      long jobId = (long) ((SignalCallData) signalData).args[0];

      if (AbstractJob.findById(jobId) == null) {
        throw new IllegalArgumentException("A job with ID [" + jobId + "] was not found");
      }


      Logger.debug("Trigger job " + jobId);

      Visibility vis = (Visibility) ((SignalCallData) signalData).args[1];
      System.out.println(vis);
      TestSession session = TestQueueController.enqueueJobWithUser(jobId, wrapper.user, null, vis);
      MinderSignalRegistry.get().initTestSession(session);
      return session;
    } else if (MinderSignalRegistry.get().hasSession(testSession)) {
      MinderSignalRegistry.get().enqueueSignal(testSession, adapterIdentifier, signature, signalData);


      if (ContextContainer.get().contains(testSession)) {
        TestRunContext testRunContext = ContextContainer.get().findAndPurge(testSession);
        TestQueueController.enqueueTestRunContext(testRunContext);
      }
      return null;
    } else {
      throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + testSession);
    }
  }
}
