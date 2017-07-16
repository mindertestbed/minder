package rest.models;

import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 16/07/17.
 */
public class SuiteRunResponse {
  public long suiteId;
  public long suiteRunId;
  public List<Long> testRuns;
}
