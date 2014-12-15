package builtin;

/**
 * Created by yerlibilgin on 11/12/14.
 */
public class Result {
  public final boolean success;
  private final String message;

  public Result(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public Result() {
    this.success = true;
    this.message = "";
  }

  /*
    Our fields are public final fields, but still we might need getters, for some reflection users
    who require getters to access member.
   */

  /**
   * gets the boolean result
   *
   * @return
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * gets the result message
   *
   * @return
   */
  public String getMessage() {
    return message;
  }
}
