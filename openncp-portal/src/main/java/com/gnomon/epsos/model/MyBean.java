package com.gnomon.epsos.model;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.FacesService;
import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.service.ConsentException;
import com.gnomon.epsos.service.Demographics;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.*;
import eu.epsos.util.IheConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tr.com.srdc.epsos.util.Constants;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.portlet.RenderRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ManagedBean
@SessionScoped
public class MyBean implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBean.class);
    private static final long serialVersionUID = 1L;
    private List<Country> countries;
    private String selectedCountry;
    private List<Identifier> identifiers;
    private List<Demographics> demographics;
    private List<Patient> patients;

    @ManagedProperty(value = "#{selectedPatient}")
    private Patient selectedPatient;
    private List<PatientDocument> patientDocuments;
    private PatientDocument selectedDocument;
    private EpsosDocument selectedEpsosDocument;
    private List<PatientDocument> patientPrescriptions;
    private boolean showDemographics;
    private boolean showPatientList = true;
    private StreamedContent prescriptionFile;
    private PatientDocument selectedPrescriptionFile;
    private Assertion hcpAssertion = null;
    private boolean trcassertionexists = false;
    private boolean trcassertionnotexists = true;
    private Assertion trcAssertion;
    private Date consentStartDate;
    private Date consentEndDate;
    private String consentOpt;
    private String purposeOfUse;
    private String purposeOfUseForPS;
    private String purposeOfUseForEP;
    private String queryPatientsException;
    private String queryDocumentsException;
    private String queryPrescriptionsException;
    private boolean showEP;
    private boolean showPS;
    private boolean showMRO;
    private boolean enableMRO;
    private boolean enableHCER;
    private boolean enableCCD;
    private boolean enableCONSENT;
    private boolean enablePatientDocuments;
    private boolean enablePrescriptionDocuments;
    private boolean canConvert;
    private boolean consentExists;
    private String errorUserAssertion;
    private String cdaStylesheet;
    private String signedTRC;

    public MyBean() {

        LOGGER.info("Initializing MyBean ...");
        checkButtonPermissions();
        String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH");
        LOGGER.info("EPSOS PROPS PATH IS: '{}'", epsosPropsPath);
        if (!Validator.isNull(epsosPropsPath)) {
            try {
                cdaStylesheet = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CDA_STYLESHEET);
                consentExists = false;
                consentOpt = "1";
                countries = new ArrayList<>();
                identifiers = new ArrayList<>();
                demographics = new ArrayList<>();
                countries = EpsosHelperService.getCountriesFromCS(LiferayUtils.getPortalLanguage());
                LOGGER.info("Countries found: '{}'", countries.size());
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error getting user", ""));
                LOGGER.error("Error getting user");
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "EPSOS_PROPS_PATH not found", ""));
        }
    }

    public void showPrescription(ActionEvent actionEvent) throws SystemException {

        RenderRequest renderRequest = (RenderRequest) (FacesContext.getCurrentInstance().getExternalContext()
                .getRequestMap().get("javax.portlet.request"));
        String parameter = renderRequest.getParameter("productId");
        LOGGER.info("PRODUCT ID: '{}'", parameter);
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(String selectedCountry) {

        User user = LiferayUtils.getPortalUser();
        LiferayUtils.storeToSession("user", user);

        this.selectedCountry = selectedCountry;
        identifiers = EpsosHelperService.getCountryIdentifiers(this.selectedCountry, LiferayUtils.getPortalLanguage(),
                null, null);
        demographics = EpsosHelperService.getCountryDemographics(this.selectedCountry, LiferayUtils.getPortalLanguage(),
                null, null);
        showDemographics = !demographics.isEmpty();
        LiferayUtils.storeToSession("selectedCountry", selectedCountry);
        patients = new ArrayList<>();
    }

    public boolean getAssertionExists() {
        return trcassertionexists;
    }

    public boolean getAssertionNotExists() {
        return !trcassertionexists;
    }

    public void searchPatientsRequest(ActionEvent event) {
        checkButtonPermissions();
        LOGGER.info("searchPatientORequest ::: Selected country is : '{}'", selectedCountry);
        String country = (String) event.getComponent().getAttributes().get("selectedCountry");
        identifiers = (List<Identifier>) event.getComponent().getAttributes().get("identifiers");
        demographics = (List<Demographics>) event.getComponent().getAttributes().get("demographics");
        if (Validator.isNotNull(country)) {
            selectedCountry = country;
        }
        LOGGER.info("searchPatientsRequest ::: Selected country is : '{}'", selectedCountry);
        searchPatients();
    }

    private void searchPatients(Assertion assertion, List<Identifier> identifiers, List<Demographics> demographics, String country) {

        LOGGER.info("Search Patients selected country is: '{}'", country);
        String runningMode = MyServletContextListener.getRunningMode();
        Assertion ass;

        if (StringUtils.equals(runningMode, "demo")) {
            patients = EpsosHelperService.getMockPatients();
            trcAssertion = null;
            trcassertionnotexists = true;
            trcassertionexists = false;
            showPatientList = true;

        } else {

            try {
                trcAssertion = null;
                trcassertionnotexists = true;
                trcassertionexists = false;
                patients = new ArrayList<>();

                String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
                LOGGER.info("Connector URL: '{}'", serviceUrl);

                PatientDemographics pd = EpsosHelperService.createPatientDemographicsForQuery(identifiers, demographics);
                ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();
                LOGGER.info("Test Assertions: " + EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_TEST_ASSERTIONS));
                ass = assertion;

                if (LOGGER.isDebugEnabled()) {

                    LOGGER.debug("Searching for patients in '{}'", country);
                    LOGGER.debug("Assertion id: '{}'", ass.getID());
                    LOGGER.debug("PatientDemographic: '{}'", pd.toString());
                }

                List<PatientDemographics> queryPatient = proxy.queryPatient(ass, country, pd);

                for (PatientDemographics aux : queryPatient) {

                    Patient patient = EpsosHelperService.populatePatient(aux);
                    patients.add(patient);
                    queryPatientsException = "";
                }
                if (queryPatient.isEmpty()) {
                    queryPatientsException = LiferayUtils.getPortalTranslation("patient.list.no.patient");
                }
                LOGGER.info("Found '{}' patients", patients.size());
                showPatientList = true;

            } catch (Exception ex) {

                LOGGER.error(ExceptionUtils.getStackTrace(ex));
                patients = new ArrayList<>();
                showPatientList = true;
                queryPatientsException = LiferayUtils.getPortalTranslation(ex.getMessage());
            }
        }
    }

    public void searchPatients() {

        LOGGER.info("Searching for patients (creating assertions)...");
        Object obj = EpsosHelperService.getUserAssertion();

        if (obj instanceof Assertion) {
            hcpAssertion = (Assertion) obj;
        } else if (obj instanceof String) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "ASSERTION", obj.toString()));
            errorUserAssertion = (String) obj;
        }
        LOGGER.info("Searching for patients (demographics) '{}' (Identifiers) '{}'", demographics.size(), identifiers.size());
        searchPatients(hcpAssertion, identifiers, demographics, selectedCountry);
    }

    public List<PatientDocument> getPatientDocuments() {

        return patientDocuments;
    }

    public void setPatientDocuments(List<PatientDocument> patientDocuments) {
        this.patientDocuments = patientDocuments;

    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public void setSelectedPatient(Patient selectedPatient) {
        this.selectedPatient = selectedPatient;
        LiferayUtils.storeToSession("patient", selectedPatient);
    }

    public PatientDocument getSelectedDocument() {
        return selectedDocument;
    }

    public void setSelectedDocument(PatientDocument selectedDocument) {
        this.selectedDocument = selectedDocument;

    }

    public void setDocumentHtml(String documentHtml) {
        String documentHtml1 = documentHtml;
    }

    public List<PatientDocument> getPatientPrescriptions() {

        return patientPrescriptions;
    }

    public void setPatientPrescriptions(
            List<PatientDocument> patientPrescriptions) {
        this.patientPrescriptions = patientPrescriptions;
    }

    public void setPrescriptionHtml(String prescriptionHtml) {
        String prescriptionHtml1 = prescriptionHtml;
    }

    public boolean isShowDemographics() {
        return showDemographics;
    }

    public void setShowDemographics(boolean showDemographics) {
        this.showDemographics = showDemographics;
    }

    public boolean getShowPatientList() {
        return showPatientList;
    }

    public void setShowPatientList(boolean showPatientList) {
        this.showPatientList = showPatientList;
    }

    public StreamedContent getPrescriptionFile() {
        return prescriptionFile;
    }

    public void setPrescriptionFile(StreamedContent prescriptionFile) {
        this.prescriptionFile = prescriptionFile;
    }

    public PatientDocument getSelectedPrescriptionFile() {
        return selectedPrescriptionFile;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<Patient> getPatients() {

        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public List<Demographics> getDemographics() {
        return demographics;
    }

    public void setDemographics(List<Demographics> demographics) {
        this.demographics = demographics;
    }

    public Assertion getHcpAssertion() {
        return hcpAssertion;
    }

    public void setHcpAssertion(Assertion hcpAssertion) {
        this.hcpAssertion = hcpAssertion;
    }

    public Assertion getTrcAssertion() {
        return trcAssertion;
    }

    public void setTrcAssertion(Assertion trcAssertion) {
        this.trcAssertion = trcAssertion;
    }

    public String getPurposeOfUse() {
        return purposeOfUse;
    }

    public void showForm(ActionEvent actionEvent) throws SystemException {

        // create user assertion
        errorUserAssertion = "";
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(
                    "/view1.xhtml?javax.portlet.faces.PortletMode=view&amp;javax.portlet.faces.WindowState=normal");
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public boolean getShowConsent() {
        LOGGER.debug("### GET SHOW CONSENT ###");
        LOGGER.debug("trcassertionexists: '{}'", trcassertionexists);
        LOGGER.debug("consentExists: '{}'", consentExists);
        if (!trcassertionexists) {
            return false;
        }
        if (trcassertionexists) {
            if (consentExists) {
                return false;
            }
        }
        return true;
    }

    public boolean getShowConfirmation() {
        LOGGER.debug("### GET SHOW CONFIRMATION ###");
        LOGGER.debug("trcassertionexists: '{}'", trcassertionexists);
        LOGGER.debug("consentExists: '{}'", consentExists);
        return !trcassertionexists;
    }

    public boolean getShowPrescriptions() {
        return showEP;
    }

    public boolean getShowPatientSummary() {
        return showPS;
    }

    public void getMRODocs() throws ConsentException {

        consentExists = true;
        trcassertionexists = true;
        PatientId patientId = null;
        try {
            patientDocuments = new ArrayList<>();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);

            LOGGER.info("Client Connector URL: '{}'", serviceUrl);
            ClientConnectorConsumer clientConectorConsumer = MyServletContextListener.getClientConnectorConsumer();

            patientId = PatientId.Factory.newInstance();
            patientId.setExtension(selectedPatient.getExtension());
            patientId.setRoot(selectedPatient.getRoot());
            GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
            classCode.setNodeRepresentation(Constants.MRO_CLASSCODE);
            classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
            // Patient Summary ClassCode.
            classCode.setValue(Constants.MRO_TITLE);

            LOGGER.info("MRO QUERY: Getting mro documents for: " + patientId.getExtension() + " from " + selectedCountry);
            List<EpsosDocument1> queryDocuments = clientConectorConsumer.queryDocuments(hcpAssertion, trcAssertion, selectedCountry, patientId, classCode);
            LOGGER.info("MRO QUERY: Found " + queryDocuments.size() + " for : " + patientId.getExtension() + " from " + selectedCountry);

            showMRO = true;
            for (EpsosDocument1 aux : queryDocuments) {
                PatientDocument document = EpsosHelperService.populateDocument(aux, "mro");
                patientDocuments.add(document);
            }
            queryDocumentsException = LiferayUtils.getPortalTranslation("report.list.no.document");
            LOGGER.debug("Selected Country: " + LiferayUtils.getFromSession("selectedCountry"));
        } catch (Exception ex) {

            LOGGER.error(ExceptionUtils.getStackTrace(ex));
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "DOCUMENT QUERY", LiferayUtils
                            .getPortalTranslation(ex.getMessage())));
            if (patientId != null) {
                LOGGER.error("MRO QUERY: Error getting ps documents for : " + patientId.getExtension() + " from " + selectedCountry + " - " + ex.getMessage());
            }
            queryDocumentsException = LiferayUtils.getPortalTranslation(ex.getMessage());

            if (ex.getMessage().contains("4701")) {
                consentExists = false;
                throw new ConsentException();
            }
        }
    }

    public void getPSDocs() throws ConsentException {

        String runningMode = MyServletContextListener.getRunningMode();

        if (runningMode.equals("demo")) {
            patientDocuments = EpsosHelperService.getMockPSDocuments();
            consentExists = true;
            trcassertionexists = true;
            showPS = true;
        } else {
            consentExists = true;
            trcassertionexists = true;
            PatientId patientId = null;
            try {
                patientDocuments = new ArrayList<>();
                ClientConnectorConsumer clientConectorConsumer = MyServletContextListener.getClientConnectorConsumer();
                patientId = PatientId.Factory.newInstance();
                patientId.setExtension(selectedPatient.getExtension());
                patientId.setRoot(selectedPatient.getRoot());
                GenericDocumentCode classCode = GenericDocumentCode.Factory
                        .newInstance();
                classCode.setNodeRepresentation(Constants.PS_CLASSCODE);
                classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
                classCode.setValue(Constants.PS_TITLE);

                LOGGER.info("PS QUERY: Getting ps documents for : " + patientId.getExtension() + " from " + selectedCountry);
                LOGGER.info("HCP ASSERTION IS : " + hcpAssertion.getID());
                LOGGER.info("TRCA ASSERTION IS : " + trcAssertion.getID());
                LOGGER.info("selectedCountry : " + selectedCountry);
                LOGGER.info("patientId: " + patientId);
                LOGGER.info("classCode: " + classCode);

                List<EpsosDocument1> queryDocuments = clientConectorConsumer.queryDocuments(hcpAssertion, trcAssertion,
                        selectedCountry, patientId, classCode);

                LOGGER.info("PS QUERY: Found " + queryDocuments.size() + " for : " + patientId.getExtension() + " from " + selectedCountry);

                showPS = true;
                for (EpsosDocument1 aux : queryDocuments) {
                    PatientDocument document = EpsosHelperService.populateDocument(aux, "ps");
                    patientDocuments.add(document);
                }
                queryDocumentsException = LiferayUtils.getPortalTranslation("report.list.no.document");
                LOGGER.debug("Selected Country: " + LiferayUtils.getFromSession("selectedCountry"));

            } catch (Exception ex) {

                LOGGER.error(ExceptionUtils.getStackTrace(ex));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "DOCUMENT QUERY", LiferayUtils.getPortalTranslation(ex.getMessage())));
                if (patientId != null) {
                    LOGGER.error("PS QUERY: Error getting ps documents for : "
                            + patientId.getExtension() + " from " + selectedCountry
                            + " - " + ex.getMessage());
                }
                queryDocumentsException = LiferayUtils.getPortalTranslation(ex
                        .getMessage());
                if (ex.getMessage().contains("4701")) {
                    consentExists = false;
                    throw new ConsentException();
                }
            }
        }
    }

    private void createTRCA(String docType, String purposeOfUse) {

        LOGGER.info("Creating TRCAssertion for '{}' request and Purpose of Use: '{}'", docType, purposeOfUse);
        String runningMode = MyServletContextListener.getRunningMode();

        if (StringUtils.equals(docType, "ps")) {
            showPS = false;
        }
        if (StringUtils.equals(docType, "ep")) {
            showEP = false;
        }
        LOGGER.info("signedTRC: '{}'", getSignedTRC());

        PatientId patientId = null;
        try {
            patientId = PatientId.Factory.newInstance();
            patientId.setExtension(selectedPatient.getExtension());
            patientId.setRoot(selectedPatient.getRoot());
            this.purposeOfUse = purposeOfUse;
            LOGGER.info("TRCA: Creating trca for hcpAssertion : "
                    + hcpAssertion.getID() + " for patient "
                    + patientId.getRoot() + ". Purpose of use is : "
                    + purposeOfUse);
            if (runningMode.equals("demo")) {
                LOGGER.info("demo running so trca not created");
            } else if (getSignedTRC() == null) {
                trcAssertion = EpsosHelperService.createPatientConfirmationPlain(purposeOfUse, hcpAssertion, patientId);
                LOGGER.info("TRCA: Created " + trcAssertion.getID() + " for : "
                        + hcpAssertion.getID() + " for patient "
                        + patientId.getRoot() + "_" + patientId.getExtension()
                        + ". Purpose of use is : " + purposeOfUse);
            }
            trcassertionexists = true;
            trcassertionnotexists = false;
            // get patient documents
            try {
                if (docType.equals("ps")) {
                    getPSDocs();
                }
                if (docType.equals("ep")) {
                    getEPDocs();
                }
            } catch (ConsentException e) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "CONSENT ERROR", LiferayUtils
                                .getPortalTranslation(e.getMessage())));
                consentExists = false;
            } catch (Exception ex) {
                FacesContext.getCurrentInstance()
                        .addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "TRCA ERROR", LiferayUtils
                                        .getPortalTranslation(ex
                                                .getMessage())));
                consentExists = false;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "TRCA ERROR", LiferayUtils.getPortalTranslation(e.getMessage())));
            if (patientId != null) {
                LOGGER.error("TRCA: Error creating trca for patient: '{}' with hcpAssetion: '{}'. " +
                                "Purpose of use is: '{} - '{}", patientId.getExtension(), hcpAssertion.getID(), purposeOfUse,
                        e.getMessage(), e);
            }
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            trcassertionexists = false;
            trcassertionnotexists = true;
            queryDocumentsException = LiferayUtils.getPortalTranslation(e.getMessage());
        }
    }

    public void setPurposeOfUseForConsent(String purposeOfUse) {
        createTRCA("consent", purposeOfUse);
    }

    public void setPurposeOfUseForGeneric(String purposeOfUse) {
        createTRCA("generic", purposeOfUse);
    }

    private void saveConsent(ActionEvent actionEvent, String docType) {
        String consentCode = "";
        String consentDisplayName = "";

        LOGGER.info("Consent Start Date: " + consentStartDate);
        LOGGER.info("Consent End Date: " + consentEndDate);
        LOGGER.info("Consent Opt: " + consentOpt);

        String consentStartDateStr = new SimpleDateFormat("yyyyMMdd")
                .format(consentStartDate);
        String consentEndDateStr = new SimpleDateFormat("yyyyMMdd")
                .format(consentEndDate);

        if (consentOpt.equals("1")) {
            consentCode = "1.3.6.1.4.1.12559.11.10.1.3.2.4.1.1";
            consentDisplayName = "Opt-in";
        }
        if (consentOpt.equals("2")) {
            consentCode = "1.3.6.1.4.1.12559.11.10.1.3.2.4.1.2";
            consentDisplayName = "Opt-out";
        }

        Patient patient = selectedPatient;
        String consent = EpsosHelperService.createConsent(patient, consentCode,
                consentDisplayName, consentStartDateStr, consentEndDateStr);

        SubmitDocumentResponse resp = null;
        try {
            resp = submitConsent(consent);
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO", resp
                            .toString()));
            // consentExists=true;
            // showPS=true;
            // showEP=true;
        } catch (Exception e) {
            LOGGER.error("CONSENT: Error submitting consent for patient : "
                    + patient.getRoot() + " with hcpAssetion : "
                    + hcpAssertion.getID() + " - " + e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            LOGGER.error(null, resp);
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR",
                            LiferayUtils.getPortalTranslation(e.getMessage())));
            consentExists = false;
        }

        try {
            if (docType.equals("ps")) {
                getPSDocs();
            }
            if (docType.equals("ep")) {
                getEPDocs();
            }
            if (docType.equals("mro")) {
                getMRODocs();
            }
        } catch (ConsentException e) {
            consentExists = false;
            showPS = false;
            showEP = false;
        }

    }

    public void saveConsentPS(ActionEvent actionEvent) throws SystemException {
        saveConsent(actionEvent, "ps");
    }

    public void saveConsentEP(ActionEvent actionEvent) throws SystemException {
        saveConsent(actionEvent, "ep");
    }

    public void saveConsentMRO(ActionEvent actionEvent) throws SystemException {
        saveConsent(actionEvent, "mro");
    }

    public void saveConsentOther(ActionEvent actionEvent)
            throws SystemException {
        saveConsent(actionEvent, "other");
    }

    private SubmitDocumentResponse submitConsent(String xml) {

        String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
        serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);

        ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();

        GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
        classCode.setNodeRepresentation(Constants.CONSENT_CLASSCODE);
        classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
        classCode.setValue(Constants.CONSENT_TITLE);

        GenericDocumentCode formatCode = GenericDocumentCode.Factory.newInstance();
        formatCode.setSchema(IheConstants.CONSENT_FORMATCODE_CODINGSCHEMA);
        formatCode.setNodeRepresentation(IheConstants.CONSENT_FORMATCODE_NODEREPRESENTATION);
        formatCode.setValue(IheConstants.CONSENT_FORMATCODE_DISPLAYNAME);

        EpsosDocument1 document = EpsosDocument1.Factory.newInstance();
        document.setAuthor(LiferayUtils.getPortalUser().getFullName());
        Calendar cal = Calendar.getInstance();
        document.setCreationDate(cal);
        document.setDescription("Privacy Policy Acknowledgement Document");
        document.setTitle("Privacy Policy Acknowledgement Document ");
        document.setUuid(EpsosHelperService.getUniqueId());
        document.setClassCode(classCode);
        document.setFormatCode(formatCode);
        document.setBase64Binary(xml.getBytes());

        return proxy.submitDocument(hcpAssertion,
                trcAssertion, selectedCountry, document,
                selectedPatient.getPatientDemographics());
    }

    public String getPurposeOfUseForPS() {
        return purposeOfUseForPS;
    }

    public void setPurposeOfUseForPS(String purposeOfUse) {
        createTRCA("ps", purposeOfUse);
    }

    /**
     * @throws ConsentException
     */
    private void getEPDocs() throws ConsentException {

        consentExists = true;
        PatientId patientId = null;
        try {
            patientPrescriptions = new ArrayList<>();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            ClientConnectorConsumer clientConectorConsumer = MyServletContextListener.getClientConnectorConsumer();

            patientId = PatientId.Factory.newInstance();
            patientId.setExtension(selectedPatient.getExtension());
            patientId.setRoot(selectedPatient.getRoot());

            GenericDocumentCode classCode = GenericDocumentCode.Factory
                    .newInstance();
            classCode.setNodeRepresentation(Constants.EP_CLASSCODE);
            classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
            classCode.setValue(Constants.EP_TITLE); // EP

            LOGGER.info("EP QUERY: Getting ePrescription documents for: {} from {}.", patientId.getExtension(), selectedCountry);
            List<EpsosDocument1> queryDocuments = clientConectorConsumer
                    .queryDocuments(hcpAssertion, trcAssertion,
                            selectedCountry, patientId, classCode);

            LOGGER.info("EP QUERY: Found " + queryDocuments.size() + " for : "
                    + patientId.getExtension() + " from " + selectedCountry);
            showEP = true;
            for (EpsosDocument1 aux : queryDocuments) {
                PatientDocument document = EpsosHelperService.populateDocument(
                        aux, "ep");
                patientPrescriptions.add(document);
            }
            queryPrescriptionsException = LiferayUtils.getPortalTranslation(
                    "document.empty.list", FacesService.getPortalLanguage());
            LOGGER.info("Documents are " + queryDocuments.size());
        } catch (Exception ex) {

            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "QUERY DOCUMENTS", LiferayUtils
                            .getPortalTranslation(ex.getMessage(),
                                    FacesService.getPortalLanguage())));
            if (patientId != null) {
                LOGGER.error("EP QUERY: Error getting ep documents for : "
                        + patientId.getExtension() + " from " + selectedCountry
                        + " - " + ex.getMessage());
            }
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
            queryPrescriptionsException = LiferayUtils.getPortalTranslation(
                    ex.getMessage(), FacesService.getPortalLanguage());
            if (ex.getMessage().contains("4701")) {
                consentExists = false;
                throw new ConsentException();
            }
        }
    }

    public String getQueryPatientsException() {
        return queryPatientsException;
    }

    public void setQueryPatientsException(String queryPatientsException) {
        this.queryPatientsException = queryPatientsException;
    }

    public String getErrorUserAssertion() {
        return errorUserAssertion;
    }

    public void setErrorUserAssertion(String errorUserAssertion) {
        this.errorUserAssertion = errorUserAssertion;
    }

    public String getQueryDocumentsException() {
        return queryDocumentsException;
    }

    public void setQueryDocumentsException(String queryDocumentsException) {
        this.queryDocumentsException = queryDocumentsException;
    }

    public String getQueryPrescriptionsException() {
        return queryPrescriptionsException;
    }

    public void setQueryPrescriptionsException(
            String queryPrescriptionsException) {
        this.queryPrescriptionsException = queryPrescriptionsException;
    }

    public boolean istrcassertionexists() {
        return trcassertionexists;
    }

    public void settrcassertionexists(boolean trcassertionexists) {
        this.trcassertionexists = trcassertionexists;
    }

    public boolean istrcassertionnotexists() {
        return trcassertionnotexists;
    }

    public void settrcassertionnotexists(boolean trcassertionnotexists) {
        this.trcassertionnotexists = trcassertionnotexists;
    }

    public boolean isShowEP() {
        return showEP;
    }

    public void setShowEP(boolean showEP) {
        this.showEP = showEP;
    }

    public boolean isShowPS() {
        return showPS;
    }

    public void setShowPS(boolean showPS) {
        this.showPS = showPS;
    }

    public boolean isShowMRO() {
        return showMRO;
    }

    public void setShowMRO(boolean showMRO) {
        this.showMRO = showMRO;
    }

    public String getConsentOpt() {
        return consentOpt;
    }

    public void setConsentOpt(String consentOpt) {
        this.consentOpt = consentOpt;
    }

    public Date getConsentStartDate() {
        return consentStartDate;
    }

    public void setConsentStartDate(Date consentStartDate) {
        this.consentStartDate = consentStartDate;
    }

    public Date getConsentEndDate() {
        return consentEndDate;
    }

    public void setConsentEndDate(Date consentEndDate) {
        this.consentEndDate = consentEndDate;
    }

    public boolean isTrcassertionexists() {
        return trcassertionexists;
    }

    public void setTrcassertionexists(boolean trcassertionexists) {
        this.trcassertionexists = trcassertionexists;
    }

    public boolean isTrcassertionnotexists() {
        return trcassertionnotexists;
    }

    public void setTrcassertionnotexists(boolean trcassertionnotexists) {
        this.trcassertionnotexists = trcassertionnotexists;
    }

    public boolean isConsentExists() {
        return consentExists;
    }

    public void setConsentExists(boolean consentExists) {
        this.consentExists = consentExists;
    }

    /**
     * This method is placed here just to keep compliance with the presence of
     * "getters" and "setters" of the property.
     *
     * @return
     */
    public String getSpecificConsent() {
        return "No";
    }

    /**
     * The main goal of this method is to define an action to be taken by the
     * portal if the Patient does not agrees with the specific and final consent
     * (answer obtained through a last question in the work-flow). This action
     * will force the portal to submit new consent for the patient.
     *
     * @param confirmation
     */
    public void setSpecificConsent(String confirmation) {
        if (confirmation.equals("No")) {
            FacesContext
                    .getCurrentInstance()
                    .addMessage(
                            null,
                            new FacesMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    LiferayUtils
                                            .getPortalTranslation("message.error"),
                                    LiferayUtils
                                            .getPortalTranslation("consent.not.given")));
            showPS = false;
        }
    }

    public String checkConfirmationDocuments() {
        if (!trcassertionexists) {
            ExternalContext ec = FacesContext.getCurrentInstance()
                    .getExternalContext();
            ec.getRequestMap().put("selectedPatient", selectedPatient);
            return "viewPatientConfirmationForDocuments";
        }
        return "";
    }

    public String submitConfirmation() {
        createTRCA("generic", purposeOfUse);
        LOGGER.info("### Selected patient: " + selectedPatient.getFamilyName());
        ExternalContext ec = FacesContext.getCurrentInstance()
                .getExternalContext();
        ec.getRequestMap().put("trcAssertion", trcAssertion);
        return "genericPatientConfirmation";
    }

    public String getPurposeOfUseForEP() {
        return purposeOfUseForEP;
    }

    public void setPurposeOfUseForEP(String purposeOfUse) {
        createTRCA("ep", purposeOfUse);
    }

    public boolean getEnableMRO() {
        return enableMRO;
    }

    public void setEnableMRO(boolean enableMRO) {
        this.enableMRO = enableMRO;
    }

    public boolean getEnableHCER() {
        return enableHCER;
    }

    public void setEnableHCER(boolean enableHCER) {
        this.enableHCER = enableHCER;
    }

    public boolean getEnablePatientDocuments() {
        return enablePatientDocuments;
    }

    public void setEnablePatientDocuments(boolean enablePatientDocuments) {
        this.enablePatientDocuments = enablePatientDocuments;
    }

    public boolean getEnablePrescriptionDocuments() {
        return enablePrescriptionDocuments;
    }

    public void setEnablePrescriptionDocuments(
            boolean enablePrescriptionDocuments) {
        this.enablePrescriptionDocuments = enablePrescriptionDocuments;
    }

    private void checkButtonPermissions() {
        User user = LiferayUtils.getPortalUser();
        if (Validator.isNotNull(user)) {

            String checkPermissions = EpsosHelperService
                    .getConfigProperty(EpsosHelperService.PORTAL_CHECK_PERMISSIONS);
            String checkHCER = EpsosHelperService
                    .getConfigProperty(EpsosHelperService.PORTAL_HCER_ENABLED);
            boolean hcer = false;
            if (Validator.isNotNull(checkHCER)) {
                if (checkHCER.equalsIgnoreCase("true")) {
                    hcer = true;
                }
            }
            String checkMRO = EpsosHelperService
                    .getConfigProperty(EpsosHelperService.PORTAL_MRO_ENABLED);
            boolean mro = false;
            if (Validator.isNotNull(checkMRO)) {
                if (checkMRO.equalsIgnoreCase("true")) {
                    mro = true;
                }
            }

            String checkCCD = EpsosHelperService
                    .getConfigProperty(EpsosHelperService.PORTAL_CCD_ENABLED);
            boolean ccd = false;
            if (Validator.isNotNull(checkCCD)) {
                if (checkCCD.equalsIgnoreCase("true")) {
                    ccd = true;
                    enableCCD = true;
                }
            }

            String checkCONSENT = EpsosHelperService
                    .getConfigProperty(EpsosHelperService.PORTAL_CONSENT_ENABLED);
            boolean consent = false;
            if (Validator.isNotNull(checkCONSENT)) {
                if (checkCONSENT.equalsIgnoreCase("true")) {
                    consent = true;
                    enableCONSENT = true;
                } else {
                    consent = false;
                    enableCONSENT = false;
                }
            }

            if (Validator.isNotNull(checkPermissions)) {
                if (checkPermissions.equalsIgnoreCase("false")) {
                    enableMRO = true;
                    enableHCER = true;
                    enablePatientDocuments = true;
                    enablePrescriptionDocuments = true;
                    enableCONSENT = true;
                    return;
                }
            }
            boolean isPhysician = LiferayUtils.isDoctor(user.getUserId(),
                    user.getCompanyId());
            boolean isPharmacist = LiferayUtils.isPharmacist(user.getUserId(),
                    user.getCompanyId());
            boolean isNurse = LiferayUtils.isNurse(user.getUserId(),
                    user.getCompanyId());
            boolean isAdministrator = LiferayUtils.isAdministrator(
                    user.getUserId(), user.getCompanyId());

            if (isNurse) {
                enableMRO = false;
                enableHCER = false;
                enablePatientDocuments = false;
                enablePrescriptionDocuments = true;
            } else if (isPhysician) {
                enableMRO = true && mro;
                enableHCER = true && hcer;
                enablePatientDocuments = true;
                enablePrescriptionDocuments = false;
            } else if (isPharmacist) {
                enableMRO = false;
                enableHCER = false;
                enablePatientDocuments = false;
                enablePrescriptionDocuments = true;
            } else if (isAdministrator) {
                enableMRO = false;
                enableHCER = false;
                enablePatientDocuments = false;
                enablePrescriptionDocuments = false;
            } else {
                enableMRO = false;
                enableHCER = false;
                enablePatientDocuments = false;
                enablePrescriptionDocuments = false;
            }

            canConvert = false;
            try {
                String canConvertToCCD = PropsUtil.get("can.convert.to.ccd");
                if (Validator.isNotNull(canConvertToCCD)) {
                    if (canConvertToCCD.equalsIgnoreCase("true")) {
                        canConvert = true;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error getting property for can convert to ccd");
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public boolean isCanConvert() {
        return canConvert;
    }

    public void setCanConvert(boolean canConvert) {
        this.canConvert = canConvert;
    }

    public boolean getEnableCCD() {
        return enableCCD;
    }

    public void setEnableCCD(boolean enableCCD) {
        this.enableCCD = enableCCD;
    }

    public boolean getEnableCONSENT() {
        return enableCONSENT;
    }

    public void setEnableCONSENT(boolean enableCONSENT) {
        this.enableCONSENT = enableCONSENT;
    }

    public String getCdaStylesheet() {
        return cdaStylesheet;
    }

    public void setCdaStylesheet(String cdaStylesheet) {
        this.cdaStylesheet = cdaStylesheet;
    }

    public String getSignedTRC() {
        return signedTRC;
    }

    public void setSignedTRC(String signedTRC) throws Exception {

        LOGGER.info("signedTRC: '{}'", signedTRC);
        if (signedTRC != null && !signedTRC.isEmpty()) {
            // Initialize the library
            DefaultBootstrap.bootstrap();

            // Get parser pool manager
            BasicParserPool ppMgr = new BasicParserPool();
            ppMgr.setNamespaceAware(true);

            // Parse metadata file
            InputStream in = new ByteArrayInputStream(signedTRC.getBytes("UTF-8"));
            Document inCommonMDDoc = ppMgr.parse(in);
            Element metadataRoot = inCommonMDDoc.getDocumentElement();

            // Get apropriate unmarshaller
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);

            // Unmarshall using the document root element, an EntitiesDescriptor in this case
            trcAssertion = (Assertion) unmarshaller.unmarshall(metadataRoot);

            LOGGER.info("TRCA '{}' with ID: '{}'", trcAssertion, trcAssertion.getID());
            LiferayUtils.storeToSession("trcAssertion", trcAssertion);
            this.signedTRC = signedTRC;
        }
    }
}
