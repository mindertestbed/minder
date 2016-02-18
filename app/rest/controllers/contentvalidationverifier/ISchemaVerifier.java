package rest.controllers.contentvalidationverifier;

import org.beybunproject.xmlContentVerifier.ArchiveType;
import org.beybunproject.xmlContentVerifier.Schema;

import java.net.MalformedURLException;

/**
 * Services provided by using Minder's built-in content validation functions.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 26/10/15.
 */
public interface ISchemaVerifier {
    public String verify(Schema schema, byte[] xml) throws RuntimeException;

    public String verify(String url, byte[] xml) throws RuntimeException, MalformedURLException;

    public Schema getSchema(String pathToSchema, byte[] schema, ArchiveType archieveType) throws RuntimeException;

    public Schema getSchema(byte[] schema) throws RuntimeException;

    public String getResult(String report);

}
