package models;

import com.avaje.ebean.annotation.EnumValue;

/**
 * @author: yerlibilgin
 * @date: 29/08/15.
 */
public enum Role {
  @EnumValue("0")
  TEST_DESIGNER("Test Designer"),
  @EnumValue("1")
  TEST_DEVELOPER("Test Designer"),
  @EnumValue("2")
  TEST_OBSERVER("Test Designer");

  public final String description;

  Role(String description) {
    this.description = description;

  }
}
