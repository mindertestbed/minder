package utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFiles;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.yerlibilgin.BinaryUtil;
import com.yerlibilgin.XMLUtils;
import com.yerlibilgin.XPathUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import models.ReportTemplate;
import models.TestAssertion;
import models.TestGroup;
import models.TestRun;
import models.TestRunStatus;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import play.Logger;
import play.Logger.ALogger;


/**
 * @author: yerlibilgin
 * @date: 03/03/16.
 */
public class ReportUtils {

  private static final ALogger LOGGER = Logger.of(ReportUtils.class);
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
    LOGGER.debug("Create PDF from test run " + tr.id);
    try {

      String template;
      if (tr.job.reportTemplate != null) {
        template = new String(Util.gunzip(tr.job.reportTemplate.html));
      } else {
        template = singleTestRunTemplate;
      }
      String html = fillTestRun(tr, template);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Document document = new Document();
      PdfWriter writer = PdfWriter.getInstance(document, baos);
      document.open();
      parseXHtml(writer, document, new ByteArrayInputStream(html.getBytes()));
      document.close();
      return baos.toByteArray();
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      return "Invalid".getBytes();
    }
  }

  private static String fillTestRun(TestRun tr, String targetString) {
    return fillTestRun(tr, targetString, null);
  }

  private static String fillTestRun(TestRun tr, String reportTemplate, Map<String, String> customParameters) {
    LOGGER.debug("Fill the report template with the test run " + +tr.id + " details.");
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

    //get all the metadata elements, and replace them in the report
    if (tr.reportMetadata != null && tr.reportMetadata.length != 0) {

      try {
        final org.w3c.dom.Document document = XMLUtils.parse(new GZIPInputStream(new ByteArrayInputStream(tr.reportMetadata)));

        final List<Node> nodes = XPathUtils.listNodes(document, "//metadata");

        for (Node node : nodes) {
          Element element = (Element) node;

          String name = element.getAttribute("name");
          String value = new String(BinaryUtil.base642b(element.getTextContent()), StandardCharsets.UTF_8);

          reportTemplate = reportTemplate.replace("${" + name + "}", value);
        }

      } catch (IOException e) {
        throw new IllegalStateException(e);
      }

    }

    //check if we still have a parameter like ${param} and remove them
    reportTemplate.replaceAll("\\$\\{(\\w|_)+(\\w|\\d|_)*\\}", "");

    System.out.println(reportTemplate);

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


  static final Comparator<TestRun> comparator = (o1, o2) ->
      o1.job.tdl.testCase.testAssertion.taId.compareToIgnoreCase(o2.job.tdl.testCase.testAssertion.taId);

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
          .append(testRun.status == TestRunStatus.SUCCESS ? (" style='background-color:" + successColor + ";'")
              : (" style='background-color:" + failColor + "'"))
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
    parseXHtml(writer, document, new ByteArrayInputStream(html.getBytes()));
    document.close();
    return baos.toByteArray();
  }

  public static byte[] toPdf(long groupId, long selectedBatchReportId, long selectedSingleReportId, Collection<TestRun> testRuns,
      Map<String, String> customParameterMap) throws Exception {

    /**
     * Replacement
     */

    StringBuilder builder = new StringBuilder();
    StringBuilder tocBuilder = new StringBuilder();

    String testGroup = TestGroup.findById(groupId).name;
    String batchTemplateHTML = reportTemplate;
    if (selectedBatchReportId != -1) {
      ReportTemplate batchTemplate = ReportTemplate.byId(selectedBatchReportId);

      if (batchTemplate == null) {
        throw new Exception("No such report with id [" + selectedBatchReportId + "]");
      }

      batchTemplateHTML = new String(Util.gunzip(batchTemplate.html));
    }

    String singleTemplate = singleTestRunTemplate;
    if (selectedSingleReportId != -1) {
      ReportTemplate testRunTemplate = ReportTemplate.byId(selectedSingleReportId);
      if (testRunTemplate == null) {
        throw new Exception("No such report with id [" + selectedSingleReportId + "]");
      }
      singleTemplate = new String(Util.gunzip(testRunTemplate.html));
    }

    for (TestRun testRun : testRuns) {

      builder.append(fillTestRun(testRun, new String(singleTemplate), customParameterMap));

      tocBuilder
          .append("<tr")
          .append(testRun.status == TestRunStatus.SUCCESS ? (" style='background-color:" + successColor + ";'")
              : (" style='background-color:" + failColor + "'"))
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

    parseXHtml(writer, document, new ByteArrayInputStream(batchTemplateHTML.getBytes()),
        XMLWorkerHelper.class.getResourceAsStream("/default.css"),
        StandardCharsets.UTF_8, new XMLWorkerFontProvider()
    );
    document.close();
    return baos.toByteArray();
  }


  private static void parseXHtml(PdfWriter writer, Document document, ByteArrayInputStream byteArrayInputStream) {
    parseXHtml(writer, document, byteArrayInputStream, XMLWorkerHelper.class.getResourceAsStream("/default.css"),
        StandardCharsets.UTF_8, new XMLWorkerFontProvider());
  }

  /**
   * @param writer
   *     the writer to use
   * @param doc
   *     the document to use
   * @param in
   *     the {@link InputStream} of the XHTML source.
   * @param in
   *     the {@link CssFiles} of the css files.
   * @param charset
   *     the charset to use
   * @throws IllegalStateException
   *     if the {@link InputStream} could not be read.
   */
  public static void parseXHtml(final PdfWriter writer, final Document doc, final InputStream in, final InputStream inCssFile,
      final Charset charset, final FontProvider fontProvider) {
    CssFilesImpl cssFiles = new CssFilesImpl();
    cssFiles.add(XMLWorkerHelper.getCSS(inCssFile));
    StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver(cssFiles);
    HtmlPipelineContext hpc = new HtmlPipelineContext(new CssAppliersImpl(fontProvider));
    hpc.setImageProvider(new Base64ImageProvider());
    hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory());
    HtmlPipeline htmlPipeline = new HtmlPipeline(hpc, new PdfWriterPipeline(doc, writer));
    Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);
    XMLWorker worker = new XMLWorker(pipeline, true);
    XMLParser p = new XMLParser(true, worker, charset);
    try {
      p.parse(in, charset);
    } catch (Exception ex) {
      throw new IllegalStateException(ex.getMessage(), ex);
    }
  }

  static class Base64ImageProvider extends AbstractImageProvider {

    @Override
    public Image retrieve(String src) {
      int pos = src.indexOf("base64,");
      try {
        Image image;
        if (src.startsWith("data") && pos > 0) {
          byte[] img = Base64.getDecoder().decode(src.substring(pos + 7));
          image = Image.getInstance(img);
        } else {
          image = Image.getInstance(src);
        }

        return image;
      } catch (Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        throw new IllegalStateException(ex);
      }
    }

    @Override
    public String getImageRootPath() {
      return null;
    }
  }
}
