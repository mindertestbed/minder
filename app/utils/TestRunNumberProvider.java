package utils;

import java.util.concurrent.atomic.AtomicInteger;
import models.TestRun;

/**
 * @author yerlibilgin
 */
public class TestRunNumberProvider {

  private static final AtomicInteger numberSource;

  static {
    numberSource = new AtomicInteger(TestRun.getMaxNumber());
  }


  public static int getNextNumber() {
    return numberSource.incrementAndGet();
  }
}
