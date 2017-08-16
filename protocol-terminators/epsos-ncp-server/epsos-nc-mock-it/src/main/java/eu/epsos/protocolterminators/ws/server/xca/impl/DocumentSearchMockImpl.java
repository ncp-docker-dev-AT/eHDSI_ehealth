package eu.epsos.protocolterminators.ws.server.xca.impl;

import eu.epsos.protocolterminators.integrationtest.cda.CdaUtils;
import eu.epsos.protocolterminators.ws.server.common.NationalConnectorGateway;
import eu.epsos.protocolterminators.ws.server.common.ResourceList;
import eu.epsos.protocolterminators.ws.server.common.ResourceLoader;
import eu.epsos.protocolterminators.ws.server.exception.NIException;
import eu.epsos.protocolterminators.ws.server.xca.DocumentSearchInterface;
import fi.kela.se.epsos.data.model.*;
import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.data.model.PatientDemographics;
import tr.com.srdc.epsos.securityman.exceptions.InsufficientRightsException;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author konstantin.hypponen@kela.fi Note this is a very dirty implementation
 */
public class DocumentSearchMockImpl extends NationalConnectorGateway implements DocumentSearchInterface {

    private static final String PATTERN_EP = "epstore.+\\.xml";
    private static final String PATTERN_PS = "psstore.+\\.xml";
    private static final String PATTERN_MRO = "mrostore.+\\.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSearchMockImpl.class);
    private List<DocumentAssociation<EPDocumentMetaData>> epDocumentMetaDatas = new ArrayList<>();
    private List<DocumentAssociation<PSDocumentMetaData>> psDocumentMetaDatas = new ArrayList<>();
    private List<DocumentAssociation<MroDocumentMetaData>> mroDocumentMetaDatas = new ArrayList<>();
    private List<EPSOSDocument> documents = new ArrayList<>();

    public DocumentSearchMockImpl() {

        Collection<String> documentlist = ResourceList.getResources(Pattern.compile(PATTERN_EP));
        ResourceLoader resourceLoader = new ResourceLoader();

        // Mocked ePrescription fill up
        for (String xmlFilename : documentlist) {

            LOGGER.debug("Reading file '{}'", xmlFilename);
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

                EPDocumentMetaData epdXml = DocumentFactory.createEPDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(), new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), "author");
                LOGGER.debug("Placed XML doc id='{}' HomeCommId='{}', Patient Id: '{}' into eP repository", epdXml.getId(), Constants.HOME_COMM_ID, pd.getId());
                documents.add(DocumentFactory.createEPSOSDocument(epdXml.getPatientId(), epdXml.getClassCode(), xmlDoc));

