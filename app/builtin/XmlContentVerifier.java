package builtin;

import minderengine.Slot;
import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * Created by yerlibilgin on 11/12/14.
 */
public class XmlContentVerifier extends BuiltInWrapper {
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
  public void verifyXsd(Schema xsd, byte[] xml) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
  }

  @Slot
  public void verifyXsd(Schema xsd, InputStream xml) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
  }

  @Slot
  public void verifyXsd(byte[] xsd, byte[] xml) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
  }

  @Slot
  public void verifyXsd(byte[] xsd, InputStream xml) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(new ByteArrayInputStream(xsd), xml);
  }

  @Slot
  public void verifyXsd(URL xsd, InputStream xml) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd.toExternalForm(), xml);
  }

  @Slot
  public void verifyXsd(URL xsd, URL xml) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
  }

  @Slot
  public void verifySchematron(byte[] sch, byte[] xml, Properties params) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(sch, xml, params);
  }


  @Slot
  public void verifySchematron(URL url, byte[] xml, Properties params) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(url, xml, params);
  }

  @Slot
  public void verifySchematron(URL url, InputStream xml, Properties params) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(url, xml, params);
  }

  @Slot
  public void verifySchematron(Schema schema, InputStream xml, Properties params) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(schema, xml, params);
  }

  @Slot
  public void verifySchematron(Schema schema, byte[] xml, Properties params) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(schema, new ByteArrayInputStream(xml), params);
  }

  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   * @param params
   */
  @Slot
  public void verifySchematron(InputStream schematron, InputStream xml, Properties params) {
    org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(schematron, xml, params);
  }
}

