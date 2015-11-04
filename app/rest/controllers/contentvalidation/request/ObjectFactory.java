
package rest.controllers.contentvalidation.request;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the rest.controllers.restbodyobject.xmlmodel.xmlvalidation.request package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ValidationRequest_QNAME = new QName("", "validationRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: rest.controllers.restbodyobject.xmlmodel.xmlvalidation.request
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ValidationRequest }
     * 
     */
    public ValidationRequest createValidationRequest() {
        return new ValidationRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidationRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "validationRequest")
    public JAXBElement<ValidationRequest> createValidationRequest(ValidationRequest value) {
        return new JAXBElement<ValidationRequest>(_ValidationRequest_QNAME, ValidationRequest.class, null, value);
    }

}
