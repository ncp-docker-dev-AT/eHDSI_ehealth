package eu.epsos.protocolterminators.ws.server.xdr;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorInterface;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import fi.kela.se.epsos.data.model.ConsentDocumentMetaData;
import fi.kela.se.epsos.data.model.DocumentAssociation;
import fi.kela.se.epsos.data.model.EDDocumentMetaData;
import fi.kela.se.epsos.data.model.EPSOSDocument;
import eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions.InsufficientRightsException;

/**
 * Interface for XDR document submit service implementation
 */
public interface DocumentSubmitInterface extends NationalConnectorInterface {

    /**
     * Stores a dispensation in the national infrastructure
     *
     * @param dispensationDocument - eDispensation document in epSOS pivot (CDA) form
     */
    void submitDispensation(EPSOSDocument dispensationDocument) throws NIException, InsufficientRightsException;

    /**
     * Discards a previously submitted dispensation
     *
     * @param dispensationToDiscard - Metadata of the dispensation to be discarded (XML and PDF versions)
     */
    void cancelDispensation(EPSOSDocument dispensationToDiscard) throws NIException, InsufficientRightsException;

    /**
     * Stores a patient consent in the national infrastructure
     *
     * @param consentDocument - patient consent document in epSOS pivot (CDA) form
     */
    void submitPatientConsent(EPSOSDocument consentDocument) throws NIException, InsufficientRightsException;

    /**
     * Discards a previously submitted consent
     *
     * @param consentToDiscard Metadata of the consent to be discarded (XML and PDF versions)
     */
    void cancelConsent(DocumentAssociation<ConsentDocumentMetaData> consentToDiscard) throws NIException, InsufficientRightsException;

    /**
     * Stores a HCER document in the national infrastructure
     *
     * @param hcerDocument - HCER document in epSOS pivot (CDA) form
     */
    void submitHCER(EPSOSDocument hcerDocument) throws DocumentProcessingException;
}
