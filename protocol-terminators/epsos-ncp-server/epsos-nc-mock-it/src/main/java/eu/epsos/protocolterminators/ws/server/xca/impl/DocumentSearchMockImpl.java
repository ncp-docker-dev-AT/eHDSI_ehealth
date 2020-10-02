package eu.epsos.protocolterminators.ws.server.xca.impl;

import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.common.ResourceList;
import eu.epsos.protocolterminators.ws.server.common.ResourceLoader;
import eu.epsos.protocolterminators.ws.server.util.NationalConnectorUtil;
import eu.epsos.protocolterminators.ws.server.xca.DocumentSearchInterface;
import eu.europa.ec.sante.ehdsi.openncp.mock.util.CdaUtils;
import fi.kela.se.epsos.data.model.*;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.codec.binary.Base64;
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
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author konstantin.hypponen@kela.fi Note this is a very dirty implementation
 */
public class DocumentSearchMockImpl extends NationalConnectorGateway implements DocumentSearchInterface {

    private static final String PATTERN_EP = "epstore.+\\.xml";
    private static final String PATTERN_PS = "psstore.+\\.xml";
    private static final String PATTERN_MRO = "mrostore.+\\.xml";
    private static final String CONSTANT_EXTENSION = "extension";
    private static final String EHDSI_HL7_NAMESPACE = "urn:hl7-org:v3";
    private static final String EHDSI_EPSOS_MEDICATION_NAMESPACE = "urn:epsos-org:ep:medication";
    private final Logger logger = LoggerFactory.getLogger(DocumentSearchMockImpl.class);
    private final List<DocumentAssociation<EPDocumentMetaData>> epDocumentMetaDatas = new ArrayList<>();
    private final List<DocumentAssociation<PSDocumentMetaData>> psDocumentMetaDatas = new ArrayList<>();
    private final List<DocumentAssociation<MroDocumentMetaData>> mroDocumentMetaDatas = new ArrayList<>();
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
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc), description, productCode, productName, true);
                logger.debug("Placed XML doc id='{}' HomeCommId='{}', Patient Id: '{}' into eP repository",
                        epdXml.getId(), Constants.HOME_COMM_ID, pd.getId());
                documents.add(DocumentFactory.createEPSOSDocument(epdXml.getPatientId(), epdXml.getClassCode(), xmlDoc));

                EPDocumentMetaData epdPdf = DocumentFactory.createEPDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc), description, productCode, productName, true);
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
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(pdfDoc), getClinicalDocumentAuthor(xmlDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdPdf.getPatientId(), psdPdf.getClassCode(), pdfDoc));
                PSDocumentMetaData psdXml = DocumentFactory.createPSDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdXml.getPatientId(), psdPdf.getClassCode(), xmlDoc));
                logger.debug("Placed PDF doc id=" + psdPdf.getId() + " into PS repository");
                logger.debug("Placed XML doc id=" + psdXml.getId() + " into PS repository");

                psDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(psdXml, psdPdf));
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
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(pdfDoc), getClinicalDocumentAuthor(xmlDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdPdf.getPatientId(), psdPdf.getClassCode(), pdfDoc));
                MroDocumentMetaData psdXml = DocumentFactory.createMroDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(),
                        new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), getClinicalDocumentAuthor(xmlDoc));
                documents.add(DocumentFactory.createEPSOSDocument(psdXml.getPatientId(), psdPdf.getClassCode(), xmlDoc));
                logger.debug("Placed PDF doc id='{}' into MRO repository", psdPdf.getId());
                logger.debug("Placed XML doc id='{}' into MRO repository", psdXml.getId());

                mroDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(psdXml, psdPdf));
            } catch (Exception e) {
                logger.warn("Could not read file at " + xmlFilename, e);
            }
        }
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
                        StringUtils.trim(family.toString()), suffix.toString());
            }
        }
        return StringUtils.trim(author);
    }


    @Override
    public DocumentAssociation<PSDocumentMetaData> getPSDocumentList(SearchCriteria searchCriteria) {

        if (logger.isInfoEnabled()) {
            logger.info("getPSDocumentList(SearchCriteria searchCriteria): '{}'", searchCriteria.toString());
        }
        for (DocumentAssociation<PSDocumentMetaData> documentAssociation : psDocumentMetaDatas) {

            if (documentAssociation.getXMLDocumentMetaData() != null) {
                logger.info("Patient ID: '{}'", documentAssociation.getXMLDocumentMetaData().getPatientId());
            } else {
                logger.info("Document Association is null");
            }
            if (documentAssociation.getXMLDocumentMetaData() != null
                    && StringUtils.equals(documentAssociation.getXMLDocumentMetaData().getPatientId(), searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                if (logger.isInfoEnabled()) {
                    logger.info("getPSDocumentList(SearchCriteria searchCriteria): '{}'", documentAssociation.toString());
                }
                return documentAssociation;
            }
        }
        return null;
    }

    @Override
    public List<DocumentAssociation<EPDocumentMetaData>> getEPDocumentList(SearchCriteria searchCriteria) {

        if (logger.isInfoEnabled()) {
            logger.info("getEPDocumentList(SearchCriteria searchCriteria)");
        }
        List<DocumentAssociation<EPDocumentMetaData>> metaDatas = new ArrayList<>();

        for (DocumentAssociation<EPDocumentMetaData> documentAssociation : epDocumentMetaDatas) {
            if (documentAssociation.getXMLDocumentMetaData() != null
                    && StringUtils.equals(documentAssociation.getXMLDocumentMetaData().getPatientId(), searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                metaDatas.add(documentAssociation);
                if (logger.isInfoEnabled()) {
                    logger.info("getEPDocumentList(SearchCriteria searchCriteria): '{}'", documentAssociation.toString());
                }
            }
        }
        return metaDatas;
    }

    @Override
    public EPSOSDocument getDocument(SearchCriteria searchCriteria) {

        logger.info("[NI] Get Document: '{}', '{}', '{}'", searchCriteria.getCriteriaValue(Criteria.DocumentId),
                searchCriteria.getCriteriaValue(Criteria.PatientId), searchCriteria.getCriteriaValue(Criteria.RepositoryId));
        for (EPSOSDocument doc : documents) {
            if (doc.matchesCriteria(searchCriteria)) {
                if (logger.isInfoEnabled()) {
                    logger.info("getDocument(SearchCriteria searchCriteria): '{}'", doc.toString());
                }
                return doc;
            }
        }

        return null;
    }

    @Override
    public DocumentAssociation<MroDocumentMetaData> getMroDocumentList(SearchCriteria searchCriteria) {

        if (logger.isInfoEnabled()) {
            logger.info("getMroDocumentList(SearchCriteria searchCriteria): '{}'", searchCriteria.toString());
        }
        for (DocumentAssociation<MroDocumentMetaData> da : mroDocumentMetaDatas) {

            if (da.getXMLDocumentMetaData() != null
                    && da.getXMLDocumentMetaData().getPatientId().equals(searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                if (logger.isInfoEnabled()) {
                    logger.info("getMroDocumentList(SearchCriteria searchCriteria): '{}'", da.toString());
                }
                return da;
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
        logger.info("CDA Document ID: '{}'", oid);
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

        logger.info("NameSpace: '{}', Document URI '{}', XML encoding: '{}', BaseURI: '{}'", doc.getNamespaceURI(),
                doc.getDocumentURI(), doc.getXmlEncoding(), doc.getBaseURI());

        // Remove old component element
        Node oldComponent = doc.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "component").item(0);
        Element newComponent = doc.createElementNS(EHDSI_HL7_NAMESPACE, "component");

        // Add new component element
        Element nonXMLBody = doc.createElementNS(EHDSI_HL7_NAMESPACE, "nonXMLBody");
        Element text = doc.createElementNS(EHDSI_HL7_NAMESPACE, "text");

        text.setAttribute("mediaType", "application/pdf");
        text.setAttribute("representation", "B64");
        text.setTextContent(new String(Base64.encodeBase64(pdf)));

        nonXMLBody.appendChild(text);
        newComponent.appendChild(nonXMLBody);

        Node rootNode = doc.getElementsByTagNameNS(EHDSI_HL7_NAMESPACE, "ClinicalDocument").item(0);

        rootNode.replaceChild(newComponent, oldComponent);
        logger.info("PDF document added.");
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
