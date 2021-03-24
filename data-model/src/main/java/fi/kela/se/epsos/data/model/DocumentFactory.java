package fi.kela.se.epsos.data.model;

import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.Constants;

import java.util.Date;

/**
 * Factory for fi.kela.se.epsos.data.model interfaces.
 *
 * @author mimyllyv
 */
public class DocumentFactory {

    private DocumentFactory() {
    }

    /**
     * SearchCriteria
     */
    public static SearchCriteria createSearchCriteria() {
        return new SearchCriteriaImpl();
    }

    public static SearchCriteria createSearchCriteria(Criteria c, String value) {
        SearchCriteria sc = new SearchCriteriaImpl();
        sc.add(c, value);
        return sc;
    }

    /**
     * DocumentAssociation
     */
    public static <T extends EPSOSDocumentMetaData> DocumentAssociation<T> createDocumentAssociation(T xml, T pdf) {
        return new DocumentAssociationImpl<T>(xml, pdf);
    }

    /**
     * EPSOSDocument
     */
    public static EPSOSDocument createEPSOSDocument(String patientId, String classCode, Document document) {
        return new EPSOSDocumentImpl(patientId, classCode, document);
    }

    /**
     * EPDocument
     */
    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String description) {

        return createEPDocument(documentFormat, id, patientId, effectiveDate, repositoryId, title, author, description, null, null, true, null, null);
    }

    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String description, String productCode, String productName, 
                                                       boolean dispensable, String confidentialityCode, String confidentialityDisplay) {
    	EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
    	if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
    		confidentiality = new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
    	}
        
		EPSOSDocumentMetaData metaData =
                new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate, Constants.EP_CLASSCODE, repositoryId, title, author, 
                		confidentiality);
        if (StringUtils.isNoneBlank(productCode, productName)) {
            return new EPDocumentMetaDataImpl(metaData, description, new EPDocumentMetaDataImpl.SimpleProductMetadata(productCode, productName), dispensable);
        } else {
            return new EPDocumentMetaDataImpl(metaData, description, dispensable);
        }
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, true);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName, true, null, null);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, null, null, dispensable, null, null);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName, boolean dispensable, String confidentialityCode, String confidentialityDisplay) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName, dispensable, confidentialityCode, confidentialityDisplay);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description) {
        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, true);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName, true, null, null);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, null, null, dispensable, null, null);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName, boolean dispensable, 
                                                         String confidentialityCode, String confidentialityDisplay) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName, dispensable, confidentialityCode, confidentialityDisplay);
    }

    /**
     * PSDocument
     */
    private static PSDocumentMetaData createPSDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String confidentialityCode, String confidentialityDisplay) {
    	EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
    	if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
    		confidentiality = new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
    	}
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.PS_CLASSCODE, repositoryId, title, author, confidentiality);
        return new PSDocumentMetaDataImpl(metaData);
    }

    public static PSDocumentMetaData createPSDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String confidentialityCode, String confidentialityDisplay) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author, confidentialityCode, confidentialityDisplay);
    }

    public static PSDocumentMetaData createPSDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String confidentialityCode, String confidentialityDisplay) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author, confidentialityCode, confidentialityDisplay);
    }

    /**
     * EDDocument
     */
    private static EDDocumentMetaData createEDDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.ED_CLASSCODE, repositoryId, title, author, null);

        return new EDDocumentMetaDataImpl(metaData);
    }

    public static EDDocumentMetaData createEDDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author) {

        return createEDDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author);
    }

    public static EDDocumentMetaData createEDDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author) {

        return createEDDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author);
    }

    /**
     * ConsentDocument
     */
    @Deprecated
    private static ConsentDocumentMetaData createConsentDocument(int documentFormat, String id, String patientId, Date effectiveDate, String repositoryId, String title, String author) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate, Constants.CONSENT_CLASSCODE, repositoryId, title, author, null);
        return new ConsentDocumentMetaDataImpl(metaData);
    }

    @Deprecated
    public static ConsentDocumentMetaData createConsentDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author) {

        return createConsentDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author);
    }

    @Deprecated
    public static ConsentDocumentMetaData createConsentDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author) {

        return createConsentDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author);
    }

    /**
     * MRODocument
     */
    @Deprecated
    private static MroDocumentMetaData createMroDocument(int documentFormat, String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, String confidentialityCode, String confidentialityDisplay) {
    	EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
    	if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
    		confidentiality = new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
    	}
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate, Constants.MRO_CLASSCODE, repositoryId, title, author, confidentiality);
        return new MroDocumentMetaDataImpl(metaData);
    }

    @Deprecated
    public static MroDocumentMetaData createMroDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, String confidentialityCode, String confidentialityDisplay) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author, confidentialityCode,  confidentialityDisplay);
    }

    @Deprecated
    public static MroDocumentMetaData createMroDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, String confidentialityCode, String confidentialityDisplay) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author, confidentialityCode,  confidentialityDisplay);
    }
}
