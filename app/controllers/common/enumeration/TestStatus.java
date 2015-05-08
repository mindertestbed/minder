package controllers.common.enumeration;

/**
 * Created by yerlibilgin on 05/05/15.
 */
public enum TestStatus {
  PENDING("Pending"), RUNNING("Running"), BAD("Bad"), GOOD("Good");

  public final String description;

  TestStatus(String description) {
    this.description = description;
  }
}