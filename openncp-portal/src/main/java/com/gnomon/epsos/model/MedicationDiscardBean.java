package com.gnomon.epsos.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.MyServletContextListener;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.model.User;

import epsos.openncp.protocolterminator.ClientConnectorConsumer;
import epsos.openncp.protocolterminator.clientconnector.EpsosDocument1;
import epsos.openncp.protocolterminator.clientconnector.GenericDocumentCode;
import epsos.openncp.protocolterminator.clientconnector.PatientId;
import epsos.openncp.protocolterminator.clientconnector.SubmitDocumentResponse;
import eu.epsos.util.IheConstants;
import tr.com.srdc.epsos.util.Constants;

@ManagedBean
@RequestScoped
public class MedicationDiscardBean implements Serializable {
	private static final long serialVersionUID = -1L;

	private static final Logger logger = LoggerFactory.getLogger(MedicationDiscardBean.class);

	public String discardDispense(String medicationFile) throws IOException {
		try {

			logger.info("[Portal] Discard Dispense Operation {}", medicationFile);
			
			String documentId = medicationFile;
			File file = loadMedication(documentId);
			byte[] fileContent = Files.readAllBytes(file.toPath());

			User user = (User) LiferayUtils.getFromSession("user");
			Patient patient = (Patient) LiferayUtils.getFromSession("patient");
			String selectedCountry = (String) LiferayUtils.getFromSession("selectedCountry");
			// Checking validity of the assertions (HCP and TRC)
			Assertion hcpAssertion = (Assertion) LiferayUtils.getFromSession("hcpAssertion");
			logger.info("HCP Assertion ID: '{}'", hcpAssertion.getID());
			Assertion trcAssertion = (Assertion) LiferayUtils.getFromSession("trcAssertion");
			logger.info("TRC Assertion ID: '{}'",
					trcAssertion != null ? hcpAssertion.getID() : "N/A - TRC ticket requested");
			if (trcAssertion == null) {
				PatientId patientId = PatientId.Factory.newInstance();
				patientId.setExtension(patient.getExtension());
				patientId.setRoot(patient.getRoot());
				createPatientConfirmationPlain(hcpAssertion, patientId);
			}
			String eDUid = generateIdentifierExtension();
			String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
			EpsosDocument1 document = buildDispenseDocument(user, edOid, eDUid, fileContent);
			ClientConnectorConsumer proxy = MyServletContextListener.getClientConnectorConsumer();
			String trcAssertionId = Optional.ofNullable(trcAssertion).map(Assertion::getID).orElse(null);
			
			logger.info("Discard Operation Debug: '{}', '{}', '{}', '{}', '{}'", hcpAssertion.getID(),
					trcAssertionId, selectedCountry, document.getRepositoryId(), patient.getRoot());
			SubmitDocumentResponse submitDocumentResponse = proxy.submitDocument(hcpAssertion, trcAssertion,
					selectedCountry, document, patient.getPatientDemographics());
			addMessage("Discard dispense", "The dispense is discarded successfully.", false);
		} catch (Exception e) {
			logger.error("Discard dispense error", e);
			addMessage("Discard dispense error", "Please contact a system administrator.", true);
		}
		return null;
	}

	private void createPatientConfirmationPlain(Assertion hcpAssertion, PatientId patientId) {
		try {
			EpsosHelperService.createPatientConfirmationPlain(hcpAssertion, patientId, "TREATMENT");
		} catch (Exception e) {
			logger.error("Exception: '{}'", e.getMessage());
		}
	}

	/**
	 * Generates UID extension required by the eDispense.
	 *
	 * @return a String formatted UID required by CDA eD ID extension.
	 */
	private String generateIdentifierExtension() {

		Random r = new SecureRandom();
		byte[] b = new byte[16];
		r.nextBytes(b);
		String s = Base64.encodeBase64String(b);
		return s.substring(0, 16);
	}

	/**
	 * @param dispenseRoot
	 * @param dispenseExtension
	 * @param dispense
	 * @return
	 */
	private EpsosDocument1 buildDispenseDocument(User user, String dispenseRoot, String dispenseExtension,
			byte[] dispense) {

		GenericDocumentCode classCode = GenericDocumentCode.Factory.newInstance();
		classCode.setNodeRepresentation(Constants.EDD_CLASSCODE);
		classCode.setSchema(IheConstants.ClASSCODE_SCHEME);
		classCode.setValue(Constants.ED_TITLE);

		// <formatCode>
		// <nodeRepresentation>urn:epsos:ep:dis:2010</nodeRepresentation>
		// <schema>epSOS formatCodes</schema>
		// <value>epSOS coded eDispensation</value>
		// </formatCode>

		// public static final String DISPLAY_NAME = "eHDSI coded eDispensation
		// Discard";
		// public static final String NODE_REPRESENTATION = "urn:eHDSI:ed:discard:2020";
		// public static final String CODING_SCHEME = "eHDSI formatCodes";
		GenericDocumentCode formatCode = GenericDocumentCode.Factory.newInstance();
		formatCode.setSchema("eHDSI formatCodes");
		formatCode.setNodeRepresentation("urn:eHDSI:ed:discard:2020");
		formatCode.setValue("eHDSI coded eDispensation Discard");

		EpsosDocument1 document = EpsosDocument1.Factory.newInstance();
		document.setAuthor(user.getFullName());
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		Calendar calendar = Calendar.getInstance(timeZone);
		document.setCreationDate(calendar);
		document.setDescription(Constants.ED_TITLE);
		document.setTitle(Constants.ED_TITLE);
		document.setUuid(dispenseRoot + "^" + dispenseExtension);
		document.setSubmissionSetId(EpsosHelperService.getUniqueId());
		document.setClassCode(classCode);
		document.setFormatCode(formatCode);
		document.setBase64Binary(dispense);

		return document;
	}

	private File loadMedication(String documentId) {
		String directoryName = Constants.EPSOS_PROPS_PATH + "integration/" + Constants.HOME_COMM_ID + "/medication/"
				+ documentId;
		return new File(directoryName);
	}

	public void addMessage(String summary, String detail, boolean isError) {
		FacesMessage message = new FacesMessage(isError ? FacesMessage.SEVERITY_ERROR: FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}
