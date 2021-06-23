package fi.kela.se.epsos.data.model;

import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.Constants;

import java.util.Date;

/**
 * Factory class for Clinical Document container (metadatas and associations).
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

    public static SearchCriteria createSearchCriteria(Criteria criteria, String value) {
        SearchCriteria searchCriteria = new SearchCriteriaImpl();
        searchCriteria.add(criteria, value);
        return searchCriteria;
    }

    /**
     * DocumentAssociation
     */
    public static <T extends EPSOSDocumentMetaData> DocumentAssociation<T> createDocumentAssociation(T xml, T pdf) {
        return new DocumentAssociationImpl<>(xml, pdf);
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

        return createEPDocument(documentFormat, id, patientId, effectiveDate, repositoryId, title, author, description,
                null, null, true, null, null, null);
    }

    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String description,
                                                       String productCode, String productName, boolean dispensable,
                                                       String confidentialityCode, String confidentialityDisplay, String languageCode) {

        EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
        if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
            confidentiality = new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
        }

        EPSOSDocumentMetaData metaData =
                new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate, Constants.EP_CLASSCODE, repositoryId, title, author,
                        confidentiality, languageCode);
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

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName,
                true, null, null, null);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, null, null,
                dispensable, null, null, null);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode,
                                                         String productName, boolean dispensable, String confidentialityCode,
                                                         String confidentialityDisplay, String languageCode) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, dispensable, confidentialityCode,
                confidentialityDisplay, languageCode);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description) {
        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, true);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName,
                true, null, null, null);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, null, null,
                dispensable, null, null, null);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode,
                                                         String productName, boolean dispensable, String confidentialityCode,
                                                         String confidentialityDisplay, String languageCode) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, dispensable, confidentialityCode,
                confidentialityDisplay, languageCode);
    }

    /**
     * PSDocument
     */
    private static PSDocumentMetaData createPSDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String confidentialityCode,
                                                       String confidentialityDisplay, String languageCode) {

        PSDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
        if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
            confidentiality = new PSDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
        }
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.PS_CLASSCODE, repositoryId, title, author, confidentiality, languageCode);
        return new PSDocumentMetaDataImpl(metaData);
    }

    public static PSDocumentMetaData createPSDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String confidentialityCode,
                                                         String confidentialityDisplay, String languageCode) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, confidentialityCode, confidentialityDisplay, languageCode);
    }

    public static PSDocumentMetaData createPSDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String confidentialityCode,
                                                         String confidentialityDisplay, String languageCode) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, confidentialityCode, confidentialityDisplay, languageCode);
    }

    /**
     * OrCDDocument
     */
    private static OrCDDocumentMetaData createOrCDDocument(String orCDClassCode, int documentFormat, String id, String patientId, Date effectiveDate, Date serviceStartTime,
                                                           String repositoryId, String title, String author, String confidentialityCode, String confidentialityDisplay, String languageCode, OrCDDocumentMetaData.DocumentFileType documentFileType, long size, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {
        OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
        if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
            confidentiality = new OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
        }
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                orCDClassCode, repositoryId, title, author, confidentiality, languageCode);
        return new OrCDDocumentMetaDataImpl(metaData, documentFileType, size, serviceStartTime, reasonOfHospitalisation);
    }

    /**
     * Laboratory Results
     */
    public static OrCDDocumentMetaData createOrCDLaboratoryResultsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                         String title, String author, String confidentialityCode, String confidentialityDisplay, String languageCode, long size) {

        return createOrCDDocument(Constants.ORCD_LABORATORY_RESULTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentialityCode, confidentialityDisplay, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, null);
    }

    /**
     * Hospital Discharge Reports
     */
    public static OrCDDocumentMetaData createOrCDHospitalDischargeReportsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                           String title, String author, String confidentialityCode, String confidentialityDisplay, String languageCode, long size, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        return createOrCDDocument(Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentialityCode, confidentialityDisplay, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, reasonOfHospitalisation);
    }

    /**
     * Medical Imaging Reports
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagingReportsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                                  String title, String author, String confidentialityCode, String confidentialityDisplay, String languageCode, long size, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        return createOrCDDocument(Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentialityCode, confidentialityDisplay, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, reasonOfHospitalisation);
    }

    /**
     * Medical Images
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagesDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                               String title, String author, String confidentialityCode, String confidentialityDisplay, String languageCode, OrCDDocumentMetaData.DocumentFileType documentFileType, long size, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        return createOrCDDocument(Constants.ORCD_MEDICAL_IMAGES_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentialityCode, confidentialityDisplay, languageCode, documentFileType, size, reasonOfHospitalisation);
    }

    /**
     * EDDocument
     */
    private static EDDocumentMetaData createEDDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.ED_CLASSCODE, repositoryId, title, author, null, null);

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
    private static ConsentDocumentMetaData createConsentDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                                 String repositoryId, String title, String author) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.CONSENT_CLASSCODE, repositoryId, title, author);
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
    private static MroDocumentMetaData createMroDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author, String confidentialityCode,
                                                         String confidentialityDisplay, String languageCode) {

        MroDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentiality = null;
        if (StringUtils.isNoneBlank(confidentialityCode, confidentialityDisplay)) {
            confidentiality = new MroDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentialityCode, confidentialityDisplay);
        }

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.MRO_CLASSCODE, repositoryId, title, author, confidentiality, languageCode);
        return new MroDocumentMetaDataImpl(metaData);
    }

    @Deprecated
    public static MroDocumentMetaData createMroDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                           String title, String author, String confidentialityCode,
                                                           String confidentialityDisplay, String languageCode) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId,
                title, author, confidentialityCode, confidentialityDisplay, languageCode);
    }

    @Deprecated
    public static MroDocumentMetaData createMroDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                           String title, String author, String confidentialityCode,
                                                           String confidentialityDisplay, String languageCode) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId,
                title, author, confidentialityCode, confidentialityDisplay, languageCode);
    }
}
