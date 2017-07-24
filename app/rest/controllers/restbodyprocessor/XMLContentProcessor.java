package rest.controllers.restbodyprocessor;

import play.mvc.Http;

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;

/**
 * The xml content processor parse a request body or prepare a response body according using the JAXB XML
 * marshaller/unmarshaller.
 * JAXB is a 3rd party library which is embedded playframework.
 * The class has generic structure which is unaware of the marshalling/unmarshalling object.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 */
public class XMLContentProcessor implements IRestContentProcessor {
  private String body;

  public XMLContentProcessor() {
  }

  public XMLContentProcessor(String body) {
    this.body = body;
  }

  public XMLContentProcessor(Http.RequestBody body) {

    System.out.println(body);

    String[] parts = body.toString().split("Some\\(");
    if (parts.length > 0) {
      String[] parts2 = parts[1].split("\\)\\,");
      if (parts2.length > 0) {
        this.body = parts2[0];
      }
    }
  }

  @Override
  public String prepareResponse(String className, Object o) throws ParseException {
    try {
      Class clazz = Class.forName(className);

      JAXBContext jaxbContext = JAXBContext.newInstance(clazz.getPackage().getName());
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      marshaller.marshal(clazz.cast(o), byteArrayOutputStream);

      return byteArrayOutputStream.toString();

    } catch (JAXBException e) {
      throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception " + e.toString(), 0);
    } catch (ClassNotFoundException e) {
      throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception " + e.toString(), 0);
    }

  }


  @Override
  public Object parseRequest(String className) throws ParseException {
    try {
      Class clazz = Class.forName(className);
      JAXBContext jaxbContext = JAXBContext.newInstance(clazz.getPackage().getName());
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      Source source = new StreamSource(new ByteArrayInputStream(body.getBytes()));
      JAXBElement<Object> root = jaxbUnmarshaller.unmarshal(source, clazz);

      return clazz.cast(root.getValue());

    } catch (JAXBException e) {
      throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception " + e.toString(), 0);
    } catch (ClassNotFoundException e) {
      throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception " + e.toString(), 0);
    }
  }

  @Override
  public String getContentType() {
    return "application/xml";
  }


}
