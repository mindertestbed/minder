package rest.models;

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

    private final static QName _GetTestCaseDefinitions_QNAME = new QName("", "getTestCaseDefinitions");
    private final static QName _RestMinderResponse_QNAME = new QName("", "restMinderResponse");
    private final static QName _RestValidationRequest_QNAME = new QName("", "restValidationRequest");
    private final static QName _RestTestGroup_QNAME = new QName("", "restTestGroup");
    private final static QName _RestDependencyString_QNAME = new QName("", "restDependencyString");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: rest.controllers.xmlmodel.response
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetTestCaseDefinitions }
     * 
     */
    public GetTestCaseDefinitions createGetTestCaseDefinitions() {
        return new GetTestCaseDefinitions();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTestCaseDefinitions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "getTestCaseDefinitions")
    public JAXBElement<GetTestCaseDefinitions> createGetTestCaseDefinitions(GetTestCaseDefinitions value) {
        return new JAXBElement<GetTestCaseDefinitions>(_GetTestCaseDefinitions_QNAME, GetTestCaseDefinitions.class, null, value);
    }
    
    /**
     * Create an instance of {@link RestMinderResponse }
     * 
     */
    public RestMinderResponse createRestMinderResponse() {
        return new RestMinderResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestMinderResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "restMinderResponse")
    public JAXBElement<RestMinderResponse> createRestMinderResponse(RestMinderResponse value) {
        return new JAXBElement<RestMinderResponse>(_RestMinderResponse_QNAME, RestMinderResponse.class, null, value);
    }
    
    /**
     * Create an instance of {@link RestValidationRequest }
     * 
     */
    public RestValidationRequest createRestValidationRequest() {
        return new RestValidationRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link rest.models.RestValidationRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "validationRequest")
    public JAXBElement<RestValidationRequest> createRestValidationRequest(RestValidationRequest value) {
        return new JAXBElement<RestValidationRequest>(_RestValidationRequest_QNAME, RestValidationRequest.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestDependencyString }
     *
     */
    public RestDependencyString createRestDependencyString() {
        return new RestDependencyString();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestDependencyString }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restDependencyString")
    public JAXBElement<RestDependencyString> createRestDependencyString(RestDependencyString value) {
        return new JAXBElement<RestDependencyString>(_RestDependencyString_QNAME, RestDependencyString.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestTestGroup }
     *
     */
    public RestTestGroup createRestTestGroup() {
        return new RestTestGroup();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestGroup }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestGroup")
    public JAXBElement<RestTestGroup> createRestTestGroup(RestTestGroup value) {
        return new JAXBElement<RestTestGroup>(_RestTestGroup_QNAME, RestTestGroup.class, null, value);
    }
}
