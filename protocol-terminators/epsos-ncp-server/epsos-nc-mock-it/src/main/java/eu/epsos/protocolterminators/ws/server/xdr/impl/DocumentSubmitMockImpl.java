package eu.epsos.protocolterminators.ws.server.xdr.impl;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.protocolterminators.ws.server.xdr.DocumentProcessingException;
import eu.epsos.protocolterminators.ws.server.xdr.DocumentSubmitInterface;
import fi.kela.se.epsos.data.model.ConsentDocumentMetaData;
import fi.kela.se.epsos.data.model.DocumentAssociation;
import fi.kela.se.epsos.data.model.EDDocumentMetaData;
import fi.kela.se.epsos.data.model.EPSOSDocument;
import org.apache.commons.lang.StringUtils;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSubmitMockImpl.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    public DocumentSubmitMockImpl() {
        LOGGER.info("Instantiating DocumentSubmitMockImpl");
    }

    /**
     * Stores a dispensation in the national infrastructure
     *
     * @param dispensationDocument eDispensation document in epSOS pivot (CDA) form
     */
    @Override
    public void submitDispensation(EPSOSDocument dispensationDocument) throws NIException {

        String dispensation = null;
        try {
            dispensation = XMLUtil.prettyPrint(dispensationDocument.getDocument().getFirstChild());
        } catch (TransformerException e) {
            LOGGER.error("TransformerException while submitDispensation(): '{}'", e.getMessage(), e);
            throwDocumentProcessingException("Cannot parse dispensation!", "4106");
        }
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            LOGGER_CLINICAL.info("eDispensation document content: '{}'", dispensation);
        }

        if (dispensation == null || dispensation.isEmpty()) {
            throwDocumentProcessingException("dispensation is null or empty!", "4106");
        }

        if (StringUtils.contains(dispensation, "testSubmitNoEP")) {
            LOGGER.error("Tried to submit dispensation with no matching ePrescription.");
            throwDocumentProcessingException("testSubmitNoEP", "4105");
        }

        if (StringUtils.contains(dispensation, "testSubmitDispEP")) {
            LOGGER.error("Tried to submit already dispensed ePrescription.");
            throwDocumentProcessingException("testSubmitDispEP", "4106");
        }
    }

    /**
     * Discards a previously submitted dispensation
     *
     * @param dispensationToDiscard Id of the dispensation to be discarded
     */
    @Override
    public void cancelDispensation(DocumentAssociation<EDDocumentMetaData> dispensationToDiscard) {
        LOGGER.info("eDispensation to be discarded: '{}'", dispensationToDiscard.getXMLDocumentMetaData().getId());
    }

    /**
     * Discards a previously submitted consent
     *
     * @param consentToDiscard Metadata of the consent to be discarded (XML and PDF versions)
     */
    @Override
    public void cancelConsent(DocumentAssociation<ConsentDocumentMetaData> consentToDiscard) {
        LOGGER.info("Consent to be discarded: '{}'", consentToDiscard.getXMLDocumentMetaData().getId());
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
            LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
            throwDocumentProcessingException("Cannot parse consent!", "4106");
        }
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            LOGGER_CLINICAL.info("Patient consent content: '{}'", consent);
        }
    }

    private void throwDocumentProcessingException(String message, String code) throws DocumentProcessingException {

        DocumentProcessingException dpe = new DocumentProcessingException();
        dpe.setMessage(message);
        dpe.setCode(code);
        throw dpe;
    }

    @Override
    public void submitHCER(EPSOSDocument hcerDocument) throws DocumentProcessingException {

        String consent = null;
        try {
            consent = PrettyPrinter.prettyPrint(hcerDocument.getDocument());
        } catch (TransformerException e) {
            LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
            throwDocumentProcessingException("Cannot parse HCER!", "4106");
        }
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            LOGGER_CLINICAL.info("HCER document content: '{}'", consent);
        }
    }
}
