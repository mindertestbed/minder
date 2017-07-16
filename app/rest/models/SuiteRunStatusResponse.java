package rest.models;

import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 16/07/17.
 */
public class SuiteRunStatusResponse {
  public long suiteRunId;

  public List<TestRunStatusResponse> suiteRunStates;
}
