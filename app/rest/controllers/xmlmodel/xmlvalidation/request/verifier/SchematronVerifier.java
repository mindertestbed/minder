package rest.controllers.xmlmodel.xmlvalidation.request.verifier;

import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;
import rest.controllers.common.Constants;

import java.net.URL;

/**
 * Created by melis on 26/10/15.
 */
public class SchematronVerifier extends SchemaVerifier{
    @Override
    public String verify(Schema schema, byte[] xml) throws Exception{
        XmlContentVerifier.verifySchematron(schema, xml);

        return XmlContentVerifier.schematronReport(schema,xml,null);
    }

    @Override
    public String verify(String url, byte[] xml) throws Exception {
        XmlContentVerifier.verifySchematron(url, xml);

        URL url2 = new URL(url);
        return XmlContentVerifier.schematronReport(url2,xml,null);
    }

    @Override
    public String getPositiveResult() {
        return Constants.UNDEFINED;
    }
}

