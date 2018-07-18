package eu.europa.ec.sante.ehdsi.gazelle.validation.util;

public class XdModelType {

    public static final String EPSOS_ED_INIT_REQUEST = "epSOS DispensationService:initialize - request"; // ObjectType.XDR_SUBMIT_REQUEST),
    public static final String EPSOS_ED_INIT_RESPONSE = "epSOS DispensationService:initialize - response"; // ObjectType.XDR_SUBMIT_RESPONSE),
    public static final String EPSOS_ED_DISCARD_REQUEST = "epSOS DispensationService:discard - request"; // ObjectType.XDR_SUBMIT_REQUEST),
    public static final String EPSOS_ED_DISCARD_RESPONSE = "epSOS DispensationService:discard - response"; // ObjectType.XDR_SUBMIT_RESPONSE),
    public static final String EPSOS_CS_PUT_REQUEST = "epSOS ConsentService:put - request"; // ObjectType.XDR_SUBMIT_REQUEST),
    public static final String EPSOS_CS_PUT_RESPONSE = "epSOS ConsentService:put - response"; // ObjectType.XDR_SUBMIT_RESPONSE),
    public static final String EPSOS_CS_DISCARD_REQUEST = "epSOS ConsentService:discard - request"; // ObjectType.XDR_SUBMIT_REQUEST),
    public static final String EPSOS_CS_DISCARD_RESPONSE = "epSOS ConsentService:discard - response"; // ObjectType.XDR_SUBMIT_RESPONSE),
    public static final String EPSOS_OS_LIST_REQUEST_XCF = "epSOS OrderService:list - request (V2.2 XCF)"; // ObjectType.XCF_REQUEST),
    public static final String EPSOS_OS_LIST_RESPONSE_XCF = "epSOS OrderService:list - response (V2.2 XCF)"; // ObjectType.XCF_RESPONSE),
    public static final String EPSOS_OS_LIST_RESPONSE_XCA = "epSOS OrderService:list - response (V1 XCA)"; // ObjectType.XCA_QUERY_RESPONSE),
    public static final String EPSOS_PS_LIST_REQUEST_XCF = "epSOS PatientService:list - request (V2.2 XCF)"; // ObjectType.XCF_REQUEST),
    public static final String EPSOS_PS_LIST_RESPONSE_XCF = "epSOS PatientService:list - response(V2.2 XCF)"; // ObjectType.XCF_RESPONSE),
    public static final String EPSOS_OS_LIST_REQUEST_XCA = "epSOS OrderService : list - request (V1 XCA)"; // ObjectType.XCA_QUERY_REQUEST),
    public static final String EPSOS_PS_LIST_REQUEST_XCA = "epSOS PatientService:list - request (V1 XCA)"; // ObjectType.XCA_QUERY_REQUEST),
    public static final String EPSOS_PS_LIST_RESPONSE_XCA = "epSOS PatientService:list - response (V1 XCA)"; // ObjectType.XCA_QUERY_RESPONSE),
    public static final String EPSOS_OS_RETRIEVE_REQUEST_XCA = "epSOS OrderService:retrieve - request (V1 XCA)"; // ObjectType.XCA_RETRIEVE_REQUEST),
    public static final String EPSOS_OS_RETRIEVE_RESPONSE_XCA = "epSOS OrderService:retrieve - response (V1 XCA)"; // ObjectType.XCA_RETRIEVE_RESPONSE),
    public static final String EPSOS_PS_RETRIEVE_REQUEST_XCA = "epSOS PatientService:retrieve - request (V1 XCA)"; // ObjectType.XCA_RETRIEVE_REQUEST),
    public static final String EPSOS_PS_RETRIEVE_RESPONSE_XCA = "epSOS PatientService:retrieve - response (V1 XCA)"; // ObjectType.XCA_RETRIEVE_RESPONSE),
    public static final String EPSOS2_FETCH_DOC_QUERY_REQUEST = "epSOS-2 FetchDocumentService QUERY - request"; // ObjectType.XCA_QUERY_REQUEST),
    public static final String EPSOS2_FETCH_DOC_QUERY_RESPONSE = "epSOS-2 FetchDocumentService QUERY - response"; // ObjectType.XCA_QUERY_RESPONSE),
    public static final String EPSOS2_FETCH_DOC_RETRIEVE_REQUEST = "epSOS-2 FetchDocumentService RETRIEVE - request"; // ObjectType.XCA_RETRIEVE_REQUEST),
    public static final String EPSOS2_FETCH_DOC_RETRIEVE_RESPONSE = "epSOS-2 FetchDocumentService RETRIEVE - response"; // ObjectType.XCA_RETRIEVE_RESPONSE),
    public static final String EPSOS2_PROVIDE_DATA_REQUEST = "epSOS-2 ProvideDataService - request"; // ObjectType.XDR_SUBMIT_REQUEST),
    public static final String EPSOS2_PROVIDE_DATA_RESPONSE = "epSOS-2 ProvideDataService - response"; // ObjectType.XDR_SUBMIT_RESPONSE);

    private XdModelType() {
    }
}
