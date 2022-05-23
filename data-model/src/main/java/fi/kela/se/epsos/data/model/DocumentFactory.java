package fi.kela.se.epsos.data.model;

import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.data.model.SimpleConfidentialityEnum;
import tr.com.srdc.epsos.util.Constants;

import java.util.Date;
import java.util.List;

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
        EpListParam epListParam = new EpListParam(true, null, null, null, null, null, null);
        return createEPDocument(documentFormat, id, patientId,
                effectiveDate, repositoryId,
                title, author, description,
                null, null,
                epListParam,
                SimpleConfidentialityEnum.N, null);
    }

    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String description,
                                                       String productCode, String productName,
                                                       EpListParam epListParam,
                                                       SimpleConfidentialityEnum confidentiality, String languageCode) {

        EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialitymetadata = null;
        confidentialitymetadata = new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData =
                new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate, Constants.EP_CLASSCODE, repositoryId, title, author,
                        confidentialitymetadata, languageCode);
        if (StringUtils.isNoneBlank(productCode, productName)) {
            return new EPDocumentMetaDataImpl(metaData, description, new EPDocumentMetaDataImpl.SimpleProductMetadata(productCode, productName), epListParam);
        } else {
            return new EPDocumentMetaDataImpl(metaData, description, epListParam);
        }
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, true);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName,
                new EpListParam(true, null, null, null, null, null, null),
                SimpleConfidentialityEnum.N, null);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description,
                null, null,
                new EpListParam(dispensable, null, null, null, null, null, null),
                SimpleConfidentialityEnum.N, null);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode,
                                                         String productName,
                                                         EpListParam epListParam,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, epListParam,
                confidentiality, languageCode);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description) {
        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, true);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName,
                new EpListParam(true, null, null, null,null,null, null),
                SimpleConfidentialityEnum.N, null);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, null, null,
                new EpListParam(dispensable, null, null, null, null,null, null),
                SimpleConfidentialityEnum.N, null);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode,
                                                         String productName,
                                                         EpListParam epListParam,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, epListParam, confidentiality,
                languageCode);
    }

    /**
     * PSDocument
     */
    private static PSDocumentMetaData createPSDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author,  SimpleConfidentialityEnum confidentiality, String languageCode) {
        PSDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialitymetadata = null;
        confidentialitymetadata = new PSDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.PS_CLASSCODE, repositoryId, title, author, confidentialitymetadata, languageCode);
        return new PSDocumentMetaDataImpl(metaData);
    }

    public static PSDocumentMetaData createPSDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode);
    }

    public static PSDocumentMetaData createPSDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode);
    }

    /**
     * OrCDDocument
     */
    private static OrCDDocumentMetaData createOrCDDocument(String orCDClassCode, int documentFormat, String id, String patientId, Date effectiveDate,
                                                           Date serviceStartTime, String repositoryId, String title, String author,
                                                           SimpleConfidentialityEnum confidentiality, String languageCode,
                                                           OrCDDocumentMetaData.DocumentFileType documentFileType,
                                                           long size,
                                                           List<OrCDDocumentMetaData.Author> authors,
                                                           OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {
        OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialitymetadata = null;
        confidentialitymetadata = new OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                orCDClassCode, repositoryId, title, author, confidentialitymetadata, languageCode);
        return new OrCDDocumentMetaDataImpl(metaData, documentFileType, size, serviceStartTime, authors, reasonOfHospitalisation);
    }

    /**
     * Laboratory Results
     */
    public static OrCDDocumentMetaData createOrCDLaboratoryResultsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                           String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode,
                                                                           long size, List<OrCDDocumentMetaData.Author> authors){

        return createOrCDDocument(Constants.ORCD_LABORATORY_RESULTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, null);
    }

    /**
     * Hospital Discharge Reports
     */
    public static OrCDDocumentMetaData createOrCDHospitalDischargeReportsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                                  String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size,
                                                                                  List<OrCDDocumentMetaData.Author> authors, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        return createOrCDDocument(Constants.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, reasonOfHospitalisation);
    }

    /**
     * Medical Imaging Reports
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagingReportsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                               String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size,
                                                                               List<OrCDDocumentMetaData.Author> authors, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        return createOrCDDocument(Constants.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, reasonOfHospitalisation);
    }

    /**
     * Medical Images
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagesDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                               String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, OrCDDocumentMetaData.DocumentFileType documentFileType, long size,
                                                                       List<OrCDDocumentMetaData.Author> authors, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation) {

        return createOrCDDocument(Constants.ORCD_MEDICAL_IMAGES_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, documentFileType, size, authors, reasonOfHospitalisation);
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
                                                         String repositoryId, String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode) {

        MroDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialitymetadata = null;
        confidentialitymetadata = new MroDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                Constants.MRO_CLASSCODE, repositoryId, title, author, confidentialitymetadata, languageCode);
        return new MroDocumentMetaDataImpl(metaData);
    }

    @Deprecated
    public static MroDocumentMetaData createMroDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                           String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId,
                title, author, confidentiality, languageCode);
    }

    @Deprecated
    public static MroDocumentMetaData createMroDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                           String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId,
                title, author, confidentiality, languageCode);
    }
}
