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
    private final static QName _RestAdapter_QNAME = new QName("", "restAdapter");
    private final static QName _RestAdapterVersion_QNAME = new QName("", "restAdapterVersion");
    private final static QName _RestAdapterList_QNAME = new QName("", "restAdapterList");
    private final static QName _RestTestAssertion_QNAME = new QName("", "restTestAssertion");
    private final static QName _RestTestCase_QNAME = new QName("", "restTestCase");
    private final static QName _RestTdl_QNAME = new QName("", "restTdl");
    private final static QName _RestTestAsset_QNAME = new QName("", "restTestAsset");
    private final static QName _RestTestAssetList_QNAME = new QName("", "restTestAssetList");
    private final static QName _RestTestGroupList_QNAME = new QName("", "restTestGroupList");
    private final static QName _RestTestAssertionList_QNAME = new QName("", "restTestAssertionList");
    private final static QName _RestTestCaseList_QNAME = new QName("", "restTestCaseList");
    private final static QName _RestUtilClass_QNAME = new QName("", "restUtilClass_QNAME");
    private final static QName _RestUtilClassList_QNAME = new QName("", "restUtilClassList_QNAME");
    private final static QName _RestJob_QNAME = new QName("", "restJob_QNAME");
    private final static QName _RestJobList_QNAME = new QName("", "restJobList_QNAME");
    private final static QName _RestParametersForAdapters_QNAME = new QName("", "restParametersForAdapters_QNAME");
    private final static QName _RestTdlAdapterParam_QNAME = new QName("", "restTdlAdapterParam_QNAME");


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
     * Create an instance of {@link RestAdapter }
     *
     */
    public RestAdapter createRestAdapter() {
        return new RestAdapter();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestAdapter }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restAdapter")
    public JAXBElement<RestAdapter> createRestAdapter(RestAdapter value) {
        return new JAXBElement<RestAdapter>(_RestAdapter_QNAME, RestAdapter.class, null, value);
    }

    /**
     * Create an instance of {@link RestAdapterVersion }
     *
     */
    public RestAdapterVersion createRestAdapterVersion() {
        return new RestAdapterVersion();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestAdapterVersion }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restAdapterVersion")
    public JAXBElement<RestAdapterVersion> createRestAdapterVersion(RestAdapterVersion value) {
        return new JAXBElement<RestAdapterVersion>(_RestAdapterVersion_QNAME, RestAdapterVersion.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestAdapterList }
     *
     */
    public RestAdapterList createRestAdapterList() {
        return new RestAdapterList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestAdapterList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restAdapterList")
    public JAXBElement<RestAdapterList> createRestAdapterList(RestAdapterList value) {
        return new JAXBElement<RestAdapterList>(_RestAdapterList_QNAME, RestAdapterList.class, null, value);
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
    public JAXBElement<RestTestAssertion> createRestTestAssertion(RestTestAssertion value) {
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


    /**
     * Create an instance of {@link rest.models.RestTestAsset }
     *
     */
    public RestTestAsset createRestTestAsset() {
        return new RestTestAsset();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestAsset }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestAsset")
    public JAXBElement<RestTestAsset> createRestTestAsset(RestTestAsset value) {
        return new JAXBElement<RestTestAsset>(_RestTestAsset_QNAME, RestTestAsset.class, null, value);
    }


    /**
     * Create an instance of {@link rest.models.RestTestAssertionList }
     *
     */
    public RestTestAssertionList createRestTestAssertionList() {
        return new RestTestAssertionList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestAssertionList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestAssertionList")
    public JAXBElement<RestTestAssertionList> createRestTestAssertionList(RestTestAssertionList value) {
        return new JAXBElement<RestTestAssertionList>(_RestTestAssertionList_QNAME, RestTestAssertionList.class, null, value);
    }


    /**
     * Create an instance of {@link rest.models.RestTestGroupList }
     *
     */
    public RestTestGroupList createRestTestGroupList() {
        return new RestTestGroupList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestGroupList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestGroupList")
    public JAXBElement<RestTestGroupList> createRestTestGroupList(RestTestGroupList value) {
        return new JAXBElement<RestTestGroupList>(_RestTestGroupList_QNAME, RestTestGroupList.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestTestCaseList }
     *
     */
    public RestTestCaseList createRestTestCaseList() {
        return new RestTestCaseList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestCaseList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestCaseList")
    public JAXBElement<RestTestCaseList> createRestTestCaseList(RestTestCaseList value) {
        return new JAXBElement<RestTestCaseList>(_RestTestCaseList_QNAME, RestTestCaseList.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestUtilClass }
     *
     */
    public RestUtilClass createRestUtilClass() {
        return new RestUtilClass();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestUtilClass }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restUtilClass")
    public JAXBElement<RestUtilClass> createRestUtilClass(RestUtilClass value) {
        return new JAXBElement<RestUtilClass>(_RestUtilClass_QNAME, RestUtilClass.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestUtilClassList }
     *
     */
    public RestUtilClassList createRestUtilClassList() {
        return new RestUtilClassList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestUtilClassList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restUtilClassList")
    public JAXBElement<RestUtilClassList> createRestUtilClassList(RestUtilClassList value) {
        return new JAXBElement<RestUtilClassList>(_RestUtilClassList_QNAME, RestUtilClassList.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestTestAssetList }
     *
     */
    public RestTestAssetList createRestTestAssetList() {
        return new RestTestAssetList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTestAssetList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTestAssetList")
    public JAXBElement<RestTestAssetList> createRestTestAssetList(RestTestAssetList value) {
        return new JAXBElement<RestTestAssetList>(_RestTestAssetList_QNAME, RestTestAssetList.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestJob }
     *
     */
    public RestJob createRestJob() {
        return new RestJob();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestJob }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restJob")
    public JAXBElement<RestJob> createRestJob(RestJob value) {
        return new JAXBElement<RestJob>(_RestJob_QNAME, RestJob.class, null, value);
    }


    /**
     * Create an instance of {@link rest.models.RestJobList }
     *
     */
    public RestJobList createRestJobList() {
        return new RestJobList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestJobList }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restJobList")
    public JAXBElement<RestJobList> createRestJobList(RestJobList value) {
        return new JAXBElement<RestJobList>(_RestJobList_QNAME, RestJobList.class, null, value);
    }

    /**
     * Create an instance of {@link rest.models.RestParametersForAdapters }
     *
     */
    public RestParametersForAdapters createRestParametersForAdapters() {
        return new RestParametersForAdapters();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestParametersForAdapters }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restParametersForAdapters")
    public JAXBElement<RestParametersForAdapters> createRestParametersForAdapters(RestParametersForAdapters value) {
        return new JAXBElement<RestParametersForAdapters>(_RestParametersForAdapters_QNAME, RestParametersForAdapters.class, null, value);
    }


    /**
     * Create an instance of {@link rest.models.RestTdlAdapterParam }
     *
     */
    public RestTdlAdapterParam createRestTdlAdapterParam() {
        return new RestTdlAdapterParam();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestTdlAdapterParam }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "restTdlAdapterParam")
    public JAXBElement<RestTdlAdapterParam> createRestTdlAdapterParam(RestTdlAdapterParam value) {
        return new JAXBElement<RestTdlAdapterParam>(_RestTdlAdapterParam_QNAME, RestTdlAdapterParam.class, null, value);
    }
}
