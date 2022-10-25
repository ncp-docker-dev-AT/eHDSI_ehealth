package fi.kela.se.epsos.data.model;

import eu.europa.ec.sante.ehdsi.constant.ClassCode;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.data.model.SimpleConfidentialityEnum;

import java.util.Date;
import java.util.List;

/**
 * Factory class for Clinical Document container (metadata and associations).
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
    public static EPSOSDocument createEPSOSDocument(String patientId, ClassCode classCode, Document document) {
        return new EPSOSDocumentImpl(patientId, classCode, document);
    }

    /**
     * EPDocument
     */
    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String description, long size, String hash) {
        EpListParam epListParam = new EpListParam(true, null, null, null, null, null, null);
        return createEPDocument(documentFormat, id, patientId,
                effectiveDate, repositoryId,
                title, author, description,
                null, null,
                epListParam,
                SimpleConfidentialityEnum.N, null, size, hash);
    }

    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, String description,
                                                       String productCode, String productName,
                                                       EpListParam epListParam,
                                                       SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata = new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData =
                new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate, ClassCode.EP_CLASSCODE, repositoryId, title, author,
                        confidentialityMetadata, languageCode, size, hash);
        if (StringUtils.isNoneBlank(productCode, productName)) {
            return new EPDocumentMetaDataImpl(metaData, description, new EPDocumentMetaDataImpl.SimpleProductMetadata(productCode, productName), epListParam);
        } else {
            return new EPDocumentMetaDataImpl(metaData, description, epListParam);
        }
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, long size, String hash) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, true, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName, long size, String hash) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName,
                new EpListParam(true, null, null, null, null, null, null),
                SimpleConfidentialityEnum.N, null, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable, long size, String hash) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description,
                null, null,
                new EpListParam(dispensable, null, null, null, null, null, null),
                SimpleConfidentialityEnum.N, null, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode,
                                                         String productName,
                                                         EpListParam epListParam,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, epListParam,
                confidentiality, languageCode, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, long size, String hash) {
        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, true, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode, String productName, long size, String hash) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, productCode, productName,
                new EpListParam(true, null, null, null, null, null, null),
                SimpleConfidentialityEnum.N, null, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, boolean dispensable, long size, String hash) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, null, null,
                new EpListParam(dispensable, null, null, null, null, null, null),
                SimpleConfidentialityEnum.N, null, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, String description, String productCode,
                                                         String productName,
                                                         EpListParam epListParam,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, epListParam, confidentiality,
                languageCode, size, hash);
    }

    /**
     * PSDocument
     */
    private static PSDocumentMetaData createPSDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {
        PSDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata = new PSDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.PS_CLASSCODE, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);
        return new PSDocumentMetaDataImpl(metaData);
    }

    public static PSDocumentMetaData createPSDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode, size, hash);
    }

    public static PSDocumentMetaData createPSDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                         String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode, size, hash);
    }

    /**
     * OrCDDocument
     */
    private static OrCDDocumentMetaData createOrCDDocument(ClassCode orCDClassCode, int documentFormat, String id, String patientId, Date effectiveDate,
                                                           Date serviceStartTime, String repositoryId, String title, String author,
                                                           SimpleConfidentialityEnum confidentiality, String languageCode,
                                                           OrCDDocumentMetaData.DocumentFileType documentFileType,
                                                           long size,
                                                           List<OrCDDocumentMetaData.Author> authors,
                                                           OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation, String hash) {
        OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata = new OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                orCDClassCode, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);
        return new OrCDDocumentMetaDataImpl(metaData, documentFileType, serviceStartTime, authors, reasonOfHospitalisation);
    }

    /**
     * Laboratory Results
     */
    public static OrCDDocumentMetaData createOrCDLaboratoryResultsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                           String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode,
                                                                           long size, List<OrCDDocumentMetaData.Author> authors, String hash) {

        return createOrCDDocument(ClassCode.ORCD_LABORATORY_RESULTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, null, hash);
    }

    /**
     * Hospital Discharge Reports
     */
    public static OrCDDocumentMetaData createOrCDHospitalDischargeReportsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                                  String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size,
                                                                                  List<OrCDDocumentMetaData.Author> authors, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation, String hash) {

        return createOrCDDocument(ClassCode.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, reasonOfHospitalisation, hash);
    }

    /**
     * Medical Imaging Reports
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagingReportsDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                               String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size,
                                                                               List<OrCDDocumentMetaData.Author> authors, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation, String hash) {

        return createOrCDDocument(ClassCode.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, reasonOfHospitalisation, hash);
    }

    /**
     * Medical Images
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagesDocument(String id, String patientId, Date effectiveDate, Date serviceStartTime, String repositoryId,
                                                                       String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, OrCDDocumentMetaData.DocumentFileType documentFileType, long size,
                                                                       List<OrCDDocumentMetaData.Author> authors, OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation, String hash) {

        return createOrCDDocument(ClassCode.ORCD_MEDICAL_IMAGES_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, serviceStartTime,
                repositoryId, title, author, confidentiality, languageCode, documentFileType, size, authors, reasonOfHospitalisation, hash);
    }

    /**
     * EDDocument
     */
    private static EDDocumentMetaData createEDDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                       String repositoryId, String title, String author, long size, String hash) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.ED_CLASSCODE, repositoryId, title, author, null, null, size, hash);

        return new EDDocumentMetaDataImpl(metaData);
    }

    public static EDDocumentMetaData createEDDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, long size, String hash) {

        return createEDDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author, size, hash);
    }

    public static EDDocumentMetaData createEDDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, long size, String hash) {

        return createEDDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author, size, hash);
    }

    /**
     * ConsentDocument
     */
    @Deprecated
    private static ConsentDocumentMetaData createConsentDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                                 String repositoryId, String title, String author, Long size, String hash) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.CONSENT_CLASSCODE, repositoryId, title, author, size, hash);
        return new ConsentDocumentMetaDataImpl(metaData);
    }

    @Deprecated(since = "2.5.0", forRemoval = true)
    public static ConsentDocumentMetaData createConsentDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, Long size, String hash) {

        return createConsentDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId, title, author, size, hash);
    }

    @Deprecated(since = "2.5.0", forRemoval = true)
    public static ConsentDocumentMetaData createConsentDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId, String title, String author, Long size, String hash) {

        return createConsentDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId, title, author, size, hash);
    }

    /**
     * MRODocument
     */
    @Deprecated(since = "2.5.0", forRemoval = true)
    private static MroDocumentMetaData createMroDocument(int documentFormat, String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        MroDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata = new MroDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.MRO_CLASSCODE, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);
        return new MroDocumentMetaDataImpl(metaData);
    }

    @Deprecated(since = "2.5.0", forRemoval = true)
    public static MroDocumentMetaData createMroDocumentPDF(String id, String patientId, Date effectiveDate, String repositoryId,
                                                           String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate, repositoryId,
                title, author, confidentiality, languageCode, size, hash);
    }

    @Deprecated(since = "2.5.0", forRemoval = true)
    public static MroDocumentMetaData createMroDocumentXML(String id, String patientId, Date effectiveDate, String repositoryId,
                                                           String title, String author, SimpleConfidentialityEnum confidentiality, String languageCode, long size, String hash) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate, repositoryId,
                title, author, confidentiality, languageCode, size, hash);
    }
}
