package rest.controllers.contentvalidationverifier;

import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;
import org.omg.SendingContext.RunTimeOperations;
import org.xml.sax.SAXException;
import rest.controllers.common.Constants;

import java.io.IOException;
import java.net.MalformedURLException;

import static rest.controllers.common.Constants.FAILURE;
import static rest.controllers.common.Constants.SUCCESS;

/**
 * This class subclasses SchemaVerifier and implements verify methods. If an exception received from Minder core,
 * it simply throws the exception. In case of exception-free validation, Minder does not throw any exception.
 * The verify methods return null String, since Minder does not return any validation report for XSD validations.
 * <p>
 * The Schema validation of Minder produces just two result: Success or fail.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 26/10/15.
 */
public class XSDVerifier extends SchemaVerifier {
  @Override
  public String verify(Schema schema, byte[] xml) throws RuntimeException {
    try {
      return XmlContentVerifier.verifyXsd(schema, xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String verify(String url, byte[] xml) throws RuntimeException, MalformedURLException {
    try {
      return XmlContentVerifier.verifyXsd(url, xml);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getResult(String report) {
    if (report.contains("<error>")|| report.contains("<fatalerror>")) {
      return FAILURE;
    }
    return SUCCESS;
  }
}

