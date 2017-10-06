package utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import models.*;
import play.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.*;


/**
 * @author: yerlibilgin
 * @date: 03/03/16.
 */
public class ReportUtils {
  static String reportTemplate;
  static String testRunTemplate;
  static String singleTestRunTemplate;

  private static String checkString = "<img src='conf/Check-icon.png' height=\"20px\"/>";
  private static String crossString = "<img src='conf/Delete-icon.png' height=\"20px\"/>";
  private static String successColor = "rgb(185, 255, 204)";
  private static String failColor = "rgb(255, 160, 180)";

  static {
    byte[] buff = new byte[1024];
    int read = 0;

    try {


      FileInputStream stream = new FileInputStream("conf/reportTemplate.html");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      while ((read = stream.read(buff, 0, 1024)) > 0) {
        baos.write(buff, 0, read);
      }
      reportTemplate = new String(baos.toByteArray());
      stream.close();


      stream = new FileInputStream("conf/testRunTemplate.html");
      baos = new ByteArrayOutputStream();
      while ((read = stream.read(buff, 0, 1024)) > 0) {
        baos.write(buff, 0, read);
      }
      testRunTemplate = new String(baos.toByteArray());
      stream.close();


      stream = new FileInputStream("conf/singleTestRunTemplate.html");
      baos = new ByteArrayOutputStream();
      while ((read = stream.read(buff, 0, 1024)) > 0) {
        baos.write(buff, 0, read);
      }
      singleTestRunTemplate = new String(baos.toByteArray());
      stream.close();


    } catch (Exception ex) {
      Logger.debug(ex.getMessage(), ex);
    }
  }

  public static byte[] toPdf(TestRun tr) {
    try {
      String html = fillTestRun(tr, singleTestRunTemplate);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Document document = new Document();
      PdfWriter writer = PdfWriter.getInstance(document, baos);
      document.open();
      XMLWorkerHelper.getInstance().parseXHtml(writer, document, new ByteArrayInputStream(html.getBytes()));
      document.close();
      return baos.toByteArray();
    } catch (Exception ex) {
      ex.printStackTrace();
      return "Invalid".getBytes();
    }
  }

  private static String fillTestRun(TestRun tr, String targetString) {
    return fillTestRun(tr, targetString, null);
  }

  private static String fillTestRun(TestRun tr, String reportTemplate, Map<String, String> customParameters) {
    TestAssertion testAssertion = TestAssertion.findById(tr.job.tdl.testCase.testAssertion.id);
    boolean isSuccess = tr.status == TestRunStatus.SUCCESS;
    reportTemplate = reportTemplate
        .replace("${taID}", testAssertion.taId)
        .replace("${testGroup}", testAssertion.testGroup.name)
        .replace("${testCase}", tr.job.tdl.testCase.name)
        .replace("${user}", tr.runner.name)
        .replace("${date}", Util.formatDate(tr.date))
        .replace("${systemsTested}", getNonNullString(tr.sutNames))
        .replace("${resultCharacter}", isSuccess ? checkString : crossString)
        .replace("${target}", getNonNullString(testAssertion.target))
        .replace("${normativeSource}", getNonNullString(testAssertion.normativeSource))
        .replace("${prescriptionLevel}", getNonNullString(testAssertion.prescriptionLevel.name()))
        .replace("${prerequisite}", getNonNullString(testAssertion.prerequisites))
        .replace("${predicate}", getNonNullString(testAssertion.predicate))
        .replace("${variables}", getNonNullString(testAssertion.variables))
        .replace("${tag}", getNonNullString(testAssertion.tag))
        .replace("${result}", isSuccess ? "Successful" : getNonNullString(tr.errorMessage))
        .replace("${resultColor}", isSuccess ? successColor : failColor)
        .replace("${log}",
            getNonNullString(tr.history.extractSystemOutputLog()
            ).replace("\n\r", "<br/>")
                .replace("\n", "<br/>")
        );

    if (customParameters != null && customParameters.size() > 0) {
      for (Map.Entry<String, String> entry : customParameters.entrySet()) {
        reportTemplate = reportTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
      }
    }

    //check if we still have a parameter like ${param} and remove them
    reportTemplate.replaceAll("\\$\\{(\\w|_)+(\\w|\\d|_)*\\}", "");

    return reportTemplate;
  }

  private static String getNonNullString(byte[] errorMessage) {
    String s = errorMessage == null ? "" : new String(errorMessage);
    s = s.replace("<", "&lt;")
        .replace(">", "&gt;");

    return s;
  }

  private static String getNonNullString(String target) {
    String s = target == null ? "" : target;
    s = s.replace("<", "&lt;")
        .replace(">", "&gt;");
    return s;
  }


