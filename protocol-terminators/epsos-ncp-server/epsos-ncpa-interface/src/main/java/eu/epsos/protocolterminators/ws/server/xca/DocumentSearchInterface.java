package eu.epsos.protocolterminators.ws.server.xca;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorInterface;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import fi.kela.se.epsos.data.model.DocumentAssociation;
import fi.kela.se.epsos.data.model.EPDocumentMetaData;
import fi.kela.se.epsos.data.model.EPSOSDocument;
import fi.kela.se.epsos.data.model.MroDocumentMetaData;
import fi.kela.se.epsos.data.model.PSDocumentMetaData;
import fi.kela.se.epsos.data.model.SearchCriteria;

import java.util.List;

import tr.com.srdc.epsos.securityman.exceptions.InsufficientRightsException;

/**
 * Combined interface for Patient Summary and ePrescription XCA Service implementation.
 * Implementations of the interface in the countries supporting only eP or only PS should throw UnsupportedOperationException on the missing methods.
 */
public interface DocumentSearchInterface extends NationalConnectorInterface {

    /**
     * This method returns one DocumentAssociation (with PSDocumentMetaData in XML and/or PDF format) that matches the searchCriteria.
     *
     * @param searchCriteria (see SearchCriteria interface for more info)
     * @return DocumentAssociation<PSDocumentMetaData>
     */
    DocumentAssociation<PSDocumentMetaData> getPSDocumentList(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException;

    /**
     * This method returns one DocumentAssociation (with MroDocumentMetaData in
     * XML and/or PDF format) that matches the searchCriteria.
     *
     * @param searchCriteria (see SearchCriteria interface for more info)
     * @return DocumentAssociation<MroDocumentMetaData>
     */
    DocumentAssociation<MroDocumentMetaData> getMroDocumentList(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException;

    /**
     * This method returns one/several DocumentAssociation(s) (EPDocumentMetaData in XML and/or PDF format) that matches the searchCriteria.
     *
     * @param searchCriteria (see SearchCriteria interface for more info)
     * @return List<DocumentAssociation < EPDocumentMetaData>>
     */
    List<DocumentAssociation<EPDocumentMetaData>> getEPDocumentList(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException;

    /**
     * This method returns one EPSOSDocument which includes document metaData
     * and the DOM document itself matching the searchCriteria. The
     * searchCriteria shall have PatientId and DocumentId as mandatory fields
     * filled.
     *
     * @param searchCriteria (see SearchCriteria interface for more info)
     * @return EPSOSDocument
     */
    EPSOSDocument getDocument(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException;
}
