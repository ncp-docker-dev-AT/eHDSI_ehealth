package eu.epsos.protocolterminators.ws.server.xdr.impl;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.protocolterminators.ws.server.exception.NationalInfrastructureException;
import eu.epsos.protocolterminators.ws.server.exception.XDSErrorCode;
import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.epsos.protocolterminators.ws.server.xdr.DocumentSubmitInterface;
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

        String dispensation;
        try {
            dispensation = XMLUtil.prettyPrint(dispensationDocument.getDocument().getFirstChild());
        } catch (TransformerException e) {
            logger.error("TransformerException while submitDispensation(): '{}'", e.getMessage(), e);
            throw new NationalInfrastructureException(XDSErrorCode.INVALID_DISPENSE);
        }
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            loggerClinical.debug("eDispensation document content: '{}'", dispensation);
        }

        if (dispensation == null || dispensation.isEmpty()) {

            throw new NationalInfrastructureException(XDSErrorCode.INVALID_DISPENSE);
        }

        if (StringUtils.contains(dispensation, "NO_MATCHING_EP")) {

            logger.error("Tried to submit dispensation with no matching ePrescription.");
            throw new NationalInfrastructureException(XDSErrorCode.NO_MATCHING_PRESCRIPTION);
        }

        if (StringUtils.contains(dispensation, "INVALID_DISPENSE")) {

            logger.error("Tried to submit already dispensed ePrescription.");
            throw new NationalInfrastructureException(XDSErrorCode.INVALID_DISPENSE);
        }
    }

    /**
     * Discards a previously submitted dispensation
     *
     * @param dispensationToDiscard Id of the dispensation to be discarded
     */
    @Override
    public void cancelDispensation(DiscardDispenseDetails discardDispenseDetails, EPSOSDocument dispensationToDiscard) {

        logger.info("eDispensation to be discarded: '{}' for Patient: '{}'", dispensationToDiscard.getClassCode(), dispensationToDiscard.getPatientId());
        logger.info("[National Connector A] Discard Dispense ID: '{}' for ePrescription ID: '{}' operation executed...\n'{}'",
                discardDispenseDetails.getDiscardId(), discardDispenseDetails.getDispenseId(), discardDispenseDetails);
    }

    /**
     * Discards a previously submitted consent
     *
     * @param consentToDiscard Metadata of the consent to be discarded (XML and PDF versions)
     */
    @Override
    public void cancelConsent(DocumentAssociation<ConsentDocumentMetaData> consentToDiscard) {
        logger.info("Consent to be discarded: '{}'", consentToDiscard.getXMLDocumentMetaData().getId());
    }

    /**
     * Stores a patient consent in the national infrastructure
     *
     * @param consentDocument consent document in epSOS pivot (CDA) form
     */
    @Override
    public void submitPatientConsent(EPSOSDocument consentDocument) throws NIException {

        String consent = null;
        try {
            consent = PrettyPrinter.prettyPrint(consentDocument.getDocument());
        } catch (TransformerException e) {
            logger.error("TransformerException: '{}'", e.getMessage(), e);
            throwDocumentProcessingException("Cannot parse consent!", "4106");
        }
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            loggerClinical.debug("Patient consent content: '{}'", consent);
        }
    }

    private void throwDocumentProcessingException(String message, String code) throws DocumentProcessingException {

        DocumentProcessingException documentProcessingException = new DocumentProcessingException();
        documentProcessingException.setMessage(message);
        documentProcessingException.setCode(code);
        throw documentProcessingException;
    }

    @Override
    public void submitHCER(EPSOSDocument hcerDocument) throws DocumentProcessingException {

        String consent = null;
        try {
            consent = PrettyPrinter.prettyPrint(hcerDocument.getDocument());
        } catch (TransformerException e) {
            logger.error("TransformerException: '{}'", e.getMessage(), e);
            throwDocumentProcessingException("Cannot parse HCER!", "4106");
        }
        if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
            loggerClinical.debug("HCER document content: '{}'", consent);
        }
    }
}