                EPDocumentMetaData epdPdf = DocumentFactory.createEPDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(), new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), "author");
                LOGGER.debug("Placed PDF doc id='{}' into eP repository", epdPdf.getId());
                documents.add(DocumentFactory.createEPSOSDocument(epdPdf.getPatientId(), epdPdf.getClassCode(), pdfDoc));

                epDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(epdXml, epdPdf));
            } catch (Exception e) {
                LOGGER.warn("Could not read file at" + xmlFilename, e);
            }
        }

        // Mocked Patient Summaries fill up

        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_PS));

        for (String xmlFilename : documentlist) {
            LOGGER.debug("Reading file '{}", xmlFilename);
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
                LOGGER.debug("Adding format to the document's OID");
                addFormatToOID(pdfDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF);
                LOGGER.debug("Parsing PS patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                PSDocumentMetaData psdPdf = DocumentFactory.createPSDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(), new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(pdfDoc), "author");
                documents.add(DocumentFactory.createEPSOSDocument(psdPdf.getPatientId(), psdPdf.getClassCode(), pdfDoc));
                PSDocumentMetaData psdXml = DocumentFactory.createPSDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(), new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), "author");
                documents.add(DocumentFactory.createEPSOSDocument(psdXml.getPatientId(), psdPdf.getClassCode(), xmlDoc));
                LOGGER.debug("Placed PDF doc id=" + psdPdf.getId() + " into PS repository");
                LOGGER.debug("Placed XML doc id=" + psdXml.getId() + " into PS repository");

                psDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(psdXml, psdPdf));
            } catch (Exception e) {
                LOGGER.warn("Could not read file at " + xmlFilename, e);
            }
        }

        // Mocked MROs fill up

        documentlist = ResourceList.getResources(Pattern.compile(PATTERN_MRO));

        for (String xmlFilename : documentlist) {
            LOGGER.debug("reading file '{}'", xmlFilename);
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
                LOGGER.debug("Adding format to the document's OID");
                addFormatToOID(pdfDoc, EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF);
                LOGGER.debug("Parsing MRO patient demographics");
                PatientDemographics pd = CdaUtils.getPatientDemographicsFromXMLDocument(xmlDoc);

                MroDocumentMetaData psdPdf = DocumentFactory.createMroDocumentPDF(getOIDFromDocument(pdfDoc), pd.getId(), new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(pdfDoc), "author");
                documents.add(DocumentFactory.createEPSOSDocument(psdPdf.getPatientId(), psdPdf.getClassCode(), pdfDoc));
                MroDocumentMetaData psdXml = DocumentFactory.createMroDocumentXML(getOIDFromDocument(xmlDoc), pd.getId(), new Date(), Constants.HOME_COMM_ID, getTitleFromDocument(xmlDoc), "author");
                documents.add(DocumentFactory.createEPSOSDocument(psdXml.getPatientId(), psdPdf.getClassCode(), xmlDoc));
                LOGGER.debug("Placed PDF doc id='{}' into MRO repository", psdPdf.getId());
                LOGGER.debug("Placed XML doc id='{}' into MRO repository", psdXml.getId());

                mroDocumentMetaDatas.add(DocumentFactory.createDocumentAssociation(psdXml, psdPdf));
            } catch (Exception e) {
                LOGGER.warn("Could not read file at " + xmlFilename, e);
            }
        }
    }

    private static Document loadCDADocument(String content) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(content));
        return docBuilder.parse(is);
    }

    @Override
    public DocumentAssociation<PSDocumentMetaData> getPSDocumentList(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException {
        LOGGER.info("getPSDocumentList(SearchCriteria searchCriteria): '{}'", searchCriteria.toString());
        for (DocumentAssociation<PSDocumentMetaData> da : psDocumentMetaDatas) {
            LOGGER.info("loop: '{}'", da.toString());
            if (da.getXMLDocumentMetaData() != null
                    && da.getXMLDocumentMetaData().getPatientId().equals(searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                LOGGER.info("getPSDocumentList(SearchCriteria searchCriteria): '{}'", da.toString());
                return da;
            }
        }

        return null;
    }

    @Override
    public List<DocumentAssociation<EPDocumentMetaData>> getEPDocumentList(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException {

        LOGGER.info("getEPDocumentList(SearchCriteria searchCriteria)");
        List<DocumentAssociation<EPDocumentMetaData>> metaDatas = new ArrayList<>();

        for (DocumentAssociation<EPDocumentMetaData> da : epDocumentMetaDatas) {
            if (da.getXMLDocumentMetaData() != null
                    && da.getXMLDocumentMetaData().getPatientId().equals(searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                metaDatas.add(da);
                LOGGER.info("getEPDocumentList(SearchCriteria searchCriteria): '{}'", da.toString());
            }
        }
        return metaDatas;
    }

    @Override
    public EPSOSDocument getDocument(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException {
        LOGGER.info("getDocument(SearchCriteria searchCriteria)");
        for (EPSOSDocument doc : documents) {
            if (doc.matchesCriteria(searchCriteria)) {
                LOGGER.info("getDocument(SearchCriteria searchCriteria): '{}'", doc.toString());
                return doc;
            }
        }

        return null;
    }

    @Override
    public DocumentAssociation<MroDocumentMetaData> getMroDocumentList(SearchCriteria searchCriteria) throws NIException, InsufficientRightsException {

        LOGGER.info("getMroDocumentList(SearchCriteria searchCriteria): '{}'", searchCriteria.toString());
        for (DocumentAssociation<MroDocumentMetaData> da : mroDocumentMetaDatas) {
            LOGGER.info("loop: '{}'", da.toString());
            if (da.getXMLDocumentMetaData() != null
                    && da.getXMLDocumentMetaData().getPatientId().equals(searchCriteria.getCriteriaValue(Criteria.PatientId))) {
                LOGGER.info("getMroDocumentList(SearchCriteria searchCriteria): '{}'", da.toString());
                return da;
            }
        }

        return null;
    }

    private String getOIDFromDocument(Document doc) {
        String oid = "";
        if (doc.getElementsByTagName("id").getLength() > 0) {
            Node id = doc.getElementsByTagName("id").item(0);
            if (id.getAttributes().getNamedItem("root") != null) {
                oid = oid + id.getAttributes().getNamedItem("root").getTextContent();
            }
            if (id.getAttributes().getNamedItem("extension") != null) {
                oid = oid + "^" + id.getAttributes().getNamedItem("extension").getTextContent();
            }
        }
        return oid;
    }

    private String getTitleFromDocument(Document doc) {
        if (doc.getElementsByTagName("epsos:name").getLength() > 0) {
            Node titleNode = doc.getElementsByTagName("epsos:name").item(0);
            return titleNode.getTextContent();
        }
        LOGGER.debug("Could not locate the title of the prescription");
        return "ePrescription";
    }

    private void wrapPDFinCDA(byte[] pdf, Document doc) {

        LOGGER.info("NameSpace: '{}', Document URI '{}', XML encoding: '{}', BaseURI: '{}'", doc.getNamespaceURI(), doc.getDocumentURI(), doc.getXmlEncoding(), doc.getBaseURI());
        // Remove old component element
        Node oldComponent = doc.getElementsByTagName("component").item(0);

        Element newComponent = doc.createElementNS("urn:hl7-org:v3", "component");
        //doc.createElement("component").setAttributeNS();

        // Add new component element
        Element nonXMLBody = doc.createElementNS("urn:hl7-org:v3", "nonXMLBody");
        Element text = doc.createElementNS("urn:hl7-org:v3", "text");

        text.setAttribute("mediaType", "application/pdf");
        text.setAttribute("representation", "B64");
        text.setTextContent(new String(Base64.encodeBase64(pdf)));

        nonXMLBody.appendChild(text);
        newComponent.appendChild(nonXMLBody);

        Node rootNode = doc.getFirstChild();
        LOGGER.info("Adding PDF document. Removing old component node and adding a new.");
        LOGGER.info("Old Namespace: '{}'", oldComponent.getNamespaceURI());
        LOGGER.info("New Namespace: '{}'", newComponent.getNamespaceURI());
//        rootNode.removeChild(oldComponent);
//        rootNode.appendChild(newComponent);
        rootNode.replaceChild(newComponent, oldComponent);
        LOGGER.info("PDF document added.");
    }

    private void addFormatToOID(Document doc, int format) {
        if (doc.getElementsByTagName("id").getLength() > 0) {
            Element id = (Element) doc.getElementsByTagName("id").item(0);
            if (id.hasAttribute("extension")) {
                id.setAttribute("extension", id.getAttribute("extension") + "." + format);
            } else {
                id.setAttribute("extension", Integer.toString(format));
            }
        }
    }
}
