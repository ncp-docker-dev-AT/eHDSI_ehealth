package eu.epsos.protocolterminators.ws.server.xdr.impl;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.protocolterminators.ws.server.exception.NationalInfrastructureException;
import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.epsos.protocolterminators.ws.server.xdr.DocumentSubmitInterface;
import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;
import eu.europa.ec.sante.ehdsi.openncp.model.DiscardDispenseDetails;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import fi.kela.se.epsos.data.model.ConsentDocumentMetaData;
import fi.kela.se.epsos.data.model.DocumentAssociation;
import fi.kela.se.epsos.data.model.EPSOSDocument;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.PrettyPrinter;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.transform.TransformerException;

/**
 * Mock implementation of the DocumentSubmitInterface, to be replaced nationally.
 *
 * @author danielgronberg
 * @author Konstantin.Hypponen@kela.fi
 */
public class DocumentSubmitMockImpl extends NationalConnectorGateway implements DocumentSubmitInterface {

    private final Logger logger = LoggerFactory.getLogger(DocumentSubmitMockImpl.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    public DocumentSubmitMockImpl() {
        logger.info("Instantiating DocumentSubmitMockImpl");
    }

    /**
     * Stores a dispensation in the national infrastructure
     *
     * @param dispensationDocument eDispensation document in epSOS pivot (CDA) form
     */
    @Override
    public void submitDispensation(EPSOSDocument dispensationDocument) throws NIException {

        if (logger.isInfoEnabled()) {
            logger.info("[National Infrastructure Mock] Submit Dispense Document for Patient: '{}'", dispensationDocument.getPatientId());
        }
        String dispensation;
        try {
            dispensation = XMLUtil.prettyPrint(dispensationDocument.getDocument().getFirstChild());
        } catch (TransformerException e) {
            logger.error("TransformerException while submitDispensation(): '{}'", e.getMessage(), e);
            throw new NationalInfrastructureException(OpenNCPErrorCode.ERROR_EP_ALREADY_DISPENSED, null);
        }
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            loggerClinical.debug("eDispensation document content: '{}'", dispensation);
        }

        if (dispensation == null || dispensation.isEmpty()) {

            throw new NationalInfrastructureException(OpenNCPErrorCode.ERROR_EP_ALREADY_DISPENSED, null);
        }

        if (StringUtils.contains(dispensation, "NO_MATCHING_EP")) {

            logger.error("Tried to submit dispensation with no matching ePrescription.");
            throw new NationalInfrastructureException(OpenNCPErrorCode.ERROR_EP_NOT_MATCHING, null);
        }

        if (StringUtils.contains(dispensation, "INVALID_DISPENSE")) {

            logger.error("Tried to submit already dispensed ePrescription.");
            throw new NationalInfrastructureException(OpenNCPErrorCode.ERROR_EP_ALREADY_DISPENSED, null);
        }
    }

    /**
     * Discards a previously submitted dispensation
     *
     * @param dispensationToDiscard Id of the dispensation to be discarded
     */
    @Override
    public void cancelDispensation(DiscardDispenseDetails discardDispenseDetails, EPSOSDocument dispensationToDiscard) throws NIException {

        if (logger.isInfoEnabled()) {
            logger.info("[National Infrastructure Mock] Submit Discard Dispense Document");
        }
        logger.info("eDispensation to be discarded: '{}' for Patient: '{}'", dispensationToDiscard.getClassCode(), dispensationToDiscard.getPatientId());
        logger.info("Discard Dispense ID: '{}' for ePrescription ID: '{}' operation executed...\n'{}'",
                discardDispenseDetails.getDiscardId(), discardDispenseDetails.getDispenseId(), discardDispenseDetails);
    }

    /**
     * Discards a previously submitted consent
     *
     * @param consentToDiscard Metadata of the consent to be discarded (XML and PDF versions)
     */
    @Override
    public void cancelConsent(DocumentAssociation<ConsentDocumentMetaData> consentToDiscard) {

        if (logger.isInfoEnabled()) {
            logger.info("[National Infrastructure Mock] Discard Consent Document");
        }
        logger.info("Consent to be discarded: '{}'", consentToDiscard.getXMLDocumentMetaData().getId());
    }

    /**
     * Stores a patient consent in the national infrastructure
     *
     * @param consentDocument consent document in epSOS pivot (CDA) form
     */
    @Override
    public void submitPatientConsent(EPSOSDocument consentDocument) throws NIException {

        if (logger.isInfoEnabled()) {
            logger.info("[National Infrastructure Mock] Submit Consent Document");
        }
        String consent;
        try {
            consent = PrettyPrinter.prettyPrint(consentDocument.getDocument());
        } catch (TransformerException e) {
            logger.error("TransformerException: '{}'", e.getMessage(), e);
            throw new DocumentProcessingException("Cannot parse consent!");
        }
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            loggerClinical.debug("Patient consent content: '{}'", consent);
        }
    }

    @Override
    public void submitHCER(EPSOSDocument hcerDocument) throws DocumentProcessingException {

        if (logger.isInfoEnabled()) {
            logger.info("[National Infrastructure Mock] Submit Health Care Encounter Report");
        }
        String consent;
        try {
            consent = PrettyPrinter.prettyPrint(hcerDocument.getDocument());
        } catch (TransformerException e) {
            logger.error("TransformerException: '{}'", e.getMessage(), e);
            throw new DocumentProcessingException("Cannot parse HCER!");
        }
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            loggerClinical.debug("HCER document content: '{}'", consent);
        }
    }
}
