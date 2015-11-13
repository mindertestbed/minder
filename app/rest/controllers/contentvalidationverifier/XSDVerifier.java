package rest.controllers.contentvalidationverifier;

import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;
import rest.controllers.common.Constants;

import java.net.MalformedURLException;

/**
 * This class subclasses SchemaVerifier and implements verify methods. If an exception received from Minder core,
 * it simply throws the exception. In case of exception-free validation, Minder does not throw any exception.
 * The verify methods return null String, since Minder does not return any validation report for XSD validations.
 *
 * The XSD validation of Minder produces just two result: Success or fail unlike the SchematronVerifier which produces
 * Undefined or Fail. Thus, the positive result for XSDs are always affirmative success.
 *
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 26/10/15.
 */
public class XSDVerifier extends SchemaVerifier{
    @Override
    public String verify(Schema schema, byte[] xml) throws RuntimeException{
        XmlContentVerifier.verifyXsd(schema, xml);

        return "";
    }

    @Override
    public String verify(String url, byte[] xml) throws RuntimeException,MalformedURLException {
        XmlContentVerifier.verifyXsd(url,xml);

        return "";
    }

    @Override
    public String getPositiveResult() {
        return Constants.SUCCESS;
    }
}
