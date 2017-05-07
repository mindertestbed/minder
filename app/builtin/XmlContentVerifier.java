package builtin;

import minderengine.Slot;
import org.beybunproject.xmlContentVerifier.Schema;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by yerlibilgin on 11/12/14.
 */
public class XmlContentVerifier extends BuiltInAdapter {
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
  public String verifyXsd(Schema xsd, byte[] xml) {
    try {
      return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Slot
  public String verifyXsd(Schema xsd, InputStream xml) {
    try {
      return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Slot
  public String verifyXsd(byte[] xsd, byte[] xml) {
    try {
      return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Slot
  public String verifyXsd(byte[] xsd, InputStream xml) {
    try {
      return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(new ByteArrayInputStream(xsd), xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Slot
  public String verifyXsd(URL xsd, InputStream xml) {
    try {
      return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd.toExternalForm(), xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Slot
  public String verifyXsd(URL xsd, URL xml) {
    try {
      return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifyXsd(xsd, xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }


  @Slot
  public String verifySchematron(byte[] sch, byte[] xml, Properties params) {
    return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(sch, xml, params);
  }


  @Slot
  public String verifySchematron(URL url, byte[] xml, Properties params) {
    return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(url, xml, params);
  }

  @Slot
  public String verifySchematron(URL url, InputStream xml, Properties params) {
    return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(url, xml, params);
  }

  @Slot
  public String verifySchematron(Schema schema, InputStream xml, Properties params) {
    return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(schema, xml, params);
  }

  @Slot
  public String verifySchematron(Schema schema, byte[] xml, Properties params) {
    return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(schema, new ByteArrayInputStream(xml), params);
  }

  /**
   * Performs schematron verification with the given schematron file on the provided xml
   *
   * @param schematron
   * @param xml
   * @param params
   */
  @Slot
  public String verifySchematron(InputStream schematron, InputStream xml, Properties params) {
    return org.beybunproject.xmlContentVerifier.XmlContentVerifier.verifySchematron(schematron, xml, params);
  }
}

