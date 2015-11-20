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
    private final static QName _RestWrapper_QNAME = new QName("", "restWrapper");
    private final static QName _RestWrapperVersion_QNAME = new QName("", "restWrapperVersion");
    private final static QName _RestWrapperList_QNAME = new QName("", "restWrapperList");
    private final static QName _RestTestAssertion_QNAME = new QName("", "restWrapperList");
    private final static QName _RestTestCase_QNAME = new QName("", "restWrapperList");
    private final static QName _RestTdl_QNAME = new QName("", "restTdl");

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

    /**
     * Create an instance of {@link rest.models.RestWrapper }
     *
     */
    public RestWrapper createRestWrapper() {
        return new RestWrapper();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestWrapper }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restWrapper")
    public JAXBElement<RestWrapper> createRestWrapper(RestWrapper value) {
        return new JAXBElement<RestWrapper>(_RestWrapper_QNAME, RestWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestWrapperVersion }
     *
     */
    public RestWrapperVersion createRestWrapperVersion() {
        return new RestWrapperVersion();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestWrapperVersion }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restWrapperVersion")
    public JAXBElement<RestWrapperVersion> createRestWrapperVersion(RestWrapperVersion value) {
        return new JAXBElement<RestWrapperVersion>(_RestWrapperVersion_QNAME, RestWrapperVersion.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestWrapperList }
     *
     */
    public RestWrapperList createRestWrapperList() {
        return new RestWrapperList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestWrapperList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restWrapperList")
    public JAXBElement<RestWrapperList> createRestWrapperList(RestWrapperList value) {
        return new JAXBElement<RestWrapperList>(_RestWrapperList_QNAME, RestWrapperList.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestTestAssertion }
     *
     */
    public RestTestAssertion createRestTestAssertion() {
        return new RestTestAssertion();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestAssertion }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestAssertion")
    public JAXBElement<RestTestAssertion> createRestTestAssetion(RestTestAssertion value) {
        return new JAXBElement<RestTestAssertion>(_RestTestAssertion_QNAME, RestTestAssertion.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestTestCase }
     *
     */
    public RestTestCase createRestTestCase() {
        return new RestTestCase();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestCase }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestCase")
    public JAXBElement<RestTestCase> createRestTestCase(RestTestCase value) {
        return new JAXBElement<RestTestCase>(_RestTestCase_QNAME, RestTestCase.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestTdl }
     *
     */
    public RestTdl createRestTdl() {
        return new RestTdl();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTdl }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTdl")
    public JAXBElement<RestTdl> createRestTdl(RestTdl value) {
        return new JAXBElement<RestTdl>(_RestTdl_QNAME, RestTdl.class, null, value);
    }
}
