
package rest.models.runModel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the rest.models.runModel package. 
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

    private final static QName _SuiteRunStatusRequest_QNAME = new QName("", "suiteRunStatusRequest");
    private final static QName _SuiteRunResponse_QNAME = new QName("", "suiteRunResponse");
    private final static QName _MinderError_QNAME = new QName("", "minderError");
    private final static QName _SuiteRunRequest_QNAME = new QName("", "suiteRunRequest");
    private final static QName _TestCaseRunRequest_QNAME = new QName("", "testCaseRunRequest");
    private final static QName _SuiteRunStatusResponse_QNAME = new QName("", "suiteRunStatusResponse");
    private final static QName _TestCaseRunResponse_QNAME = new QName("", "testCaseRunResponse");
    private final static QName _TestRunStatusResponse_QNAME = new QName("", "testRunStatusResponse");
    private final static QName _TestRunStatusRequest_QNAME = new QName("", "testRunStatusRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: rest.models.runModel
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SuiteRunStatusRequest }
     * 
     */
    public SuiteRunStatusRequest createSuiteRunStatusRequest() {
        return new SuiteRunStatusRequest();
    }

    /**
     * Create an instance of {@link SuiteRunResponse }
     * 
     */
    public SuiteRunResponse createSuiteRunResponse() {
        return new SuiteRunResponse();
    }

    /**
     * Create an instance of {@link MinderError }
     * 
     */
    public MinderError createMinderError() {
        return new MinderError();
    }

    /**
     * Create an instance of {@link SuiteRunRequest }
     * 
     */
    public SuiteRunRequest createSuiteRunRequest() {
        return new SuiteRunRequest();
    }

    /**
     * Create an instance of {@link TestCaseRunRequest }
     * 
     */
    public TestCaseRunRequest createTestCaseRunRequest() {
        return new TestCaseRunRequest();
    }

    /**
     * Create an instance of {@link SuiteRunStatusResponse }
     * 
     */
    public SuiteRunStatusResponse createSuiteRunStatusResponse() {
        return new SuiteRunStatusResponse();
    }

    /**
     * Create an instance of {@link TestCaseRunResponse }
     * 
     */
    public TestCaseRunResponse createTestCaseRunResponse() {
        return new TestCaseRunResponse();
    }

    /**
     * Create an instance of {@link TestRunStatusResponse }
     * 
     */
    public TestRunStatusResponse createTestRunStatusResponse() {
        return new TestRunStatusResponse();
    }

    /**
     * Create an instance of {@link TestRunStatusRequest }
     * 
     */
    public TestRunStatusRequest createTestRunStatusRequest() {
        return new TestRunStatusRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SuiteRunStatusRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "suiteRunStatusRequest")
    public JAXBElement<SuiteRunStatusRequest> createSuiteRunStatusRequest(SuiteRunStatusRequest value) {
        return new JAXBElement<SuiteRunStatusRequest>(_SuiteRunStatusRequest_QNAME, SuiteRunStatusRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SuiteRunResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "suiteRunResponse")
    public JAXBElement<SuiteRunResponse> createSuiteRunResponse(SuiteRunResponse value) {
        return new JAXBElement<SuiteRunResponse>(_SuiteRunResponse_QNAME, SuiteRunResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MinderError }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "minderError")
    public JAXBElement<MinderError> createMinderError(MinderError value) {
        return new JAXBElement<MinderError>(_MinderError_QNAME, MinderError.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SuiteRunRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "suiteRunRequest")
    public JAXBElement<SuiteRunRequest> createSuiteRunRequest(SuiteRunRequest value) {
        return new JAXBElement<SuiteRunRequest>(_SuiteRunRequest_QNAME, SuiteRunRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestCaseRunRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "testCaseRunRequest")
    public JAXBElement<TestCaseRunRequest> createTestCaseRunRequest(TestCaseRunRequest value) {
        return new JAXBElement<TestCaseRunRequest>(_TestCaseRunRequest_QNAME, TestCaseRunRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SuiteRunStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "suiteRunStatusResponse")
    public JAXBElement<SuiteRunStatusResponse> createSuiteRunStatusResponse(SuiteRunStatusResponse value) {
        return new JAXBElement<SuiteRunStatusResponse>(_SuiteRunStatusResponse_QNAME, SuiteRunStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestCaseRunResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "testCaseRunResponse")
    public JAXBElement<TestCaseRunResponse> createTestCaseRunResponse(TestCaseRunResponse value) {
        return new JAXBElement<TestCaseRunResponse>(_TestCaseRunResponse_QNAME, TestCaseRunResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestRunStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "testRunStatusResponse")
    public JAXBElement<TestRunStatusResponse> createTestRunStatusResponse(TestRunStatusResponse value) {
        return new JAXBElement<TestRunStatusResponse>(_TestRunStatusResponse_QNAME, TestRunStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestRunStatusRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "testRunStatusRequest")
    public JAXBElement<TestRunStatusRequest> createTestRunStatusRequest(TestRunStatusRequest value) {
        return new JAXBElement<TestRunStatusRequest>(_TestRunStatusRequest_QNAME, TestRunStatusRequest.class, null, value);
    }

}
