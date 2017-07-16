package rest.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 16/07/17.
 */
public class SuiteRunRequest {
  private long suiteId;
  private List<Long> jobs;

  public long getSuiteId() {
    return suiteId;
  }

  public void setSuiteId(long suiteId) {
    this.suiteId = suiteId;
  }

  public List<Long> getJobs() {
    return jobs;
  }

  public void setJobs(List<Long> jobs) {
    this.jobs = jobs;
  }
}
