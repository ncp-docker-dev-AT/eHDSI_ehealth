package com.gnomon.epsos.model;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.service.ConsentException;
import com.gnomon.epsos.service.Demographics;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.HCPIAssertionCreator;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import epsos.openncp.protocolterminator.clientconnector.PatientDemographics;
import epsos.openncp.protocolterminator.clientconnector.PatientId;
import eu.epsos.util.IheConstants;
import eu.europa.ec.joinup.ecc.openstork.utils.StorkUtils;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.*;

@ManagedBean
@SessionScoped
public class PacBean implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacBean.class);

    private static final long serialVersionUID = 1L;
    private String selectedCountry;
    private List<Identifier> identifiers;
    private List<Demographics> demographics;
    private List<Patient> patients;
    private Patient selectedPatient;
    private List<PatientDocument> patientDocuments;
    private PatientDocument selectedDocument;
    private boolean showDemographics;
    private boolean showPatientList = true;
    private StreamedContent prescriptionFile;
    private PatientDocument selectedPrescriptionFile;
    private Assertion hcpAssertion = null;
    private boolean trcAssertionExists = false;
    private boolean trcAssertionNotExists = true;
    private Assertion trcAssertion;
    private String purposeOfUse;
    private String purposeOfUseForPS;
    private String queryPatientsException;
    private String queryDocumentsException;
    private boolean showPS;
    private boolean consentExists;
    private Map<String, String> ltrlanguages = new HashMap<>();
    private String ltrlang;
    private boolean hasPacToOther;
    private boolean hasPac;
    private String errorUserAssertion;
    private Map<String, String> onbehalfdemographicsattrs;

    public PacBean() {

        STORKSAMLEngine engine;
        String host;
        String userSamlReponseXml;

        try {
            engine = STORKSAMLEngine.getInstance("SP");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            PortletRequest portletRequest = (PortletRequest) externalContext.getRequest();
            HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(portletRequest);

            host = httpServletRequest.getRemoteHost();
            LOGGER.info("HOST IS: '{}'", host);

            userSamlReponseXml = (String) LiferayUtils.getFromSession("USER_samlResponseXML");


            STORKAuthnResponse authnResponse = engine.validateSTORKAuthnResponse(userSamlReponseXml.getBytes(), host);
            onbehalfdemographicsattrs = StorkUtils.getRepresentedDemographics(authnResponse);
            String givenName = onbehalfdemographicsattrs.get("patient.data.surname");
            LOGGER.info("REP GIVEN NAME IS: '{}'", givenName);
            hasPacToOther = Validator.isNotNull(givenName);
            LOGGER.info("PATIENT HAS ACCESS TO OTHER: '{}'", hasPacToOther);


            selectedCountry = ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.country");
            LOGGER.info("Selected Country: '{}'", selectedCountry);
            identifiers = new ArrayList<>();
            demographics = new ArrayList<>();
            ltrlanguages = EpsosHelperService.getLTRLanguages();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Country", selectedCountry));
            getPACDocuments();

        } catch (Exception e) {
            LOGGER.error("Error starting Stork engine");
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(String selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public void displayLocation() {
        FacesMessage msg = new FacesMessage("Selected", "City:" + ltrlang);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void getPACDocuments() {

        LOGGER.info("HCP Assertion is going to be created");
        errorUserAssertion = "";
        Object obj = EpsosHelperService.getUserAssertion();
        LOGGER.info("User Assertion: '{}'", obj);
        if (obj instanceof Assertion) {
            hcpAssertion = (Assertion) obj;
        } else if (obj instanceof String) {
            errorUserAssertion = (String) obj;
        }
        LOGGER.info("HCP Assertion has been created");
        User user = LiferayUtils.getPortalUser();
        LiferayUtils.storeToSession("user", user);

        identifiers = EpsosHelperService.getCountryIdentifiers(selectedCountry,
                LiferayUtils.getPortalLanguage(), null, user);

        demographics = EpsosHelperService.getCountryDemographics(selectedCountry,
                LiferayUtils.getPortalLanguage(), null, user);

        LOGGER.info("Selected Country: '{}'", selectedCountry);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Country", selectedCountry));
        LiferayUtils.storeToSession("selectedCountry", selectedCountry);
        LOGGER.info("Get my patient info (Start)");
        getMyPatientInfo();
        LOGGER.info("Get my patient info (End)");

        if (Validator.isNotNull(selectedPatient)) {
            LOGGER.info("Create TRCA (Start)");
            createTRCA("ps", "TREATMENT");
            LOGGER.info("Create TRCA (End)");
            try {
                LOGGER.info("Get PS Docs (Start)");
                getPSDocs();
                LOGGER.info("Get PS Docs (End)");
            } catch (ConsentException e) {
                queryDocumentsException = LiferayUtils.getPortalTranslation(e.getMessage());
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void getMyPatientInfo() {

        try {
            trcAssertion = null;
            trcAssertionNotExists = true;
            trcAssertionExists = false;
            patients = new ArrayList<>();

            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            PatientDemographics pd = EpsosHelperService.createPatientDemographicsForQuery(identifiers, demographics);

            LOGGER.info("Running client connector: '{}'", serviceUrl);
            ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();
            LOGGER.info("Test Assertions: '{}'", EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_TEST_ASSERTIONS));
            boolean testAssertion = GetterUtil.getBoolean(
                    EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_TEST_ASSERTIONS), true);
            Assertion ass;
            if (testAssertion) {
                ass = new HCPIAssertionCreator().createHCPIAssertion();
            } else {
                ass = hcpAssertion;
            }
            LOGGER.info("Searching for patients in '{}'", selectedCountry);
            LOGGER.info("Assertion id: '{}'", ass.getID());
            try {
                LOGGER.info("Patient Demographic: '{}'", pd.getPatientIdArray(0));
            } catch (Exception e) {
                LOGGER.error("No patient identifier declared - {}'", e.getMessage());
            }

            List<PatientDemographics> queryPatient = proxy.queryPatient(ass, selectedCountry, pd);
            int i = 0;
            for (PatientDemographics aux : queryPatient) {
                Patient patient = EpsosHelperService.populatePatient(aux);
                LOGGER.info("PATIENT FOUND: '{}'", patient.getExtension());
                selectedPatient = patient;
                i++;
                if (i > 1) {
                    selectedPatient = null;
                    LOGGER.error("More than one patients exist with these criteria. Exiting ...");
                    return;
                }
            }
            queryPatientsException = LiferayUtils.getPortalTranslation("patient.list.no.patient");
            LOGGER.info("Found '{}' patients", i);
            showPatientList = true;
        } catch (Exception ex) {
            LOGGER.error(ExceptionUtils.getStackTrace(ex));
            patients = new ArrayList<>();
            showPatientList = true;
            queryPatientsException = LiferayUtils.getPortalTranslation(ex.getMessage());
        }
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

    public boolean getDisablePatientDocuments() {

        User user = LiferayUtils.getPortalUser();

        boolean isPhysician = LiferayUtils.isDoctor(user.getUserId(),
                user.getCompanyId());
        boolean isPharmacist = LiferayUtils.isPharmacist(user.getUserId(),
                user.getCompanyId());
        boolean isNurse = LiferayUtils.isNurse(user.getUserId(),
                user.getCompanyId());
        boolean isAdministrator = LiferayUtils.isAdministrator(
                user.getUserId(), user.getCompanyId());

        if (isNurse) {
            return false;
        } else if (isPhysician) {
            return false;
        } else if (isPharmacist) {
            return true;
        } else if (isAdministrator) {
            return false;
        } else {
            return false;
        }
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

    public void setSelectedPrescriptionFile(PatientDocument selectedPrescriptionFile) {
        this.selectedPrescriptionFile = selectedPrescriptionFile;
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

    public void setPurposeOfUse(String purposeOfUse) {
        this.purposeOfUse = purposeOfUse;
    }

    public boolean getShowConfirmation() {

        LOGGER.debug("### GET SHOW CONFIRMATION ###");
        LOGGER.debug("trcAssertionExists: '{}'", trcAssertionExists);
        LOGGER.debug("consentExists: '{}'", consentExists);
        return !trcAssertionExists;
    }

    public void getPSDocs() throws ConsentException {

        consentExists = true;
        trcAssertionExists = true;
        PatientId patientId = null;
        try {
            patientDocuments = new ArrayList<>();
            String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);
            LOGGER.info("Client Connector URL: '{}'", serviceUrl);
            ClientConnectorConsumer clientConnectorConsumer = MyServletContextListener.getClientConnectorConsumer();

            patientId = PatientId.Factory.newInstance();
            patientId.setExtension(selectedPatient.getExtension());
            patientId.setRoot(selectedPatient.getRoot());

            // Patient Summary ClassCode.
            GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
            classCode.setNodeRepresentation(Constants.PS_CLASSCODE);
            classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
            classCode.setValue(Constants.PS_TITLE);

            LOGGER.info("PS QUERY: Getting ps documents for: '{}' from '{}'", patientId.getExtension(), selectedCountry);
            List<EpsosDocument1> queryDocuments = clientConnectorConsumer.queryDocuments(hcpAssertion, trcAssertion, selectedCountry, patientId, classCode);
            LOGGER.info("PS QUERY: Found '{}' for: '{}' from '{}", queryDocuments.size(), patientId.getExtension(), selectedCountry);
            showPS = true;
            consentExists = true;
            for (EpsosDocument1 aux : queryDocuments) {
                PatientDocument document = new PatientDocument();
                document.setAuthor(aux.getAuthor());
                Calendar cal = aux.getCreationDate();
                DateFormat sdf = LiferayUtils.getPortalUserDateFormat();
                try {
                    document.setCreationDate(sdf.format(cal.getTime()));
                } catch (Exception e) {
                    LOGGER.error("Problem converting date '{}'", aux.getCreationDate());
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                }
                document.setDescription(aux.getDescription());
                document.setHealthcareFacility("");
                document.setTitle(aux.getTitle());
                document.setFile(aux.getBase64Binary());
                document.setUuid(URLEncoder.encode(aux.getUuid(), "UTF-8"));
                document.setFormatCode(aux.getFormatCode());
                document.setRepositoryId(aux.getRepositoryId());
                document.setHcid(aux.getHcid());
                patientDocuments.add(document);
                hasPac = true;
            }
            queryDocumentsException = LiferayUtils.getPortalTranslation("report.list.no.document");
        } catch (Exception ex) {
            hasPac = false;
            consentExists = true;
            LOGGER.error(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "DOCUMENT QUERY", ex.getMessage()));
            LOGGER.error("PS QUERY: Error getting ps documents for: '{}' from '{}' - '{}'", patientId.getExtension(), selectedCountry, ex.getMessage());
            queryDocumentsException = LiferayUtils.getPortalTranslation(ex.getMessage());
            if (ex.getMessage().contains("4701")) {
                consentExists = false;
                throw new ConsentException();
            }
        }
    }

    private void createTRCA(String docType, String purposeOfUse) {

        if (docType.equals("ps")) {
            showPS = false;
        }
        LOGGER.info("TRCA: Starting setting the purpose of use: '{}'", purposeOfUse);
        PatientId patientId = null;
        try {
            patientId = PatientId.Factory.newInstance();
            patientId.setExtension(selectedPatient.getExtension());
            patientId.setRoot(selectedPatient.getRoot());
            this.purposeOfUse = purposeOfUse;
            LOGGER.info("TRCA: Creating trca for hcpAssertion: '{}' for patient '{}'. Purpose of use is: '{}'",
                    hcpAssertion.getID(), patientId.getRoot(), purposeOfUse);
            trcAssertion = EpsosHelperService.createPatientConfirmationPlain(
                    purposeOfUse, hcpAssertion, patientId);
            LOGGER.info("TRCA: Created '{}' for: '{}' for patient '{}_{}'. Purpose of use is: '{}'",
                    trcAssertion.getID(), hcpAssertion.getID(), patientId.getRoot(), patientId.getExtension(), purposeOfUse);
            trcAssertionExists = true;
            trcAssertionNotExists = false;
            // get patient documents
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "TRCA ERROR", e.getMessage()));
            if (patientId != null) {
                LOGGER.error("TRCA: Error creating trca for patient: '{}' with hcpAssetion: '{}'. Purpose of use is: '{}' - '{}'",
                        patientId.getExtension(), hcpAssertion.getID(), purposeOfUse, e.getMessage());
            }
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            trcAssertionExists = false;
            trcAssertionNotExists = true;
            queryDocumentsException = LiferayUtils.getPortalTranslation(e.getMessage());
        }
    }

    public void setPurposeOfUseForEP(String purposeOfUse) {
        createTRCA("ep", purposeOfUse);
    }

    public String getPurposeOfUseForPS() {
        return purposeOfUseForPS;
    }

    public void setPurposeOfUseForPS(String purposeOfUse) {
        createTRCA("ps", purposeOfUse);
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

    public boolean isTrcAssertionExists() {
        return trcAssertionExists;
    }

    public void setTrcAssertionExists(boolean trcAssertionExists) {
        this.trcAssertionExists = trcAssertionExists;
    }

    public boolean isTrcAssertionNotExists() {
        return trcAssertionNotExists;
    }

    public void setTrcAssertionNotExists(boolean trcAssertionNotExists) {
        this.trcAssertionNotExists = trcAssertionNotExists;
    }

    public boolean isShowPS() {
        return showPS;
    }

    public void setShowPS(boolean showPS) {
        this.showPS = showPS;
    }

    public boolean isConsentExists() {
        return consentExists;
    }

    public void setConsentExists(boolean consentExists) {
        this.consentExists = consentExists;
    }

    public Map<String, String> getLtrlanguages() {
        return ltrlanguages;
    }

    public void setLtrlanguages(Map<String, String> ltrlanguages) {
        this.ltrlanguages = ltrlanguages;
    }

    public String getLtrlang() {
        return ltrlang;
    }

    public void setLtrlang(String ltrlang) {
        this.ltrlang = ltrlang;
    }

    public boolean getHasPacToOther() {
        return hasPacToOther;
    }

    public void setHasPacToOther(boolean hasPacToOther) {
        this.hasPacToOther = hasPacToOther;
    }

    public Map<String, String> getOnbehalfdemographicsattrs() {
        return onbehalfdemographicsattrs;
    }

    public void setOnbehalfdemographicsattrs(Map<String, String> onbehalfdemographicsattrs) {
        this.onbehalfdemographicsattrs = onbehalfdemographicsattrs;
    }

    public boolean getHasPac() {
        return hasPac;
    }

    public void setHasPac(boolean hasPac) {
        this.hasPac = hasPac;
    }

}
