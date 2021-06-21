package eu.epsos.protocolterminators.ws.server.xca.impl;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.common.ResourceList;
import eu.epsos.protocolterminators.ws.server.common.ResourceLoader;
import eu.epsos.protocolterminators.ws.server.xca.DocumentSearchInterface;
import eu.europa.ec.sante.ehdsi.openncp.mock.util.CdaUtils;
import fi.kela.se.epsos.data.model.*;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import javassist.Loader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author konstantin.hypponen@kela.fi Note this is a very dirty implementation
 */
public class DocumentSearchMockImpl extends NationalConnectorGateway implements DocumentSearchInterface {

    private static final String PATTERN_EP = "epstore.+\\.xml";
    private static final String PATTERN_PS = "psstore.+\\.xml";
    private static final String PATTERN_MRO = "mrostore.+\\.xml";
    private static final String PATTERN_ORCD_LABORATORY_RESULTS = "orcd_laboratoryresultsstore.+\\.xml";
    private static final String PATTERN_ORCD_HOSPITAL_DISCHARGE_REPORTS = "orcd_hospitaldischargereportsstore.+\\.xml";
    private static final String PATTERN_ORCD_MEDICAL_IMAGING_REPORTS = "orcd_medicalimagingreportsstore.+\\.xml";
    private static final String PATTERN_ORCD_MEDICAL_IMAGES = "orcd_medicalimagesstore.+\\.xml";
    private static final String CONSTANT_EXTENSION = "extension";
    private static final String EHDSI_HL7_NAMESPACE = "urn:hl7-org:v3";
    private static final String EHDSI_EPSOS_MEDICATION_NAMESPACE = "urn:epsos-org:ep:medication";
    private static final String EHDSI_PS_L3_TEMPLATE_ID = "1.3.6.1.4.1.12559.11.10.1.3.1.1.3";
    private static final String EHDSI_PS_L1_TEMPLATE_ID = "1.3.6.1.4.1.12559.11.10.1.3.1.1.7";
    private static final String EHDSI_EP_L1_TEMPLATE_ID = "1.3.6.1.4.1.12559.11.10.1.3.1.1.6";
    private final Logger logger = LoggerFactory.getLogger(DocumentSearchMockImpl.class);
    private final List<DocumentAssociation<EPDocumentMetaData>> epDocumentMetaDatas = new ArrayList<>();
    private final List<DocumentAssociation<PSDocumentMetaData>> psDocumentMetaDatas = new ArrayList<>();
    private final List<DocumentAssociation<MroDocumentMetaData>> mroDocumentMetaDatas = new ArrayList<>();
    private final List<OrCDDocumentMetaData> orCDDocumentLaboratoryResultsMetaDatas = new ArrayList<>();
    private final List<OrCDDocumentMetaData> orCDDocumentHospitalDischargeReportsMetaDatas = new ArrayList<>();
    private final List<OrCDDocumentMetaData> orCDDocumentMedicalImagingReportsMetaDatas = new ArrayList<>();
    private final List<OrCDDocumentMetaData> orCDDocumentMedicalImagesMetaDatas = new ArrayList<>();
    private final List<EPSOSDocument> documents = new ArrayList<>();

