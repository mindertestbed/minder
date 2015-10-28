package rest.controllers.xmlmodel.xmlvalidation.request.verifier;

import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;

/**
 * Created by melis on 26/10/15.
 */
public abstract class SchemaVerifier implements ISchemaVerifier{
    public Schema getSchema(String pathToSchema, byte[] schema, ArchiveType archieveType) throws Exception{
        return XmlContentVerifier.schemaFromByteArray(pathToSchema, schema, archieveType);
    }

    public Schema getSchema(byte[] schema) throws Exception{
        return XmlContentVerifier.schemaFromByteArray(schema);
    }

    abstract public String verify(Schema schema, byte[] xml) throws Exception;

    abstract public String verify(String url, byte[] xml) throws Exception;

    abstract public String getPositiveResult();
}
