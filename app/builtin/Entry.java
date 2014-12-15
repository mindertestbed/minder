package builtin;

import java.util.Map;

/**
 * Created by yerlibilgin on 12/12/14.
 */
public class Entry implements Map.Entry<String, String> {

  private final String key;
  private String value;

  public Entry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String setValue(String value) {
    String old = this.value;
    this.value = value;
    return old;
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }
}
