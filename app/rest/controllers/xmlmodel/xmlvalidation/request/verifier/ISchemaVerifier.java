package rest.controllers.xmlmodel.xmlvalidation.request.verifier;

import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;

/**
 * Created by melis on 26/10/15.
 */
public interface ISchemaVerifier {
    public String verify(Schema schema, byte[] xml) throws Exception;

    public String verify(String url, byte[] xml) throws Exception;

    public Schema getSchema(String pathToSchema, byte[] schema, ArchiveType archieveType) throws Exception;

    public Schema getSchema(byte[] schema) throws Exception;

    public String getPositiveResult();

}
