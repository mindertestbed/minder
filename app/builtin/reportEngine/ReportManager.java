package builtin.reportEngine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ReportManager {
  /**
   * get the input XML or byte array template and parse it into a dom
   * document, generate a ReportModel according to it
   */

  public Report report;

  public ReportManager() {
    report = new Report();
  }

  public void setReportTemplate(byte[] template) {

    // parse the template xml to dom document

    try {
      Document templateDoc = ReportParser.parseTemplate(template);

      report.setTemplateDomDocument(templateDoc);
      report.setReportModel(new ReportModel());

    } catch (ParserConfigurationException | SAXException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void setReportData(HashMap<String, String> data) {
    ReportModel rm = report.getReportModel();
    HashMap<String, String> body = rm.getBody();
    body.putAll(data);

  }

  public void setReportAuthor(String author, String email) {
    ReportModel rm = report.getReportModel();
    HashMap<String, String> header = rm.getHeader();
    header.put("Author", author);
    header.put("Email", email);
  }

  public void setReportTitle(String title) {
    ReportModel rm = report.getReportModel();
    rm.setTitle(title);

  }

  private Document fillreportBody(Document document, ReportModel reportModel) {
    // TODO Auto-generated method stub
    Node foundNode = null;
    NodeList nList = ReportParser.getNodeList(document);
    for (int temp2 = 0; temp2 < nList.getLength(); temp2++) {
      Node node = nList.item(temp2);
      foundNode = ReportParser.recursiveFindNode(node, "Data");
    }

    System.out.println(foundNode == null);

    HashMap<String, String> data = reportModel.getBody();
    for (String key : data.keySet()) {

      Node datakey = document.createElement("Key");
      Node datavalue = document.createElement("Value");
      datakey.setTextContent(key);
      datavalue.setTextContent(data.get(key));
      foundNode.appendChild(datakey);
      foundNode.appendChild(datavalue);

    }

    return document;
  }

  private Document fillreportHeader(Document document, ReportModel reportModel) {
    // TODO Auto-generated method stub
    Node foundNode = null;
    NodeList nList = ReportParser.getNodeList(document);
    for (int temp2 = 0; temp2 < nList.getLength(); temp2++) {
      Node node = nList.item(temp2);
      foundNode = ReportParser.recursiveFindNode(node, "Header");
    }

    System.out.println(foundNode == null);

    HashMap<String, String> header = reportModel.getHeader();

    for (String key : header.keySet()) {

      Node text = document.createElement("Text");
      Element eltext = (Element) text;
      eltext.setAttribute("id", key);
      eltext.setAttribute("value", header.get(key));

      foundNode.appendChild(eltext);
    }

    // set time and date
    SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
    String date = sdf.format(new Date());
    Node text = document.createElement("Text");
    Element eltext = (Element) text;
    eltext.setAttribute("id", "ReportDate");
    eltext.setAttribute("value", date);
    foundNode.appendChild(eltext);

    return document;
  }

  private Document fillreportTitle(Document document, ReportModel reportModel) {
    Node foundNode = null;
    NodeList nList = ReportParser.getNodeList(document);
    for (int temp2 = 0; temp2 < nList.getLength(); temp2++) {
      Node node = nList.item(temp2);
      foundNode = ReportParser.recursiveFindNode(node, "Title");
    }

    String title = reportModel.getTitle();

    Node text = document.createElement("Text");
    Element eltext = (Element) text;
    eltext.setAttribute("id", "ReportTitle");
    eltext.setAttribute("value", title);

    foundNode.appendChild(eltext);

    return document;

  }

  /**
   * write the xml to output stream
   */
  public byte[] generateReport() {

    byte[] rep = null;
    try {
      Document doc = report.getTemplateDomDocument();

      doc = fillreportHeader(doc, report.reportModel);
      doc = fillreportTitle(doc, report.reportModel);
      doc = fillreportBody(doc, report.reportModel);

      Transformer transformer = TransformerFactory.newInstance()
          .newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      StreamResult fresult = null;
      StreamResult result = null;

      //fresult = new StreamResult(new FileWriter("samples/report1"));
      result = new StreamResult(new StringWriter());

      DOMSource source = new DOMSource(doc);
      //transformer.transform(source, fresult);
      transformer.transform(source, result);
      String xmlString = result.getWriter().toString();
      //System.out.println(xmlString);
      rep = xmlString.getBytes();

    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return rep;

  }

  public static void main(String[] args) {

    HashMap<String, String> data = new HashMap<>();
    data.put("Student Name", "Leong Chee");
    data.put("Phone", "1-111-111");
    data.put("Result", "100");

    ReportManager rmg = new ReportManager();
    rmg.setReportTemplate("samples/template1.xml".getBytes());
    rmg.setReportAuthor("Virginia Hayes", "vhayes@smail.com");
    rmg.setReportTitle("Student Grades Report");
    rmg.setReportData(data);
    rmg.generateReport();

  }
}
