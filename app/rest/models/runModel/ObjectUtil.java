package rest.models.runModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;

/**
 * @author: ${user}
 * @date: 23/07/17.
 */
public class ObjectUtil {

  public static byte[] newMinderError(long inventoryId, int errorCode, String errorSummary, byte[] errorLog) throws JAXBException {
    ObjectFactory objectFactory = new ObjectFactory();
    MinderError minderError = objectFactory.createMinderError();
    minderError.errorCode = errorCode;
    minderError.errorSummary = errorSummary;
    minderError.errorLog = errorLog;
    minderError.inventoryId = inventoryId;


    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    marshaller.marshal(objectFactory.createMinderError(minderError), os);
    return os.toByteArray();
  }

  public static byte[] marshalAsByteArray(Object object) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    XmlType xmlType = object.getClass().getDeclaredAnnotation(XmlType.class);

    QName name = new QName("", xmlType.name());
    JAXBElement<Object> element = new JAXBElement<>(name, (Class<Object>) object.getClass(), object);
    marshaller.marshal(element, os);
    return os.toByteArray();
  }

  public static byte[] marshal(Object object, QName qName) {
    try {
      ObjectFactory factory = new ObjectFactory();
      JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      marshaller.marshal(new JAXBElement<Object>(qName, (Class<Object>) object.getClass(), null, object), os);

      return os.toByteArray();
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
}
