package rest.controllers.xmlmodel.xmlvalidation.request.verifier;

import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;
import rest.controllers.common.Constants;

/**
 * Created by melis on 26/10/15.
 */
public class XMLVerifier extends SchemaVerifier{
    @Override
    public String verify(Schema schema, byte[] xml) throws Exception{
        XmlContentVerifier.verifyXsd(schema, xml);

        return "";
    }

    @Override
    public String verify(String url, byte[] xml) throws Exception{
        XmlContentVerifier.verifyXsd(url,xml);

        return "";
    }

    @Override
    public String getPositiveResult() {
        return Constants.SUCCESS;
    }
}
