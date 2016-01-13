package models;

/**
 * @author: yerlibilgin
 * @date: 11/01/16.
 */
public enum Visibility {
  /**
   * Everyone registered user can see
   */
  PUBLIC,
  /**
   * Only users of the same role can see
   */
  PROTECTED,
  /**
   * Only the owner can see
   */
  PRIVATE
}