  static final Comparator<TestRun> comparator = new Comparator<TestRun>() {
    @Override
    public int compare(TestRun o1, TestRun o2) {
      return o1.job.tdl.testCase.testAssertion.taId.compareToIgnoreCase(o2.job.tdl.testCase.testAssertion.taId);
    }
  };

  public static byte[] toPdf(List<TestRun> testRuns, String subTitle) throws Exception {

    /**
     * Replacement
     */

    StringBuilder builder = new StringBuilder();
    StringBuilder tocBuilder = new StringBuilder();


    Collections.sort(testRuns, comparator);

    String testGroup = "";
    for (TestRun testRun : testRuns) {
      builder.append(fillTestRun(testRun, testRunTemplate)).append('\n');
      tocBuilder
          .append("<tr")
          .append(testRun.status == TestRunStatus.SUCCESS ? (" style='background-color:" + successColor + ";'") : (" style='background-color:" + failColor + "'"))
          .append("><td><a href='#")
          .append(testRun.job.tdl.testCase.testAssertion.taId)
          .append("'>")
          .append(testRun.job.tdl.testCase.testAssertion.taId)
          .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></td><td>")
          .append((testRun.status == TestRunStatus.SUCCESS ? checkString : crossString))
          .append("</td></tr>\n");
      testGroup = testRun.job.tdl.testCase.testAssertion.testGroup.name;
    }

    String html = reportTemplate
        .replace("${testRuns}", builder.toString())
        .replace("${toc}", tocBuilder.toString())
        .replace("${testGroup}", testGroup)
        .replace("${date}", Util.formatDate(new Date()))
        .replace("${subTitle}", subTitle);


    Document document = new Document();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PdfWriter writer = PdfWriter.getInstance(document, baos);
    document.open();
    XMLWorkerHelper.getInstance().parseXHtml(writer, document, new ByteArrayInputStream(html.getBytes()));
    document.close();
    return baos.toByteArray();
  }

  public static byte[] toPdf(long groupId, long selectedBatchReportId, long selectedSingleReportId, Collection<TestRun> testRuns, Map<String, String> customParameterMap) throws Exception {

    /**
     * Replacement
     */

    StringBuilder builder = new StringBuilder();
    StringBuilder tocBuilder = new StringBuilder();

    String testGroup = TestGroup.findById(groupId).name;
    String batchTemplateHTML = reportTemplate;
    if (selectedBatchReportId != -1) {
      ReportTemplate batchTemplate = ReportTemplate.byId(selectedBatchReportId);

      if (batchTemplate == null)
        throw new Exception("No such report with id [" + selectedBatchReportId + "]");

      batchTemplateHTML = new String(Util.gunzip(batchTemplate.html));
    }

    String singleTemplate = singleTestRunTemplate;
    if (selectedSingleReportId != -1) {
      ReportTemplate testRunTemplate = ReportTemplate.byId(selectedSingleReportId);
      if (testRunTemplate == null)
        throw new Exception("No such report with id [" + selectedSingleReportId + "]");
      singleTemplate = new String(Util.gunzip(testRunTemplate.html));
    }

    for (TestRun testRun : testRuns) {

      builder.append(fillTestRun(testRun, new String(singleTemplate), customParameterMap));

      tocBuilder
          .append("<tr")
          .append(testRun.status == TestRunStatus.SUCCESS ? (" style='background-color:" + successColor + ";'") : (" style='background-color:" + failColor + "'"))
          .append("><td><a href='#")
          .append(testRun.job.tdl.testCase.testAssertion.taId)
          .append("'>")
          .append(testRun.job.tdl.testCase.testAssertion.taId)
          .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></td><td>")
          .append((testRun.status == TestRunStatus.SUCCESS ? checkString : crossString))
          .append("</td></tr>\n");
    }


    batchTemplateHTML = batchTemplateHTML
        .replace("${testRuns}", builder.toString())
        .replace("${toc}", tocBuilder.toString())
        .replace("${testGroup}", testGroup)
        .replace("${date}", Util.formatDate(new Date()))
        .replace("${subTitle}", "");

    if (customParameterMap != null && customParameterMap.size() > 0) {
      for (Map.Entry<String, String> entry : customParameterMap.entrySet()) {
        batchTemplateHTML = batchTemplateHTML.replace("${" + entry.getKey() + "}", entry.getValue());
      }
    }

    //check if we still have a parameter like ${param} and remove them
    batchTemplateHTML.replaceAll("\\$\\{(\\w|_)+(\\w|\\d|_)*\\}", "");


    Document document = new Document();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PdfWriter writer = PdfWriter.getInstance(document, baos);
    document.open();
    XMLWorkerHelper.getInstance().parseXHtml(writer, document, new ByteArrayInputStream(batchTemplateHTML.getBytes()));
    document.close();
    return baos.toByteArray();
  }

}
