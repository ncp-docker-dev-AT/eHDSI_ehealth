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
 */
public class DocumentFactory {

    /**
     * Private constructor restricting Object creation
     */
    private DocumentFactory() {
    }

    /**
     * Method creating an empty SearchCriteria class used by XCA Service transactions
     *
     * @return an implementation of the SearchCriteria interface used to pass the search parameters
     */
    public static SearchCriteria createSearchCriteria() {
        return new SearchCriteriaImpl();
    }

    /**
     * Method creating a SearchCriteria class used by XCA Service transactions and set with parameters
     *
     * @param criteria - Criteria from the Enum (PATIENT_ID, REPOSITORY_ID, DOCUMENT_ID etc.)
     * @param value    - Value of the parameter
     * @return an initialized instance of the SearchCriteria interface used to pass the search parameters
     */
    public static SearchCriteria createSearchCriteria(Criteria criteria, String value) {
        SearchCriteria searchCriteria = new SearchCriteriaImpl();
        searchCriteria.add(criteria, value);
        return searchCriteria;
    }

    /**
     * Method creating an association between two clinical document
     *
     * @param xml - Structured document L3 as XML
     * @param pdf - Original document L1 as PDF
     * @param <T> - Specified type of object
     * @return a DocumentAssociation of the two documents
     */
    public static <T extends EPSOSDocumentMetaData> DocumentAssociation<T> createDocumentAssociation(T xml, T pdf) {
        return new DocumentAssociationImpl<>(xml, pdf);
    }

    /**
     * Method creating a generic Clinical Document
     *
     * @param patientId - identifier of the patient involved into the transaction
     * @param classCode - classCode of the type of Clinical Document
     * @param document  - generic Clinical Document
     * @return an instance of EPSOSDocument initialized with the parameters
     */
    public static EPSOSDocument createEPSOSDocument(String patientId, ClassCode classCode, Document document) {
        return new EPSOSDocumentImpl(patientId, classCode, document);
    }

    //  Management of the EPDocument

