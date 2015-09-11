package builtin;

import assets.iso_schematron_xslt2.SchematronClassResolver;
import minderengine.Slot;
import mtdl.Utils;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import play.Logger;
import scala.io.Codec;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.Exception;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.Map;
import java.util.Properties;

import mtdl.utils.*;

/**
 * Created by yerlibilgin on 11/12/14.
 */
public class XmlContentVerifier extends BuiltInWrapper {

  Utils utils = new Utils();

  public XmlContentVerifier() {
    System.err.println("Created XML CONTENT VERIFIER");
  }

  @Override
  public String getShortDescription() {
    return "The default minder xml content verifier";
  }

  /**
   * Checks the schema of the xml WRT the given xsd and returns the result
   *
   * @param xsd the schema definition that will be used for verification
   * @param xml the xml that will be verified
   * @return the result of the verification process
   */
  @Slot
  public void verifyXsd(MinderXsd xsd, byte[] xml) {
    utils.verifyXsd(xsd, xml);
  }

  @Slot
  public void verifyXsdStream(MinderXsd xsd, InputStream xml) {
    utils.verifyXsdStream(xsd, xml);
  }


  @Slot
  public void verifySchematron(byte[] sch, byte[] xml, Properties params) {
    utils.verifySchematron(sch, xml, params);
  }

  /**
   * Performs schematron verification with the given schematrno file on the provided xml
   *
   * @param schematron
   * @param xml
   * @param result
   */
  @Slot
  public void verifySchematronStream(InputStream schematron, InputStream xml, OutputStream result, Properties params) {
    utils.verifySchematronStream(schematron, xml, result, params);
  }
/*
  public static void main2(String[] args) throws IOException {
    //Set saxon as transformer.
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");

    FileInputStream fisSch = new FileInputStream("chapter.sch");
    FileInputStream fisXml = new FileInputStream("in.xml");
    FileOutputStream fosResult = new FileOutputStream("result.xml");
    XmlContentVerifier verf = new XmlContentVerifier();
    verf.verifySchematron(fisSch, fisXml, fosResult, new Entry("myVar", "apppproved"));
    fisSch.close();
    fisXml.close();
    fosResult.close();
  }

  public static void main(String[] args) throws IOException {
    //Set saxon as transformer.
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");

    //byte []xsd = scala.io.Source.fromFile("sampleXsd/EuropeanCore.xsd", Codec.string2codec("utf-8")).mkString().getBytes();
    //byte []xml = scala.io.Source.fromFile("pom.xml", Codec.string2codec("utf-8")).mkString().getBytes();
    XmlContentVerifier verf = new XmlContentVerifier();
    verf.verifyXsdFile(new File("sampleXsd/EuropeanCore.xsd").toURI().toURL(), new File("pom.xml").toURI().toURL());

  }*/
}

