package models;

/**
 * @author: Yerlibilgin
 * @date: 31/07/17.
 */
public enum TestRunStatus {
  PENDING("Pending"),
  IN_PROGRESS("In progress"),
  CANCELLED("Cancelled"),
  SUCCESS("Success"),
  FAILED("Failed");


  public final String label;

  TestRunStatus(String label) {
    this.label = label;
  }
}
