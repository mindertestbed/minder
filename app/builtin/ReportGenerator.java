package builtin;

import builtin.reportEngine.ReportManager;
import minderengine.MinderException;
import minderengine.Slot;
import models.Job;
import models.TestAssertion;
import models.TestCase;
import models.TestGroup;

import java.util.HashMap;

public class ReportGenerator extends BuiltInWrapper {

  private boolean isRunning = false;
  private ReportManager rmg;
  private HashMap<String, String> currentMap;

  @Override
  public void startTest() {
    isRunning = true;
    rmg = new ReportManager();
    currentMap = new HashMap<>();

  }

  @Override
  public void finishTest() {
    isRunning = false;
    currentMap = null;
  }

  @Override
  public void reportErrorForSignal(String s, String s1) {

  }

  @Override
  public String getShortDescription() {
    return "The minder built-in xml report generator wrapper";
  }

  @Slot
  public void setReportTemplate(byte[] template) {
    if (!isRunning)
      throw new MinderException(MinderException.E_SUT_NOT_RUNNING);

    System.out.println("Template: " + new String(template));
    rmg.setReportTemplate(template);

  }

  @Slot
  public void setReportAuthor(String author, String email) {
    if (!isRunning)
      throw new MinderException(MinderException.E_SUT_NOT_RUNNING);
    rmg.setReportAuthor(author, email);
  }

  @Slot
  public void setTestDetails(TestGroup group, TestAssertion ta, TestCase testCase, Job job, java.util.Set<String> wrappers, String log) {
    if (!isRunning)
      throw new MinderException(MinderException.E_SUT_NOT_RUNNING);
    rmg.report.getReportModel().getHeader().put("Test Group Name:", group.name);
    rmg.report.getReportModel().getHeader().put("Test Assertion Id", ta.taId);
    rmg.report.getReportModel().getHeader().put("Test Case Name", testCase.name);
    rmg.report.getReportModel().getHeader().put("Job", job.name);


    updateField("Normative Source", ta.normativeSource);
    updateField("Short Description", ta.shortDescription);
    updateField("Prerequisites", ta.prerequisites);
    updateField("Target", ta.target);
    updateField("Predicate", ta.predicate);

    int i = 1;

    System.out.println("NULL " + (wrappers == null));
    if (wrappers!=null) {
      for (String wrapper : wrappers) {
        updateField("Wrapper " + i, wrapper);
        ++i;
      }
    }

    updateField("Log", log);
  }

  @Slot
  public void setReportTitle(String title) {
    if (!isRunning)
      throw new MinderException(MinderException.E_SUT_NOT_RUNNING);
    rmg.setReportTitle(title);

  }

  @Slot
  public void setReportData(HashMap<String, String> data) {
    if (!isRunning)
      throw new MinderException(MinderException.E_SUT_NOT_RUNNING);
    rmg.setReportData(data);
  }

  @Slot
  public void updateField(String name, String value) {
    currentMap.put(name, value);
  }
  @Slot
  public byte[] generateReport() {
    rmg.setReportData(currentMap);
    return rmg.generateReport();
  }
}