    public DocumentSearchMockImpl() {

        Collection<String> documentlist = ResourceList.getResources(Pattern.compile(PATTERN_EP));
        ResourceLoader resourceLoader = new ResourceLoader();

        // Mocked ePrescription fill up
        for (String xmlFilename : documentlist) {

            logger.debug("Reading file '{}'", xmlFilename);
            // make sure there is a pdf version of the document in the repository
            String pdfFilename = xmlFilename.substring(0, xmlFilename.length() - 4) + ".pdf";
            if (StringUtils.equals(resourceLoader.getResource(pdfFilename), "")) {
                continue;
            }

            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = XMLUtil.parseContent(xmlDocString);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                Document pdfDoc = XMLUtil.parseContent(xmlDocString);
                byte[] pdfcontents = resourceLoader.getResourceAsByteArray(pdfFilename);
                wrapPDFinCDA(pdfcontents, pdfDoc);
                addFormatToOID(pdfDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF);

                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                String productCode = null;
                String productName = null;
                Element element = getProductFromPrescription(xmlDoc);
                if (element != null) {
                    productCode = element.getAttribute("code");
                    productName = element.getAttribute("displayName");
                }

                String description = getDescriptionFromDocument(xmlDoc);

                EPDocumentMetaData epdXml = DocumentFactory.createEPDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc), description, productCode, productName, true,
                        getClinicalDocumentConfidentialityCode(xmlDoc), getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc));
                logger.debug("Placed XML doc id='{}' HomeCommId='{}', Patient Id: '{}' into eP repository",
                        epdXml.getId(), Constants.HOME_COMM_ID, pd.getId());
                documents.add(DocumentFactory.createEPSOSDocument(epdXml.getPatientId(), epdXml.getClassCode(), xmlDoc));

                EPDocumentMetaData epdPdf = DocumentFactory.createEPDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc), description, productCode, productName, true,
                        getClinicalDocumentConfidentialityCode(xmlDoc), getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc));
                logger.debug("Placed PDF doc id='{}' into eP repository", epdPdf.getId());
                documents.add(DocumentFactory.createEPSOSDocument(epdPdf.getPatientId(), epdPdf.getClassCode(), pdfDoc));

                epDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(epdXml, epdPdf));
            } catch (Exception e) {
                logger.warn("Could not read file at" + xmlFilename, e);
            }
        }

        // Mocked Patient Summaries fill up
        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_PS));

        for (String xmlFilename : documentlist) {
            logger.debug("Reading file '{}", xmlFilename);
            // make sure there is a pdf version of the document in the repository
            String pdfFilename = xmlFilename.substring(0, xmlFilename.length() - 4) + ".pdf";
            if (StringUtils.equals(resourceLoader.getResource(pdfFilename), "")) {
                continue;
            }

            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = XMLUtil.parseContent(xmlDocString);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                Document pdfDoc = XMLUtil.parseContent(xmlDocString);
                byte[] pdfcontents = resourceLoader.getResourceAsByteArray(pdfFilename);
                wrapPDFinCDA(pdfcontents, pdfDoc);
                logger.debug("Adding format to the document's OID");
                addFormatToOID(pdfDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF);
                logger.debug("Parsing PS patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);


                PSDocumentMetaData psdPdf = DocumentFactory.createPSDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(pdfDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(pdfDoc), this.getClinicalDocumentConfidentialityDisplay(pdfDoc), this.getClinicalDocumentLanguage(pdfDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdPdf.getPatientId(), psdPdf.getClassCode(), pdfDoc));
                PSDocumentMetaData psdXml = DocumentFactory.createPSDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(xmlDoc), this.getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdXml.getPatientId(), psdPdf.getClassCode(), xmlDoc));
                logger.debug("Placed PDF doc id=" + psdPdf.getId() + " into PS repository");
                logger.debug("Placed XML doc id=" + psdXml.getId() + " into PS repository");

                psDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(psdXml, psdPdf));
            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }

        // Mocked OrCDs fill up

        /* Hospital Discharge Reports */
        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_ORCD_HOSPITAL_DISCHARGE_REPORTS));
        for (String xmlFilename : documentlist) {
            logger.debug("Reading file '{}", xmlFilename);
            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = XMLUtil.parseContent(xmlDocString);
                long size = getFileSize(xmlFilename);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                logger.debug("Parsing OrCD patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                OrCDDocumentMetaData orcddXml = DocumentFactory.createOrCDHospitalDischargeReportsDocument(getOIDFromDocument(xmlDoc), pd.getId(),
                        getCreationDateFromDocument(xmlDoc), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(xmlDoc), this.getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc), size);
                documents.add(DocumentFactory.createEPSOSDocument(orcddXml.getPatientId(), orcddXml.getClassCode(), xmlDoc));
                orCDDocumentHospitalDischargeReportsMetaDatas.add(orcddXml);
                logger.debug("Placed XML doc id= '{}' into OrCD repository", orcddXml.getId());

            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }

        /* Laboratory Results */
        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_ORCD_LABORATORY_RESULTS));
        for (String xmlFilename : documentlist) {
            logger.debug("Reading file '{}", xmlFilename);
            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = XMLUtil.parseContent(xmlDocString);
                long size = getFileSize(xmlFilename);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                logger.debug("Parsing OrCD patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                OrCDDocumentMetaData orcddXml = DocumentFactory.createOrCDLaboratoryResultsDocument(getOIDFromDocument(xmlDoc), pd.getId(),
                        getCreationDateFromDocument(xmlDoc), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(xmlDoc), this.getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc), size);
                documents.add(DocumentFactory.createEPSOSDocument(orcddXml.getPatientId(), orcddXml.getClassCode(), xmlDoc));
                orCDDocumentLaboratoryResultsMetaDatas.add(orcddXml);
                logger.debug("Placed XML doc id= '{}' into OrCD repository", orcddXml.getId());
            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }

        /* Medical Imaging Reports */
        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_ORCD_MEDICAL_IMAGING_REPORTS));
        for (String xmlFilename : documentlist) {
            logger.debug("Reading file '{}", xmlFilename);
            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = XMLUtil.parseContent(xmlDocString);
                long size = getFileSize(xmlFilename);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                logger.debug("Parsing OrCD patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                OrCDDocumentMetaData orcddXml = DocumentFactory.createOrCDMedicalImagingReportsDocument(getOIDFromDocument(xmlDoc), pd.getId(),
                        getCreationDateFromDocument(xmlDoc), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(xmlDoc), this.getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc), size);
                documents.add(DocumentFactory.createEPSOSDocument(orcddXml.getPatientId(), orcddXml.getClassCode(), xmlDoc));

                orCDDocumentMedicalImagingReportsMetaDatas.add(orcddXml);
                logger.debug("Placed XML doc id= '{}' into OrCD repository", orcddXml.getId());
            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }

        /* Medical Images */
        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_ORCD_MEDICAL_IMAGES));
        for (String xmlFilename : documentlist) {
            logger.debug("Reading file '{}'", xmlFilename);
            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = XMLUtil.parseContent(xmlDocString);
                long size = getFileSize(xmlFilename);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                logger.debug("Parsing OrCD patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                OrCDDocumentMetaData orcddXml = DocumentFactory.createOrCDMedicalImagesDocument(getOIDFromDocument(xmlDoc), pd.getId(),
                        getCreationDateFromDocument(xmlDoc), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(xmlDoc), this.getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc), OrCDDocumentMetaData.DocumentFileType.PNG, size);
                documents.add(DocumentFactory.createEPSOSDocument(orcddXml.getPatientId(), orcddXml.getClassCode(), xmlDoc));

                orCDDocumentMedicalImagesMetaDatas.add(orcddXml);
                logger.debug("Placed XML doc id= '{}' into OrCD repository", orcddXml.getId());
            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }

        // Mocked MROs fill up
        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_MRO));

        for (String xmlFilename : documentlist) {
            logger.debug("reading file '{}'", xmlFilename);
            // make sure there is a pdf version of the document in the repository
            String pdfFilename = xmlFilename.substring(0, xmlFilename.length() - 4) + ".pdf";
            if (StringUtils.equals(resourceLoader.getResource(pdfFilename), "")) {
                continue;
            }

            try {
                String xmlDocString = resourceLoader.getResource(xmlFilename);
                Document xmlDoc = loadCDADocument(xmlDocString);
                addFormatToOID(xmlDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_XML);
                Document pdfDoc = loadCDADocument(xmlDocString);
                byte[] pdfcontents = resourceLoader.getResourceAsByteArray(pdfFilename);
                wrapPDFinCDA(pdfcontents, pdfDoc);
                logger.debug("Adding format to the document's OID");
                addFormatToOID(pdfDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF);
                logger.debug("Parsing MRO patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                MroDocumentMetaData psdPdf = DocumentFactory.createMroDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(pdfDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(pdfDoc), this.getClinicalDocumentConfidentialityDisplay(pdfDoc), this.getClinicalDocumentLanguage(pdfDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdPdf.getPatientId(), psdPdf.getClassCode(), pdfDoc));
                MroDocumentMetaData psdXml = DocumentFactory.createMroDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc),
                        this.getClinicalDocumentConfidentialityCode(xmlDoc), this.getClinicalDocumentConfidentialityDisplay(xmlDoc), this.getClinicalDocumentLanguage(xmlDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdXml.getPatientId(), psdPdf.getClassCode(), xmlDoc));
                logger.debug("Placed PDF doc id='{}' into MRO repository", psdPdf.getId());
                logger.debug("Placed XML doc id='{}' into MRO repository", psdXml.getId());

                mroDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(psdXml, psdPdf));
            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }
    }

    private long getFileSize(String xmlFilename) throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(xmlFilename);
        File tempFile = new File("temp.xml");
        FileUtils.copyInputStreamToFile(is, tempFile);
        long bytes = tempFile.length();
        tempFile.delete();
        return bytes;
    }

    private Document loadCDADocument(String content) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(new StringReader(content));
        return docBuilder.parse(inputSource);
    }

    private String getClinicalDocumentAuthor(Document doc) {

        List<Node> nodeList = XMLUtil.getNodeList(doc, "ClinicalDocument/author/assignedAuthor/assignedPerson/name");
        String author = "";
        for (Node node : nodeList) {

            NodeList nodeList1 = node.getChildNodes();
            if (nodeList1 != null) {
                StringBuilder prefix = new StringBuilder();
                StringBuilder suffix = new StringBuilder();
                StringBuilder given = new StringBuilder();
                StringBuilder family = new StringBuilder();
                for (int i = 0; i < nodeList1.getLength(); i++) {
                    Node node1 = nodeList1.item(i);

                    if (node1.getNodeType() == Node.ELEMENT_NODE) {
                        logger.debug("Node: '{}'", node1.getLocalName());
                        switch (node1.getLocalName()) {
                            case "prefix":
                                prefix.append(node1.getTextContent()).append(" ");
                                break;
                            case "suffix":
                                suffix.append(node1.getTextContent()).append(" ");
                                break;
                            case "given":
                                given.append(node1.getTextContent()).append(" ");
                                break;
                            case "family":
                                family.append(node1.getTextContent()).append(" ");
                                break;
                            default:
                                logger.warn("No Author information to append...");
                                break;
                        }
                    }
                }
                author = String.format("%s %s %s %s", StringUtils.trim(prefix.toString()), StringUtils.trim(given.toString()),
                        StringUtils.trim(family.toString()), suffix);
            }
        }
        return StringUtils.trim(author);
    }

    private String getClinicalDocumentConfidentialityDisplay(Document doc) {
        List<Node> nodeList = XMLUtil.getNodeList(doc, "ClinicalDocument/confidentialityCode");
        String display = "";
        for (Node node : nodeList) {
            if (node.getAttributes().getNamedItem("displayName") != null) {
                display = node.getAttributes().getNamedItem("displayName").getTextContent();
                logger.debug("confidentiality displayName: '{}'", display);
            }
        }
        return StringUtils.trim(display);
    }

    private Date getCreationDateFromDocument(Document doc) throws ParseException {
        List<Node> nodeList = XMLUtil.getNodeList(doc, "ClinicalDocument/effectiveTime");
        Date creationDate = null;
        for (Node node : nodeList) {
            if (node.getAttributes().getNamedItem("value") != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
                creationDate = simpleDateFormat.parse(node.getAttributes().getNamedItem("value").getTextContent());
                logger.debug("creationDate: '{}'", creationDate);
            }
        }
        return creationDate;
    }

    private String getClinicalDocumentConfidentialityCode(Document doc) {
        List<Node> nodeList = XMLUtil.getNodeList(doc, "ClinicalDocument/confidentialityCode");
        String code = "";
        for (Node node : nodeList) {
            if (node.getAttributes().getNamedItem("code") != null) {
                code = node.getAttributes().getNamedItem("code").getTextContent();
                logger.debug("confidentiality code: '{}'", code);
            }
        }
        return StringUtils.trim(code);
    }

    private String getClinicalDocumentLanguage(Document doc) {
        List<Node> nodeList = XMLUtil.getNodeList(doc, "ClinicalDocument/languageCode");
        String documentLanguage = "";
        for (Node node : nodeList) {
            if (node.getAttributes().getNamedItem("code") != null) {
                documentLanguage = node.getAttributes().getNamedItem("code").getTextContent();
                logger.debug("clinical Document language: '{}'", documentLanguage);
            }
        }
        return StringUtils.trim(documentLanguage);
    }

    @Override
    public DocumentAssociation<PSDocumentMetaData> getPSDocumentList(SearchCriteria searchCriteria) {

        logger.info("[National Infrastructure Mock] Get Patient Summary Document List: '{}'", searchCriteria.toString());
        for (DocumentAssociation<PSDocumentMetaData> documentAssociation : psDocumentMetaDatas) {

            if (documentAssociation.getXMLDocumentMetaData() != null) {
                logger.debug("Patient ID: '{}'", documentAssociation.getXMLDocumentMetaData().getPatientId());
            } else {
                logger.debug("Document Association is null");
            }
            if (documentAssociation.getXMLDocumentMetaData() != null
                    && StringUtils.equals(documentAssociation.getXMLDocumentMetaData().getPatientId(), searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                logger.debug("getPSDocumentList(SearchCriteria searchCriteria): '{}'", documentAssociation);
                return documentAssociation;
            }
        }
        return null;
    }

    @Override
    public List<DocumentAssociation<EPDocumentMetaData>> getEPDocumentList(SearchCriteria searchCriteria) {

        logger.info("[National Infrastructure Mock] Get ePrescription Document List: '{}'", searchCriteria.toString());
        List<DocumentAssociation<EPDocumentMetaData>> metaDatas = new ArrayList<>();

        for (DocumentAssociation<EPDocumentMetaData> documentAssociation : epDocumentMetaDatas) {
            if (documentAssociation.getXMLDocumentMetaData() != null
                    && StringUtils.equals(documentAssociation.getXMLDocumentMetaData().getPatientId(), searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                metaDatas.add(documentAssociation);
                logger.debug("getEPDocumentList(SearchCriteria searchCriteria): '{}'", documentAssociation);
            }
        }
        return metaDatas;
    }

    @Override
    public List<OrCDDocumentMetaData> getOrCDHospitalDischargeReportsDocumentList(SearchCriteria searchCriteria) {
        logger.info("[National Infrastructure Mock] Get Original Clinical Document List for Hospital Discharge Reports: '{}'", searchCriteria.toString());
        return getOrCDDocumentList(searchCriteria, orCDDocumentHospitalDischargeReportsMetaDatas);
    }

    @Override
    public List<OrCDDocumentMetaData> getOrCDLaboratoryResultsDocumentList(SearchCriteria searchCriteria) {
        logger.info("[National Infrastructure Mock] Get Original Clinical Document List for Laboratory results: '{}'", searchCriteria.toString());
        return getOrCDDocumentList(searchCriteria, orCDDocumentLaboratoryResultsMetaDatas);
    }

    @Override
    public List<OrCDDocumentMetaData> getOrCDMedicalImagingReportsDocumentList(SearchCriteria searchCriteria) {
        logger.info("[National Infrastructure Mock] Get Original Clinical Document List for Medical Imaging Reports: '{}'", searchCriteria.toString());
        return getOrCDDocumentList(searchCriteria, orCDDocumentMedicalImagingReportsMetaDatas);
    }

    @Override
    public List<OrCDDocumentMetaData> getOrCDMedicalImagesDocumentList(SearchCriteria searchCriteria) {
        logger.info("[National Infrastructure Mock] Get Original Clinical Document List for Medical Images: '{}'", searchCriteria.toString());
        return getOrCDDocumentList(searchCriteria, orCDDocumentMedicalImagesMetaDatas);
    }

    private List<OrCDDocumentMetaData> getOrCDDocumentList(SearchCriteria searchCriteria, List<OrCDDocumentMetaData> orCDMetaDataList) {
        List<OrCDDocumentMetaData> metaDatas = new ArrayList<>();

        Long maximumSize = null;
        String maximumSizeCriteriaString = searchCriteria.getCriteriaValue(Criteria.MaximumSize);
        if (!StringUtils.isEmpty(maximumSizeCriteriaString)) {
            maximumSize = Long.parseLong(maximumSizeCriteriaString);
        }
        Instant createdAfter = null;
        String createdAfterCriteriaString = searchCriteria.getCriteriaValue(Criteria.CreatedAfter);
        if (createdAfterCriteriaString != null) {
            createdAfter = Instant.parse(createdAfterCriteriaString);
        }
        Instant createdBefore = null;
        String createdBeforeCriteriaString = searchCriteria.getCriteriaValue(Criteria.CreatedBefore);
        if (createdBeforeCriteriaString != null) {
            createdBefore = Instant.parse(createdBeforeCriteriaString);
        }

        for (OrCDDocumentMetaData orCDDocumentMetaData : orCDMetaDataList) {
            Instant creationInstant = orCDDocumentMetaData.getEffectiveTime().toInstant();
            if (StringUtils.equals(orCDDocumentMetaData.getPatientId(), searchCriteria.getCriteriaValue(Criteria.PatientId))
                    && (maximumSize == null || orCDDocumentMetaData.getSize() <= maximumSize)
                    && (createdBefore == null || (creationInstant.compareTo(createdBefore) <= 0))
                    && (createdAfter == null || (createdAfter.compareTo(creationInstant) <= 0))) {
                metaDatas.add(orCDDocumentMetaData);
                logger.debug("getOrCDDocumentList(SearchCriteria searchCriteria): '{}'", orCDDocumentMetaData);
            }
        }
        return metaDatas;
    }

    @Override
    public EPSOSDocument getDocument(SearchCriteria searchCriteria) {

        logger.info("[National Infrastructure Mock] Retrieve Document: '{}', '{}', '{}'", searchCriteria.getCriteriaValue(Criteria.DocumentId),
                searchCriteria.getCriteriaValue(Criteria.PatientId), searchCriteria.getCriteriaValue(Criteria.RepositoryId));
        for (EPSOSDocument epsosDocument : documents) {
            if (epsosDocument.matchesCriteria(searchCriteria)) {
                logger.debug("getDocument(SearchCriteria searchCriteria): '{}'", epsosDocument);
                return epsosDocument;
            }
        }

        return null;
    }

    @Override
    public DocumentAssociation<MroDocumentMetaData> getMroDocumentList(SearchCriteria searchCriteria) {

        logger.info("[National Infrastructure Mock] Get Medication Related Overview Document List: '{}'", searchCriteria.toString());
        for (DocumentAssociation<MroDocumentMetaData> documentAssociation : mroDocumentMetaDatas) {

            if (documentAssociation.getXMLDocumentMetaData() != null
                    && documentAssociation.getXMLDocumentMetaData().getPatientId().equals(searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                logger.debug("getMroDocumentList(SearchCriteria searchCriteria): '{}'", documentAssociation);
                return documentAssociation;
            }
        }

        return null;
    }

    /**
     * @param document
     * @return
     */
    private String getOIDFromDocument(Document document) {

        String oid = "";
        if (document.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "id").getLength() > 0) {
            Node id = document.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "id").item(0);
            if (id.getAttributes().getNamedItem("root") != null) {
                oid = oid + id.getAttributes().getNamedItem("root").getTextContent();
            }
            if (id.getAttributes().getNamedItem(CONSTANT_EXTENSION) != null) {
                oid = oid + "^" + id.getAttributes().getNamedItem(CONSTANT_EXTENSION).getTextContent();
            }
        }
        logger.debug("CDA Document ID: '{}'", oid);
        return oid;
    }

    /**
     * @param doc
     * @return
     */
    private String getTitleFromDocument(Document doc) {

        NodeList documentNames = doc.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "title");

        if (documentNames != null && documentNames.getLength() > 0) {
            Node titleNode = documentNames.item(0);
            return titleNode.getTextContent();
        }
        logger.debug("Could not locate the title of the prescription");
        return "Document Title Not Available";
    }


    private Element getProductFromPrescription(Document document) {
        NodeList elements = document.getElementsByTagNameNS(EHDSI_EPSOS_MEDICATION_NAMESPACE, "generalizedMedicineClass");
        if (elements.getLength() == 0) {
            return null;
        }
        NodeList children = elements.item(0)
                .getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (Objects.equals(Node.ELEMENT_NODE, node.getNodeType()) &&
                    Objects.equals("code", node.getLocalName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private void wrapPDFinCDA(byte[] pdf, Document doc) {

        logger.debug("NameSpace: '{}', Document URI '{}', XML encoding: '{}', BaseURI: '{}'", doc.getNamespaceURI(),
                doc.getDocumentURI(), doc.getXmlEncoding(), doc.getBaseURI());

        // Remove old component element
        Node oldComponent = doc.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "component").item(0);
        Element newComponent = doc.createElementNS(EHDSI_HL7_NAMESPACE, "component");

        // Replace templateID value
        Node templateId = doc.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "templateId").item(0);
        Node value = templateId.getAttributes().getNamedItem("root");
        String val = value.getNodeValue();
        String updatedValue = StringUtils.equals(val, EHDSI_PS_L3_TEMPLATE_ID) ? EHDSI_PS_L1_TEMPLATE_ID : EHDSI_EP_L1_TEMPLATE_ID;
        value.setNodeValue(updatedValue);

        // Add new component element
        Element nonXMLBody = doc.createElementNS(EHDSI_HL7_NAMESPACE, "nonXMLBody");
        nonXMLBody.setAttribute("classCode", "DOCBODY");
        nonXMLBody.setAttribute("moodCode", "EVN");
        Element text = doc.createElementNS(EHDSI_HL7_NAMESPACE, "text");

        text.setAttribute("mediaType", "application/pdf");
        text.setAttribute("representation", "B64");
        text.setTextContent(new String(Base64.encodeBase64(pdf)));

        nonXMLBody.appendChild(text);
        newComponent.appendChild(nonXMLBody);

        Node rootNode = doc.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "ClinicalDocument").item(0);

        rootNode.replaceChild(newComponent, oldComponent);
        logger.debug("PDF document added.");
    }

    /**
     * @param document
     * @param format
     */
    private void addFormatToOID(Document document, int format) {

        if (document.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "id").getLength() > 0) {
            Element id = (Element) document.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "id").item(0);
            if (id.hasAttribute(CONSTANT_EXTENSION)) {
                id.setAttribute(CONSTANT_EXTENSION, id.getAttribute(CONSTANT_EXTENSION) + "." + format);
            } else {
                id.setAttribute(CONSTANT_EXTENSION, Integer.toString(format));
            }
        }
    }

    private String getDescriptionFromDocument(Document doc) {
        XPathFactory factory = XPathFactory.newInstance();
        XPath path = factory.newXPath();
        String description = null;

        try {
            description = path.evaluate("//*[local-name()='manufacturedMaterial']/*[local-name()='name']/text()", doc) +
                    ", " + path.evaluate("//*[local-name()='manufacturedMaterial']/*[local-name()='formCode']/@displayName", doc) +
                    ", " + path.evaluate("//*[local-name()='manufacturedMaterial']/*[local-name()='desc']/text()", doc);
        } catch (XPathExpressionException e) {
            logger.error("XPath expression error", e);
        }

        return description;
    }

}
