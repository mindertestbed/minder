package rest.models;

import java.util.Date;

/**
 * @author: yerlibilgin
 * @date: 16/07/17.
 */
public class TestRunStatusResponse {
  public long testRunId;

  public RunStatus status;

  public byte[] log;

  public Date startDate;

  public Date finishDate;
}
