package rest.controllers.contentvalidation.verifier;

import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;
import org.beybunproject.xmlContentVerifier.XmlContentVerifier;

import java.net.MalformedURLException;

/**
 * This abstract method implements the Interface ISchemaVerifier which states the provided services.
 * getSchema functions are common for both schema and schematron. Therefore, they are implemented in here.
 * On the other hand, verify methods differs for XSD and schematrons. The implementation of these methods are leaved
 * to the subclasses.
 *
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 26/10/15.
 */
public abstract class SchemaVerifier implements ISchemaVerifier{
    public Schema getSchema(String pathToSchema, byte[] schema, ArchiveType archieveType) throws RuntimeException{
        return XmlContentVerifier.schemaFromByteArray(pathToSchema, schema, archieveType);
    }

    public Schema getSchema(byte[] schema) throws RuntimeException{
        return XmlContentVerifier.schemaFromByteArray(schema);
    }

    abstract public String verify(Schema schema, byte[] xml) throws RuntimeException;

    abstract public String verify(String url, byte[] xml) throws RuntimeException, MalformedURLException;

    abstract public String getPositiveResult();
}
