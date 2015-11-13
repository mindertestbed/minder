
package rest.controllers.xmlmodel.response;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the rest.controllers.xmlmodel.response package. 
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

    private final static QName _MinderResponse_QNAME = new QName("", "minderResponse");
    private final static QName _GetTestCaseDefinitions_QNAME = new QName("", "getTestCaseDefinitions");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: rest.controllers.xmlmodel.response
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MinderResponse }
     * 
     */
    public MinderResponse createMinderResponse() {
        return new MinderResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MinderResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "minderResponse")
    public JAXBElement<MinderResponse> createMinderResponse(MinderResponse value) {
        return new JAXBElement<MinderResponse>(_MinderResponse_QNAME, MinderResponse.class, null, value);
    }
    
    public GetTestCaseDefinitions createGetTestCaseDefinitions() {
        return new GetTestCaseDefinitions();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MinderResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "getTestCaseDefinitions")
    public JAXBElement<GetTestCaseDefinitions> createGetTestCaseDefinitions(GetTestCaseDefinitions value) {
        return new JAXBElement<GetTestCaseDefinitions>(_GetTestCaseDefinitions_QNAME, GetTestCaseDefinitions.class, null, value);
    }

}
