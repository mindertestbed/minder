package rest.controllers.contentvalidationverifier;

import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;
import rest.controllers.common.Constants;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class subclasses SchemaVerifier and implements verify methods. If an exception received from Minder core,
 * it simply throws the exception. In case of exception-free validation, Minder does not throw any exception.
 * The verify methods return the schematron report(provided by Minder), which states the affirmative result or gives the failures.
 *
 * The XSD validation of Minder produces just two result: Success or fail unlike the SchematronVerifier which produces
 * Undefined or Fail. Thus, both "positive and negative" results(aka. UNDEFINED) of a schematron validation resides in schematron Report and the
 * receiver side must interpret these results. In case of exception throwing, the result will be FAIL.
 *
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 26/10/15.
 */
public class SchematronVerifier extends SchemaVerifier{
    @Override
    public String verify(Schema schema, byte[] xml) throws RuntimeException{
        XmlContentVerifier.verifySchematron(schema, xml);

        return XmlContentVerifier.schematronReport(schema,xml,null);
    }

    @Override
    public String verify(String url, byte[] xml) throws RuntimeException, MalformedURLException {
        XmlContentVerifier.verifySchematron(url, xml);

        URL url2 = new URL(url);
        return XmlContentVerifier.schematronReport(url2,xml,null);
    }

    @Override
    public String getPositiveResult() {
        return Constants.UNDEFINED;
    }
}