    /**
     * Private Method creating a Prescription Document Metadata based on a minimal list of metadata
     *
     * @param documentFormat - classCode of the document exchanged.
     * @param id             - identifier of the clinical document.
     * @param patientId      - identifier of the patient involved into the transaction
     * @param effectiveDate  - date of the document
     * @param repositoryId   - identifier of the National clinical document repository
     * @param title          - title of the document
     * @param author         - author of the document
     * @param description    - summary of the document content
     * @param size           - size of the document exchanged in bytes
     * @param hash           - hash of the document exchanged
     * @return an instance of ePrescription Document Metadata
     */
    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId,
                                                       Date effectiveDate, String repositoryId, String title,
                                                       String author, String description, Long size, String hash) {

        EpListParam epListParam = new EpListParam(true, null, null, null,
                null, null, null);
        return createEPDocument(documentFormat, id, patientId, effectiveDate, repositoryId, title, author, description,
                null, null, epListParam, SimpleConfidentialityEnum.N, null, size, hash);
    }

    /**
     * Private Method creating a Prescription Document Metadata considering the required and optional information including
     * hash and size values of the document
     *
     * @param documentFormat  - classCode of the document exchanged.
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param description     - summary of the document content
     * @param productCode     - code of the product prescribed
     * @param productName     - name of the product described
     * @param epListParam     - ePrescription parameters as a List
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @param size            - size of the document exchanged in bytes
     * @param hash            - hash of the document exchanged
     * @return an instance of ePrescription Document Metadata
     */
    private static EPDocumentMetaData createEPDocument(int documentFormat, String id, String patientId,
                                                       Date effectiveDate, String repositoryId, String title,
                                                       String author, String description, String productCode,
                                                       String productName, EpListParam epListParam,
                                                       SimpleConfidentialityEnum confidentiality, String languageCode,
                                                       Long size, String hash) {

        EPDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata =
                new EPDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.EP_CLASSCODE, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);

        if (StringUtils.isNoneBlank(productCode, productName)) {
            return new EPDocumentMetaDataImpl(metaData, description,
                    new EPDocumentMetaDataImpl.SimpleProductMetadata(productCode, productName), epListParam);
        } else {
            return new EPDocumentMetaDataImpl(metaData, description, epListParam);
        }
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, Long size, String hash) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description,
                true, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, String productCode, String productName,
                                                         Long size, String hash) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, productCode,
                productName, new EpListParam(true, null, null, null, null,
                        null, null), SimpleConfidentialityEnum.N, null, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, boolean dispensable, long size, String hash) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, null,
                null, new EpListParam(dispensable, null, null, null, null,
                        null, null), SimpleConfidentialityEnum.N, null, size, hash);
    }

    /**
     * Method creating an original Prescription Document Metadata L1 containing only the required information of the document
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param description     - summary of the document content
     * @param productCode     - code of the product prescribed
     * @param productName     - name of the product described
     * @param epListParam     - ePrescription parameters as a List
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @return a fully initialized original ePrescription Document Metadata
     */
    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, String productCode, String productName,
                                                         EpListParam epListParam,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createEPDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, description, productCode,
                productName, epListParam, confidentiality, languageCode, null, null);
    }

    /**
     * Method creating an original Prescription Document Metadata L1 containing required and optional information
     * of the document (hash and  size)
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param description     - summary of the document content
     * @param productCode     - code of the product prescribed
     * @param productName     - name of the product described
     * @param epListParam     - ePrescription parameters as a List
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @param size            - size of the document exchanged in bytes
     * @param hash            - hash of the document exchanged
     * @return a fully initialized original ePrescription Document Metadata
     */
    public static EPDocumentMetaData createEPDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, String productCode, String productName,
                                                         EpListParam epListParam, SimpleConfidentialityEnum confidentiality,
                                                         String languageCode, Long size, String hash) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, epListParam, confidentiality,
                languageCode, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author, String description, Long size, String hash) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description,
                true, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, String productCode, String productName,
                                                         Long size, String hash) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description,
                productCode, productName, new EpListParam(true, null, null, null,
                        null, null, null), SimpleConfidentialityEnum.N, null, size, hash);
    }

    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, boolean dispensable, Long size, String hash) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, null,
                null, new EpListParam(dispensable, null, null, null, null,
                        null, null), SimpleConfidentialityEnum.N, null, size, hash);
    }

    /**
     * Method creating a structured Prescription Document Metadata L3 containing only the required information of the document
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param description     - summary of the document content
     * @param productCode     - code of the product prescribed
     * @param productName     - name of the product described
     * @param epListParam     - ePrescription parameters as a List
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @return a fully initialized structured ePrescription Document Metadata
     */
    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, String productCode, String productName,
                                                         EpListParam epListParam,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createEPDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, description, productCode,
                productName, epListParam, confidentiality, languageCode, null, null);
    }

    /**
     * Method creating a structured Prescription Document Metadata L3 containing required and optional information
     * of the document (hash and size)
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param author          - author of the document
     * @param description     - summary of the document content
     * @param productCode     - code of the product prescribed
     * @param productName     - name of the product described
     * @param epListParam     - ePrescription parameters as a List
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @param size            - size of the document exchanged in bytes
     * @param hash            - hash value of the document
     * @return a fully initialized original ePrescription Document Metadata
     */
    public static EPDocumentMetaData createEPDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         String description, String productCode, String productName,
                                                         EpListParam epListParam, SimpleConfidentialityEnum confidentiality,
                                                         String languageCode, Long size, String hash) {

        return createEPDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, description, productCode, productName, epListParam, confidentiality,
                languageCode, size, hash);
    }

    //  Management of the PSDocument

    /**
     * Private Method creating a Patient Summary Document Metadata
     *
     * @param documentFormat  - classCode of the document exchanged.
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @param size            - size of the document exchanged in bytes
     * @param hash            - hash of the document exchanged
     * @return an instance of Patient Summary Document Metadata
     */
    private static PSDocumentMetaData createPSDocument(int documentFormat, String id, String patientId,
                                                       Date effectiveDate, String repositoryId, String title,
                                                       String author, SimpleConfidentialityEnum confidentiality,
                                                       String languageCode, Long size, String hash) {

        PSDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata =
                new PSDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.PS_CLASSCODE, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);
        return new PSDocumentMetaDataImpl(metaData);
    }

    /**
     * Method creating an original Patient Summary Document Metadata L1 containing only the required information of the document
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @return a fully initialized structured Patient Summary Document Metadata
     */
    public static PSDocumentMetaData createPSDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createPSDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, confidentiality,
                languageCode, null, null);
    }

    /**
     * Method creating an original Patient Summary Document Metadata L1 containing all the required and optional information including
     * hash and size values of the document
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @param size            - size of the document exchanged in bytes
     * @param hash            - hash of the document exchanged
     * @return a fully initialized original Patient Summary Document Metadata
     */
    public static PSDocumentMetaData createPSDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode,
                                                         Long size, String hash) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode, size, hash);
    }

    /**
     * Method creating a structured Patient Summary Document Metadata L3 containing only the required information of the document
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @return a fully initialized structured Patient Summary Document Metadata
     */
    public static PSDocumentMetaData createPSDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode) {

        return createPSDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, confidentiality, languageCode, null, null);
    }

    /**
     * Method creating a structured Patient Summary Document Metadata L3 containing all the required and optional information including
     * hash and size values of the document
     *
     * @param id              - identifier of the clinical document.
     * @param patientId       - identifier of the patient involved into the transaction
     * @param effectiveDate   - date of the document
     * @param repositoryId    - identifier of the National clinical document repository
     * @param title           - title of the document
     * @param author          - author of the document
     * @param confidentiality - level of confidentiality of the document
     * @param languageCode    - language of the document
     * @param size            - size of the document exchanged in bytes
     * @param hash            - hash of the document exchanged
     * @return a fully initialized structured Patient Summary Document Metadata
     */
    public static PSDocumentMetaData createPSDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         SimpleConfidentialityEnum confidentiality, String languageCode,
                                                         Long size, String hash) {

        return createPSDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode, size, hash);
    }

    //  Management of the OrCDDocument
    private static OrCDDocumentMetaData createOrCDDocument(ClassCode orCDClassCode,
                                                           int documentFormat,
                                                           String id,
                                                           String patientId,
                                                           Date effectiveDate,
                                                           Date serviceStartTime,
                                                           String repositoryId,
                                                           String title,
                                                           String author,
                                                           SimpleConfidentialityEnum confidentiality,
                                                           String languageCode,
                                                           OrCDDocumentMetaData.DocumentFileType documentFileType,
                                                           Long size,
                                                           List<OrCDDocumentMetaData.Author> authors,
                                                           OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation,
                                                           String hash) {

        OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata =
                new OrCDDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                orCDClassCode, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);
        return new OrCDDocumentMetaDataImpl(metaData, documentFileType, serviceStartTime, authors, reasonOfHospitalisation);
    }

    /**
     * Laboratory Results
     */
    public static OrCDDocumentMetaData createOrCDLaboratoryResultsDocument(String id,
                                                                           String patientId,
                                                                           Date effectiveDate,
                                                                           Date serviceStartTime,
                                                                           String repositoryId,
                                                                           String title,
                                                                           String author,
                                                                           SimpleConfidentialityEnum confidentiality,
                                                                           String languageCode,
                                                                           Long size,
                                                                           List<OrCDDocumentMetaData.Author> authors,
                                                                           String hash) {

        return createOrCDDocument(ClassCode.ORCD_LABORATORY_RESULTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML,
                id, patientId, effectiveDate, serviceStartTime, repositoryId, title, author, confidentiality,
                languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, null, hash);
    }

    /**
     * Hospital Discharge Reports
     */
    public static OrCDDocumentMetaData createOrCDHospitalDischargeReportsDocument(String id,
                                                                                  String patientId,
                                                                                  Date effectiveDate,
                                                                                  Date serviceStartTime,
                                                                                  String repositoryId,
                                                                                  String title,
                                                                                  String author,
                                                                                  SimpleConfidentialityEnum confidentiality,
                                                                                  String languageCode,
                                                                                  Long size,
                                                                                  List<OrCDDocumentMetaData.Author> authors,
                                                                                  OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation,
                                                                                  String hash) {

        return createOrCDDocument(ClassCode.ORCD_HOSPITAL_DISCHARGE_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML,
                id, patientId, effectiveDate, serviceStartTime, repositoryId, title, author, confidentiality,
                languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, reasonOfHospitalisation, hash);
    }

    /**
     * Medical Imaging Reports
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagingReportsDocument(String id,
                                                                               String patientId,
                                                                               Date effectiveDate,
                                                                               Date serviceStartTime,
                                                                               String repositoryId,
                                                                               String title,
                                                                               String author,
                                                                               SimpleConfidentialityEnum confidentiality,
                                                                               String languageCode,
                                                                               Long size,
                                                                               List<OrCDDocumentMetaData.Author> authors,
                                                                               OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation,
                                                                               String hash) {

        return createOrCDDocument(ClassCode.ORCD_MEDICAL_IMAGING_REPORTS_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML,
                id, patientId, effectiveDate, serviceStartTime, repositoryId, title, author, confidentiality,
                languageCode, OrCDDocumentMetaData.DocumentFileType.PDF, size, authors, reasonOfHospitalisation, hash);
    }

    /**
     * Medical Images
     */
    public static OrCDDocumentMetaData createOrCDMedicalImagesDocument(String id,
                                                                       String patientId,
                                                                       Date effectiveDate,
                                                                       Date serviceStartTime,
                                                                       String repositoryId,
                                                                       String title,
                                                                       String author,
                                                                       SimpleConfidentialityEnum confidentiality,
                                                                       String languageCode,
                                                                       OrCDDocumentMetaData.DocumentFileType documentFileType,
                                                                       Long size,
                                                                       List<OrCDDocumentMetaData.Author> authors,
                                                                       OrCDDocumentMetaData.ReasonOfHospitalisation reasonOfHospitalisation,
                                                                       String hash) {

        return createOrCDDocument(ClassCode.ORCD_MEDICAL_IMAGES_CLASSCODE, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML,
                id, patientId, effectiveDate, serviceStartTime, repositoryId, title, author, confidentiality,
                languageCode, documentFileType, size, authors, reasonOfHospitalisation, hash);
    }


    //  Management of the EDDocument

    /**
     * Private Method creating a Dispense Document Metadata considering the required and optional information including
     * hash and size values of the document
     *
     * @param documentFormat - classCode of the document exchanged
     * @param id             - identifier of the clinical document
     * @param patientId      - patient identifier involved during the exchange
     * @param effectiveDate  - date of the document
     * @param repositoryId   - identifier of the National clinical document repository
     * @param title          - title of the document exchanged
     * @param author         - author of the clinical document exchanged
     * @param size           - size of the document exchanged in bytes
     * @param hash           - hash of the document exchanged
     * @return a fully initialized Dispense Document Metadata
     */
    private static EDDocumentMetaData createEDDocument(int documentFormat, String id, String patientId,
                                                       Date effectiveDate, String repositoryId, String title,
                                                       String author, Long size, String hash) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.ED_CLASSCODE, repositoryId, title, author, null, null, size, hash);

        return new EDDocumentMetaDataImpl(metaData);
    }

    /**
     * Method creating a Dispense Document Metadata L1 containing only the required information of the document
     *
     * @param id            - identifier of the clinical document
     * @param patientId     - patient identifier involved during the exchange
     * @param effectiveDate - date of the document
     * @param repositoryId  - identifier of the National clinical document repository
     * @param title         - title of the document exchanged
     * @param author        - author of the clinical document exchanged
     * @return a fully initialized original Dispense Document Metadata
     */
    public static EDDocumentMetaData createEDDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author) {

        return createEDDocumentPDF(id, patientId, effectiveDate, repositoryId, title, author, null, null);
    }

    /**
     * Method creating a Dispense Document Metadata L1 containing all the required and optional information including
     * hash and size values of the document
     *
     * @param id            - identifier of the clinical document
     * @param patientId     - patient identifier involved during the exchange
     * @param effectiveDate - date of the document
     * @param repositoryId  - identifier of the National clinical document repository
     * @param title         - title of the document exchanged
     * @param author        - author of the clinical document exchanged
     * @param size          - size of the document exchanged in bytes
     * @param hash          - hash of the document exchanged
     * @return a fully initialized original Dispense Document Metadata
     */
    public static EDDocumentMetaData createEDDocumentPDF(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         Long size, String hash) {

        return createEDDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, size, hash);
    }

    /**
     * Method creating a Dispense Document Metadata L3 containing only the required information of the document
     *
     * @param id            - identifier of the clinical document
     * @param patientId     - patient identifier involved during the exchange
     * @param effectiveDate - date of the document
     * @param repositoryId  - identifier of the National clinical document repository
     * @param title         - title of the document exchanged
     * @param author        - author of the clinical document exchanged
     * @return a fully initialized structured Dispense Document Metadata
     */
    public static EDDocumentMetaData createEDDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author) {

        return createEDDocumentXML(id, patientId, effectiveDate, repositoryId, title, author, null, null);
    }

    /**
     * Method creating a Dispense Document Metadata L3 containing all the required and optional information including
     * hash and size values of the document
     *
     * @param id            - identifier of the clinical document
     * @param patientId     - patient identifier involved during the exchange
     * @param effectiveDate - date of the document
     * @param repositoryId  - identifier of the National clinical document repository
     * @param title         - title of the document exchanged
     * @param author        - author of the clinical document exchanged
     * @param size          - size of the document exchanged in bytes
     * @param hash          - hash of the document exchanged
     * @return a fully initialized structured Dispense Document Metadata
     */
    public static EDDocumentMetaData createEDDocumentXML(String id, String patientId, Date effectiveDate,
                                                         String repositoryId, String title, String author,
                                                         Long size, String hash) {

        return createEDDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, size, hash);
    }

    //  Management of the ConsentDocument (not currently implemented in OpenNCP).

    /**
     * @deprecated Service currently not implemented in OpenNCP
     */
    @Deprecated(since = "2.4.0", forRemoval = true)
    private static ConsentDocumentMetaData createConsentDocument(int documentFormat, String id, String patientId,
                                                                 Date effectiveDate, String repositoryId, String title,
                                                                 String author, Long size, String hash) {

        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.CONSENT_CLASSCODE, repositoryId, title, author, size, hash);
        return new ConsentDocumentMetaDataImpl(metaData);
    }

    /**
     * @deprecated Service currently not implemented in OpenNCP
     */
    @Deprecated(since = "2.4.0", forRemoval = true)
    public static ConsentDocumentMetaData createConsentDocumentPDF(String id, String patientId, Date effectiveDate,
                                                                   String repositoryId, String title, String author,
                                                                   Long size, String hash) {

        return createConsentDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, size, hash);
    }

    /**
     * @deprecated Service currently not implemented in OpenNCP
     */
    @Deprecated(since = "2.4.0", forRemoval = true)
    public static ConsentDocumentMetaData createConsentDocumentXML(String id, String patientId, Date effectiveDate,
                                                                   String repositoryId, String title, String author,
                                                                   Long size, String hash) {

        return createConsentDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, size, hash);
    }

    //  Management of the MRODocument (not currently implemented in OpenNCP).

    /**
     * @deprecated Service currently not implemented in OpenNCP
     */
    @Deprecated(since = "2.4.0", forRemoval = true)
    private static MroDocumentMetaData createMroDocument(int documentFormat, String id, String patientId,
                                                         Date effectiveDate, String repositoryId, String title,
                                                         String author, SimpleConfidentialityEnum confidentiality,
                                                         String languageCode, Long size, String hash) {

        MroDocumentMetaDataImpl.SimpleConfidentialityMetadata confidentialityMetadata =
                new MroDocumentMetaDataImpl.SimpleConfidentialityMetadata(confidentiality);
        EPSOSDocumentMetaData metaData = new EPSOSDocumentMetaDataImpl(id, patientId, documentFormat, effectiveDate,
                ClassCode.MRO_CLASSCODE, repositoryId, title, author, confidentialityMetadata, languageCode, size, hash);
        return new MroDocumentMetaDataImpl(metaData);
    }

    /**
     * @deprecated Service currently not implemented in OpenNCP
     */
    @Deprecated(since = "2.4.0", forRemoval = true)
    public static MroDocumentMetaData createMroDocumentPDF(String id, String patientId, Date effectiveDate,
                                                           String repositoryId, String title, String author,
                                                           SimpleConfidentialityEnum confidentiality,
                                                           String languageCode, Long size, String hash) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode, size, hash);
    }

    /**
     * @deprecated Service currently not implemented in OpenNCP
     */
    @Deprecated(since = "2.4.0", forRemoval = true)
    public static MroDocumentMetaData createMroDocumentXML(String id, String patientId, Date effectiveDate,
                                                           String repositoryId, String title, String author,
                                                           SimpleConfidentialityEnum confidentiality,
                                                           String languageCode, Long size, String hash) {

        return createMroDocument(EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML, id, patientId, effectiveDate,
                repositoryId, title, author, confidentiality, languageCode, size, hash);
    }
}
