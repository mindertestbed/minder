package global;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import models.TestAssertion;
import models.TestRun;
import play.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;


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
    TestAssertion testAssertion = TestAssertion.findById(tr.job.tdl.testCase.testAssertion.id);
    return targetString
        .replace("${taID}", testAssertion.taId)
        .replace("${testGroup}", testAssertion.testGroup.name)
        .replace("${testCase}", tr.job.tdl.testCase.name)
        .replace("${user}", tr.runner.email)
        .replace("${date}", Util.formatDate(tr.date))
        .replace("${systemsTested}", getNonNullString(tr.sutNames))
        .replace("${resultCharacter}", tr.success ? checkString : crossString)
        .replace("${target}", getNonNullString(testAssertion.target))
        .replace("${normativeSource}", getNonNullString(testAssertion.normativeSource))
        .replace("${prescriptionLevel}", getNonNullString(testAssertion.prescriptionLevel.name()))
        .replace("${prerequisite}", getNonNullString(testAssertion.prerequisites))
        .replace("${predicate}", getNonNullString(testAssertion.predicate))
        .replace("${variables}", getNonNullString(testAssertion.variables))
        .replace("${tag}", getNonNullString(testAssertion.tag))
        .replace("${result}", tr.success ? "Successful" : getNonNullString(tr.errorMessage))
        .replace("${resultColor}", tr.success ? successColor : failColor)
        .replace("${log}",
            getNonNullString(tr.history.extractSystemOutputLog()
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n\r", "<br/>")
                .replace("\n", "<br/>"))
        );
  }

  private static String getNonNullString(byte[] errorMessage) {
    return errorMessage == null ? "" : new String(errorMessage);
  }

  private static String getNonNullString(String target) {
    return target == null ? "" : target;
  }


  public static byte[] toPdf(List<TestRun> testRuns, String subTitle) throws Exception {

    /**
     * Replacement
     */

    StringBuilder builder = new StringBuilder();
    StringBuilder tocBuilder = new StringBuilder();

    String testGroup = "";
    for (TestRun testRun : testRuns) {
      builder.append(fillTestRun(testRun, testRunTemplate)).append('\n');
      tocBuilder
          .append("<tr")
          .append(testRun.success ? (" style='background-color:" + successColor + ";'") : (" style='background-color:" + failColor + "'"))
          .append("><td><a href='#")
          .append(testRun.job.tdl.testCase.testAssertion.taId)
          .append("'>")
          .append(testRun.job.tdl.testCase.testAssertion.taId)
          .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></td><td>")
          .append((testRun.success ? checkString : crossString))
          .append("</td></tr>\n");
      testGroup = testRun.job.tdl.testCase.testAssertion.testGroup.name;
    }

    String html = reportTemplate
        .replace("${testRuns}", builder.toString())
        .replace("${toc}", tocBuilder.toString())
        .replace("${testGroup}", testGroup)
        .replace("${date}", Util.formatDate(new Date()))
        .replace("${subTitle}", subTitle);


    Logger.debug(html);
    Document document = new Document();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PdfWriter writer = PdfWriter.getInstance(document, baos);
    document.open();
    XMLWorkerHelper.getInstance().parseXHtml(writer, document, new ByteArrayInputStream(html.getBytes()));
    document.close();
    return baos.toByteArray();
  }

}
