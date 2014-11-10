package minderengine;

import scala.actors.threadpool.AtomicInteger

/**
 * The metadata information that lives aside a wrapper.
 * Provides information about the location, size, icon, label and description
 * This information can be used to represent the wrapper in UI's.
 *
 * @author yerlibilgin
 *
 */
class UIMetadata {
  /**
   * The label of the owning wrapper
   */
  var label = "Wrapper " + IdFactory.next

  /**
   * the full URL of the icon.
   * default is /icons/wrapper.png
   */
  var iconUrl: String = "/icons/wrapper.png";

  /**
   * Extended information about the owning wrapper
   */
  var description: String = "";

  /**
   * X coortinate
   */
  var x: Int = 0;

  /**
   * Y coortinate of the representation
   */
  var y: Int = 0;

  /**
   * Width of the representation
   */
  var w: Int = 30;

  /**
   * Height of the representation
   */
  var h: Int = 30;

  /* GETTERS */
  def getLabel() = label
  def getIconUrl() = iconUrl
  def getDescription() = description
  def getX() = x
  def getY() = y
  def getW() = w
  def getH() = h

  /* SETTERS */
  def setDescription(description: String) = this.description = description
  def setLabel(label: String) = this.label = label;
  def setIconUrl(iconUrl: String) = this.iconUrl = iconUrl
  def setX(x: Int) = this.x = x
  def setY(y: Int) = this.y = y
  def setW(w: Int) = this.w = w
  def setH(h: Int) = this.h = h
}

object IdFactory{
  /**
   * Current integer value
   */
  private var _next : AtomicInteger = new AtomicInteger(0)
  
  /**
   * Static function to increment and get the id. But get comes first.
   * so the first returned value is going to be the initial value of _next
   */
  def next =_next.getAndIncrement().intValue;
}
