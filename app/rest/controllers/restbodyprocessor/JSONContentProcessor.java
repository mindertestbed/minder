package rest.controllers.restbodyprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import play.mvc.Http;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * The JSON content processor parse a request body or prepare a response body according using the JAXB JSON
 * marshaller/unmarshaller. It has a generic structure which is unaware of the marshalling/unmarshalling class.
 *
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 */
public class JSONContentProcessor implements IRestContentProcessor {
    private JsonNode json;
    private ObjectNode result;
    String body;

    public JSONContentProcessor() {}

    public JSONContentProcessor(Http.RequestBody body) {
        json = body.asJson();
        //TODO check
        System.out.println("JSON value:" + json.textValue());
        System.out.println("JSON as text:"+json.asText());
    }

    @Override
    public String prepareResponse(String className, Object o) throws ParseException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Class.forName(className));
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            marshaller.marshal(Class.forName(className).cast(o), byteArrayOutputStream);

            return byteArrayOutputStream.toString();

        }catch (JAXBException e) {
            throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception "+e.toString(),0);
        } catch (ClassNotFoundException e) {
            throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception "+e.toString(),0);
        }
    }

    @Override
    public Object parseRequest(String className) throws ParseException{
        try {
            Map<String, Object> jaxbProperties = new HashMap<String, Object>(2);
            JAXBContext jaxbContext = JAXBContext.newInstance(Class.forName(className));
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);

            StreamSource json = new StreamSource(new ByteArrayInputStream(body.getBytes()));
            return  unmarshaller.unmarshal(json, Class.forName(className)).getValue();

        } catch (JAXBException e) {
            throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception "+e.toString(),0);
        } catch (ClassNotFoundException e) {
            throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception "+e.toString(),0);
        }
    }

}
