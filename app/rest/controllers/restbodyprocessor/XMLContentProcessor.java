package rest.controllers.restbodyprocessor;

import org.w3c.dom.Document;
import play.mvc.Http;

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 */
public class XMLContentProcessor implements IRestContentProcessor {
    private Document dom;
    String body;

    public XMLContentProcessor() {}

    public XMLContentProcessor(Http.RequestBody body) {
        String[] parts = body.toString().split("Some\\(");
        if(parts.length >0){
            String[] parts2 = parts[1].split("\\)\\,");
            if(parts2.length >0){
                this.body = parts2[0];
            }
        }
    }

    @Override
    public String prepareResponse(String className, Object o) {
        try {
            Class clazz = Class.forName(className);

            JAXBContext jaxbContext = JAXBContext.newInstance(clazz.getPackage().getName());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            marshaller.marshal(clazz.cast(o), byteArrayOutputStream);

            return byteArrayOutputStream.toString();

        }catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public Object parseRequest(String className) {
        try {
            Class clazz = Class.forName(className);
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz.getPackage().getName());
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            Source source = new StreamSource(new ByteArrayInputStream(body.getBytes()));
            JAXBElement<Object> root = jaxbUnmarshaller.unmarshal(source, clazz);

            return clazz.cast(root.getValue());

        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
