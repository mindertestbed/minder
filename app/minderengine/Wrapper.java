package minderengine;

import java.util.List;

/**
 * Base class for all wrappers. Using this class, any module, built-in function
 * will be represented as an active object belonging to the minder environment.
 * 
 * This class was deliberately written in java because it will be provided to TD's
 * who are not required to know scala
 * @author yerlibilgin
 *
 */
public abstract class Wrapper {
  private UIMetadata uiMetadata;

  public Wrapper(){
    this.uiMetadata = new UIMetadata();
  }
  
  /**
   * Override if you really have a wrapped object that needs to be started up before getting used
   */
  public void start(){
    
  }
  
  /**
   * Override if you have a wrapper that must be closed before the test finishes
   */
  public void stop(){
    
  }
 
  /**
   * Returns the list of signals (asynhronous calls, triggered by this wrapper object)
   * @return
   */
  public abstract List<Signal> listSignals();

  /**
   * Returns the list of slots (services provided by) this wrapped object
   * @return
   */
  public abstract List<Slot> listSlots();
  
  /**
   * A shortcut, convenience method for restarting the wrapped object. Defaults to
   * stop, start. Might be changed.
   */
  public void restart(){
    stop();
    start();
  }

  /**
   * Returns the information about the design time UI.
   * @return
   */
  public UIMetadata getMetadata(){
    return uiMetadata;
  }
}
