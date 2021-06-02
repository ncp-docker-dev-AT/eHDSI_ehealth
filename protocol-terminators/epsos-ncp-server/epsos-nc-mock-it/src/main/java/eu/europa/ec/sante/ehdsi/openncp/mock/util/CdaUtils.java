package eu.europa.ec.sante.ehdsi.openncp.mock.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import tr.com.srdc.epsos.data.model.PatientDemographics;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.text.ParseException;
import java.util.Iterator;

public class CdaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdaUtils.class);
    private static final NamespaceContext hl7 = new NamespaceContext() {
        public String getNamespaceURI(String prefix) {
            String uri;
            if (StringUtils.equals(prefix, "hl7")) {
                uri = "urn:hl7-org:v3";
            } else {
                uri = null;
            }
            return uri;
        }

        public Iterator getPrefixes(String val) {
            return null;
        }

        public String getPrefix(String uri) {
            return null;
        }
    };

    private CdaUtils() {
    }

    /**
     * Returns PatientDemographics information from a CDA document
     *
     * @param doc CDA document
     * @return PatientDemographics object extracted from the patientRole in
     * Header
     */
    public static PatientDemographics getPatientDemographicsFromXMLDocument(Document doc) {

        PatientDemographics patientDemographics = new PatientDemographics();

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(hl7);
        try {
            String extension = xPath.evaluate("/hl7:ClinicalDocument/hl7:recordTarget/hl7:patientRole/hl7:id/@extension", doc);
            patientDemographics.setId(extension);
        } catch (XPathExpressionException e) {
            LOGGER.warn("Could not find patient's id in the CDA document: '{}'", e.getMessage(), e);
        }

        try {
            NodeList givenNames = (NodeList) xPath.evaluate("/hl7:ClinicalDocument/hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:name/hl7:given", doc, XPathConstants.NODESET);
            for (int i = 0; i < givenNames.getLength(); i++) {
                // Skip the given names with attributes such as "call me"
                if (givenNames.item(i).hasAttributes()) {
                    continue;
                }
                if (patientDemographics.getGivenName() == null) {
                    patientDemographics.setGivenName(givenNames.item(i).getTextContent());
                } else {
                    patientDemographics.setGivenName(patientDemographics.getGivenName() + " " + givenNames.item(i).getTextContent());
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.warn("Could not find patient's given name in the CDA document: '{}'", e.getMessage(), e);
        }

        try {
            NodeList familyNames = (NodeList) xPath.evaluate("/hl7:ClinicalDocument/hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:name/hl7:family", doc, XPathConstants.NODESET);
            for (int i = 0; i < familyNames.getLength(); i++) {
                if (patientDemographics.getFamilyName() == null) {
                    patientDemographics.setFamilyName(familyNames.item(i).getTextContent());
                } else {
                    patientDemographics.setFamilyName(patientDemographics.getFamilyName() + " " + familyNames.item(i).getTextContent());
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.warn("Could not find patient's family name in the CDA document: '{}'", e.getMessage(), e);
        }


        try {
            String administrativeGenderCode = xPath.evaluate("/hl7:ClinicalDocument/hl7:recordTarget/hl7:patientRole/hl7:patient/hl7:administrativeGenderCode/@code", doc);
            patientDemographics.setAdministrativeGender(PatientDemographics.Gender.parseGender(administrativeGenderCode));
        } catch (XPathExpressionException e) {
            LOGGER.warn("Could not find patient's administrative gender code in the CDA document: '{}'", e.getMessage(), e);
        } catch (ParseException pe) {
            LOGGER.error("Error parsing patient administrative gender code.");
        }

        return patientDemographics;
    }

    /**
     * Returns The classCode from a CDA document
     *
     * @param doc CDA document
     * @return CDA Document classCode
     */
    public static String getClassCodeFromXMLDocument(Document doc) {

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(hl7);
        String classCode = StringUtils.EMPTY;
        try {
            classCode = xPath.evaluate("/hl7:ClinicalDocument/hl7:code/@code", doc);
        } catch (XPathExpressionException e) {
            LOGGER.warn("Could not find classCode in the CDA document: '{}'", e.getMessage(), e);
        }
        return classCode;
    }
}
