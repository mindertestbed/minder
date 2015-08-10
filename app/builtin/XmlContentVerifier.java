package builtin;

import assets.iso_schematron_xslt2.SchematronClassResolver;
import minderengine.Slot;
import play.Logger;

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
import java.util.Map;

/**
 * Created by yerlibilgin on 11/12/14.
 */
public class XmlContentVerifier extends BuiltInWrapper {
  static {
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
  }

  private SchematronClassResolver resolver;
  private TransformerFactory tFactory = TransformerFactory.newInstance();

  public XmlContentVerifier() {
    System.err.println("Created XML CONTENT VERIFIER");
    resolver = new SchematronClassResolver();
    tFactory = TransformerFactory.newInstance();
    tFactory.setURIResolver(resolver);
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
  public void verifyXsd(byte[] xsd, byte[] xml) {
    Logger.debug("VERIFY XSD : " + new String(xsd));
    Logger.debug("VERIFY XML : " + new String(xml));

    //read schema
    Schema schema;
    try {
      SchemaFactory schemaFactory = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(xsd)));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to parse schema", ex);
    }

    try {
      Source xmlFile = new StreamSource(new ByteArrayInputStream(xml));
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);
      System.out.println("XSD verification success");
    } catch (Exception e) {
      System.out.println("XSD verification fail");
      e.printStackTrace();
      throw new RuntimeException("XML Verification failed", e);
    }
  }

  @Slot
  public void verifySchematron(byte[] sch, byte[] xml, Entry... entries) {
    ByteArrayInputStream bSchematron = new ByteArrayInputStream(sch);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    verifySchematron(bSchematron, bXml, bOut, entries);

    byte[] result = bOut.toByteArray();
    //todo: perform a an actual error handling.
    String str = new String(result);
    if (str.contains("failed")) {
      Logger.debug("Schematron result " + str);
      throw new RuntimeException("Schematron verification failed");
    }
  }

  /**
   * Simple transformation method.
   *
   * @param xslStream    - The input stream that the xsl will be read from
   * @param sourceStream - Input that the xml for verification will be read from.
   * @param outputStream - The output stream that the result will be written into.
   */
  public void simpleTransform(InputStream xslStream, InputStream sourceStream, OutputStream outputStream, Entry... params) {
    try {
      Transformer transformer =
          tFactory.newTransformer(new StreamSource(xslStream));


      for (Map.Entry<String, String> entry : params) {
        System.out.println(entry.getKey() + " " + entry.getValue());
        transformer.setParameter(entry.getKey(), entry.getValue());
      }
      transformer.transform(new StreamSource(sourceStream),
          new StreamResult(outputStream));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Simple transformation method.
   *
   * @param xsl - The byte array that includes the xsl
   * @param xml - The byte array that includes the xml
   * @return result as byte []
   */
  public byte[] simpleTransform(byte[] xsl, byte[] xml, Entry... params) {
    ByteArrayInputStream bXsl = new ByteArrayInputStream(xsl);
    ByteArrayInputStream bXml = new ByteArrayInputStream(xml);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    simpleTransform(bXsl, bXml, baos, params);
    return baos.toByteArray();
  }


  /**
   * Performs schematron verification with the given schematrno file on the provided xml
   *
   * @param schematron
   * @param xml
   * @param result
   */
  @Slot
  public void verifySchematron(InputStream schematron, InputStream xml, OutputStream result, Entry... params) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    simpleTransform(resolver.rstrm("iso_schematron_xslt2/iso_dsdl_include.xsl"),
        schematron, baos);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransform(resolver.rstrm("iso_schematron_xslt2/iso_abstract_expand.xsl"), bais,
        baos);

    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransform(resolver.rstrm("iso_schematron_xslt2/iso_svrl_for_xslt2.xsl"), bais
        , baos);

    bais = new ByteArrayInputStream(baos.toByteArray());
    baos.reset();
    simpleTransform(bais, xml, result, params);
  }

  public static void main(String[] args) throws IOException {
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
}

