package minderengine;

import controllers.TestQueueController;
import controllers.TestRunContext;
import models.Adapter;
import utils.Util;
import models.*;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * This is the default implementation of the minder server that will run on the server side and
 * respond to Xoola remote requests.
 * Created by yerlibilgin on 02/12/14.
 */
@Singleton
public class MinderServer implements IMinderServer {

  @Inject
  TestQueueController testQueueController;

  /**
   * The client calls this method after a sucessfull handshake.
   *
   * @param adapterIdentifier
   * @param methodSet
   */
  @Override
  public void hello(AdapterIdentifier adapterIdentifier, Set<MethodContainer> methodSet) {
    MinderAdapterRegistry.get().updateAdapter(adapterIdentifier, methodSet);
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
      Adapter adapter = Adapter.findByName(adapterIdentifier.getName());
      if (adapter == null) {
        throw new IllegalArgumentException("Unidentified adapter [" + adapterIdentifier.getName() + "]");
      }


      long jobId = (Long)Util.readObject(((SignalCallData) signalData).args[0], null  );

      if (AbstractJob.findById(jobId) == null) {
        throw new IllegalArgumentException("A job with ID [" + jobId + "] was not found");
      }


      Logger.debug("Trigger job " + jobId);

      Visibility vis = (Visibility) Util.readObject(((SignalCallData) signalData).args[1], null);
      TestSession session = testQueueController.enqueueJobWithUser(jobId, adapter.user, null, vis);
      MinderSignalRegistry.get().initTestSession(session);
      return session;
    } else if (MinderSignalRegistry.get().hasSession(testSession)) {
      MinderSignalRegistry.get().enqueueSignal(testSession, adapterIdentifier, signature, signalData);
      //check if the test is suspended and enqueue it properly into the queue.
      if (ContextContainer.get().contains(testSession)) {
        TestRunContext testRunContext = ContextContainer.get().findAndPurge(testSession);
        testQueueController.enqueueTestRunContext(testRunContext);
      }
      return null;
    } else {
      throw new IllegalArgumentException("No MinderSignalRegistry object defined for session " + testSession);
    }
  }
}
