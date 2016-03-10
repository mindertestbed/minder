package minderengine;

import models.User;

import java.util.HashMap;

/**
 * This class maps adapters to the signals that they produced
 *
 * @author: yerlibilgin
 * @date: 09/03/16.
 */
public class AdapterSignalMap extends HashMap<AdapterIdentifier, SignalMap> {
  private User owner;

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }
}
