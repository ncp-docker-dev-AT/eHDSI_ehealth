package com.gnomon.epsos.model.cda;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CDAUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDAUtils.class);

    private final static String XML_LOINC_SYSTEM = "LOINC",
            XML_LOINC_CODESYSTEM = "2.16.840.1.113883.6.1",
            XML_PRESCRIPTION_TEMPLATEID = "1.3.6.1.4.1.12559.11.10.1.3.1.1.1",
            XML_PRESCRIPTION_ENTRY_TEMPLATEID = "1.3.6.1.4.1.12559.11.10.1.3.1.2.1",
            XML_DISPENSATION_TEMPLATEID = "1.3.6.1.4.1.12559.11.10.1.3.1.1.2",
            XML_DISPENSATION_LOINC_CODE = "60593-1",
            XML_DISPENSATION_LOINC_CODESYSTEMNAME = "LOINC",
            XML_DISPENSATION_LOINC_CODESYSTEM = "2.16.840.1.113883.6.1",
            XML_CONSENT_TITLE = "Consent to Share Information",
            XML_DISPENSATION_TITLE = "Medication dispensed",
            XML_DISPENSATION_ENTRY_TEMPLATEID = "1.3.6.1.4.1.12559.11.10.1.3.1.2.2",
            XML_DISPENSATION_ENTRY_PARENT_TEMPLATEID = "2.16.840.1.113883.10.20.1.8",
            XML_DISPENSTATION_ENTRY_REFERENCEID = "1.3.6.1.4.1.12559.11.10.1.3.1.3.2",
            XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE1 = "2.16.840.1.113883.10.20.1.34",
            XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE2 = "1.3.6.1.4.1.19376.1.5.3.1.4.7.3",
            XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE3 = "1.3.6.1.4.1.12559.11.10.1.3.1.3.3",
            XML_DISPENSATION_ENTRY_SUPPLY_ID_ROOT = "2.16.840.1.113883.2.19.40.5.410286.10.11",
            XML_DISPENSATION_PERFORMER_ID_ROOT = "1.3.6.1.4.1.19376.1.5.3.1.2.3",
            XML_DISPENSATION_PRODUCT_EPSOSNS = "urn:epsos-org:ep:medication",
            XML_DISPENSATION_PRODUCT_CLASSCODE = "MANU",
            XML_DISPENSATION_PRODUCT_TEMPLATE1 = "1.3.6.1.4.1.19376.1.5.3.1.4.7.2",
            XML_DISPENSATION_PRODUCT_TEMPLATE2 = "2.16.840.1.113883.10.20.1.53",
            XML_DISPENSATION_PRODUCT_MATERIAL_CLASSCODE = "MMAT",
            XML_DISPENSATION_PRODUCT_MATERIAL_CONTENT_CLASSCODE = "CONT",
            XML_DISPENSATION_PRODUCT_MATERIAL_DETERMINERCODE = "KIND",
            XML_DISPENSATION_PRODUCT_MATERIAL_CONTAINER_DETERMINERCODE = "INSTANCE",
            XML_DISPENSATION_PRODUCT_MATERIAL_TEMPLATE = "1.3.6.1.4.1.12559.11.10.1.3.1.3.1",
            XML_DISPENSATION_PRODUCT_MATERIAL_FORMCODE_SYSTEM = "1.3.6.1.4.1.12559.11.10.1.3.1.42.2",
            XML_DISPENSATION_PRODUCT_MATERIAL_CONTAINER_FORMCODE_SYSTEM = "1.3.6.1.4.1.12559.11.10.1.3.1.42.3";

    private CDAUtils() {
    }

    public static String createDispensation(Document epDoc, CDAHeader cda, String eDuuid) throws ParserConfigurationException, SAXException, IOException {

        return CDAModelToEDXML(epDoc, cda, eDuuid);
    }

    private static Document readEpXML(String xml) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    private static XPath getXPathFactory() {

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        xpath.setNamespaceContext(new CDANameSpaceContext());
        return xpath;
    }

    public static String getRelativePrescriptionBarcode(Document doc) {

        NodeList nl;
        String refBarcode = "";

        try {
            XPath xpath = getXPathFactory();
            XPathExpression epExpr = xpath.compile("//xsi:component/xsi:structuredBody/xsi:component/xsi:section//xsi:id/@extension");
            nl = (NodeList) epExpr.evaluate(doc, XPathConstants.NODESET);
            if (nl.item(0) != null) {
                refBarcode = nl.item(0).getNodeValue();
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return refBarcode;
    }

    public static String getRelativePrescriptionBarcode(String xml) {

        String nodeString = "";
        try {
            Document doc = readEpXML(xml);
            nodeString = getRelativePrescriptionBarcode(doc);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return nodeString;
    }

    private static String getRelativePrescriptionLineFromEP(Document epDoc, String id) {

        Node nl;
        String nodeString = "";
        try {
            Document doc = epDoc;
            XPath xpath = getXPathFactory();
            XPathExpression epExpr = xpath.compile("//xsi:substanceAdministration[xsi:id/@extension='" + id + "']");
            nl = (Node) epExpr.evaluate(doc, XPathConstants.NODE);
            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return nodeString;
    }

    private static String getRelativeProductLineFromEP(Document epDoc, String id) {

        Node nl;
        String nodeString = "";
        try {
            Document doc = epDoc;

            XPath xpath = getXPathFactory();

            XPathExpression epExpr = xpath.compile("//xsi:substanceAdministration[xsi:id/@extension='" + id + "']//xsi:consumable//xsi:manufacturedProduct");
            nl = (Node) epExpr.evaluate(doc, XPathConstants.NODE);
            XPathExpression materialExpr = xpath.compile("//xsi:substanceAdministration/xsi:consumable/xsi:manufacturedProduct/xsi:manufacturedMaterial");
            Node oldMaterialNode = (Node) materialExpr.evaluate(epDoc, XPathConstants.NODE);
            XPathExpression code = xpath.compile("//xsi:substanceAdministration/xsi:consumable/xsi:manufacturedProduct/xsi:manufacturedMaterial/xsi:code");
            Node codeNode = (Node) code.evaluate(epDoc, XPathConstants.NODE);

            if (codeNode == null) {
                code = xpath.compile("//xsi:substanceAdministration/xsi:consumable/xsi:manufacturedProduct/xsi:manufacturedMaterial/xsi:name");
                codeNode = (Node) code.evaluate(epDoc, XPathConstants.NODE);
            }

            Node epsosID = epDoc.createElement("epsos:id");
            EpsosHelperService.addAttribute(epDoc, epsosID, "root", XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE3);
            EpsosHelperService.addAttribute(epDoc, epsosID, "extension", id);
            oldMaterialNode.insertBefore(epsosID, codeNode);

            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return nodeString;
    }

    /**
     * A method returning the updated context of the substituted product during
     * the dispensation.
     *
     * @param doc      the cloned prescription document.
     * @param id       the id of the corresponding product.
     * @param product  the substituted name of the product.
     * @param unit     the substituted unit of the product.
     * @param quantity the substituted quantity size of the product.
     * @return the updated context of the substituted product.
     */
    private static String getSubstitutedRelativeProductLineFromEP(org.dom4j.Document doc, String id, String product, String unit, String quantity) {

        //Context of the substituted product
        String context = "";

        try {
            //Adding the namespaces within the document
            Map<String, String> namespaces = new HashMap<>();
            namespaces.put("hl7", "urn:hl7-org:v3");
            namespaces.put("epsos", "urn:epsos-org:ep:medication");
            namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");

            //Creating an xpath expression for the displayName attribute of the code element
            String expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/hl7:code/@displayName";

            //Setting the xpath processor and the namespaces
            org.dom4j.XPath xpath = org.dom4j.DocumentHelper.createXPath(expression);
            xpath.setNamespaceURIs(namespaces);

            //Getting the displayName node
            org.dom4j.Node displayName = xpath.selectSingleNode(doc);

            //Updating the displayName node context
            displayName.setText(product);

            //Creating an xpath expression for the name element
            expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/hl7:name";

            //Setting the xpath procesor and the namespaces
            xpath = org.dom4j.DocumentHelper.createXPath(expression);
            xpath.setNamespaceURIs(namespaces);

            //Getting the name node
            org.dom4j.Node name = xpath.selectSingleNode(doc);

            //Updating the name node context
            name.setText(product);

            //Creating an xpath expression for the epsos:name element
            expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:asContent/epsos:containerPackagedMedicine/epsos:name";

            //Setting the xpath procesor and the namespaces
            xpath = org.dom4j.DocumentHelper.createXPath(expression);
            xpath.setNamespaceURIs(namespaces);

            //Getting the epsos:name node
            org.dom4j.Node epsosName = xpath.selectSingleNode(doc);

            //Updating the name node context
            epsosName.setText(product);

            //TODO epsos:capacityQuantity/@unit
            //Creating an xpath expression for the quantity value attribute of the epsos:capacityQuantity element
            expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:asContent/epsos:containerPackagedMedicine/epsos:capacityQuantity/@value";

            //Setting the xpath procesor and the namespaces
            xpath = org.dom4j.DocumentHelper.createXPath(expression);
            xpath.setNamespaceURIs(namespaces);

            //Getting the quantity value node
            org.dom4j.Node value = xpath.selectSingleNode(doc);

            //Updating the quantity value node context
            value.setText(quantity);

            //Creating an xpath expression for the manufacturedMaterial code element
            expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/hl7:code";

            //Setting the xpath procesor and the namespaces
            xpath = org.dom4j.DocumentHelper.createXPath(expression);
            xpath.setNamespaceURIs(namespaces);

            //Getting the manufacturedMaterial code node
            org.dom4j.Node pivot = xpath.selectSingleNode(doc);

            //Checking if the code element is not provided
            if (pivot == null) {
                //Creating an xpath expression for the manufacturedMaterial name element instead
                expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/hl7:name";

                //Setting the xpath procesor and the namespaces
                xpath = org.dom4j.DocumentHelper.createXPath(expression);
                xpath.setNamespaceURIs(namespaces);

                //Getting the manufacturedMaterial name node
                pivot = xpath.selectSingleNode(doc);
            }

            //Getting the parent manufacturedMaterial element of the pivot
            org.dom4j.Element parent = pivot.getParent();

            //Creating the epsos:id element
            org.dom4j.Element epsosID = org.dom4j.DocumentHelper.createElement("epsos:id");

            //Adding root and extension attributes to the epsos:id element
            epsosID.addAttribute("root", XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE3);
            epsosID.addAttribute("extension", id);

            //Adding the epsos:id element as the first child of the manufacturedMaterial element
            parent.elements().add(0, epsosID);

            //Creating a xpath expression for the manufacturedProduct element
            expression = "//hl7:substanceAdministration[hl7:id/@extension='" + id + "']/hl7:consumable/hl7:manufacturedProduct";

            //Setting the xpath procesor and the namespaces
            xpath = org.dom4j.DocumentHelper.createXPath(expression);
            xpath.setNamespaceURIs(namespaces);

            //Getting the manufacturedProduct node
            org.dom4j.Node manufacturedProduct = xpath.selectSingleNode(doc);

            //Getting the string representation of the node
            context = manufacturedProduct.asXML().replaceAll("xmlns=\"\"", "");
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return context;
    }

    private static String getRecordTargetFromEP(Document epDoc) {

        Node nl;
        String nodeString = "";
        try {
            nl = epDoc.getElementsByTagName("recordTarget").item(0);
            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return nodeString;
    }

    private static String getCustodianFromEP(Document epDoc) {

        Node nl;
        String nodeString = "";
        try {
            nl = epDoc.getElementsByTagName("custodian").item(0);
            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return nodeString;
    }

    private static String getLegalAuthFromEP(Document epDoc) {

        Node nl;
        String nodeString = "";
        try {
            nl = epDoc.getElementsByTagName("legalAuthenticator").item(0);
            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return nodeString;
    }

    private static String getAuthorFromEP(Document epDoc) {

        Node nl;
        String nodeString = "";
        try {
            nl = epDoc.getElementsByTagName("author").item(0);
            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return nodeString;
    }

    private static String getRelativePrescriptionText(Document epDoc) {

        Node nl;
        String nodeString = "";
        try {
            Document doc = epDoc;
            XPath xpath = getXPathFactory();
            XPathExpression epExpr = xpath.compile("//xsi:component/xsi:structuredBody/xsi:component/xsi:section//xsi:text");
            nl = (Node) epExpr.evaluate(doc, XPathConstants.NODE);
            nodeString = Utils.nodeToString(nl);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return nodeString;
    }

    public static String getRelativePrescriptionRoot(Document doc) {

        NodeList nl;
        String refBarcode = "";
        try {
            XPath xpath = getXPathFactory();
            XPathExpression epExpr = xpath.compile("//xsi:component/xsi:structuredBody/xsi:component/xsi:section//xsi:id/@root");
            nl = (NodeList) epExpr.evaluate(doc, XPathConstants.NODESET);
            if (nl.item(0) != null) {
                refBarcode = nl.item(0).getNodeValue();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return refBarcode;
    }

    public static String CDAModelToConsent(CDAHeader cda, String rolename) {

        String edCountry = GetterUtil.getString(ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.country"), "");
        String consentOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CONSENT_OID);
        String patientOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_PATIENTS_OID);
        String custodianOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CUSTODIAN_OID);
        String custodianName = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CUSTODIAN_NAME);
        String legalOrgOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_ORG_OID);
        String legalauthenticatorfirstname = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_FIRSTNAME);
        String legalauthenticatorlastname = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_LASTNAME);
        String legalauthenticatorcity = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_CITY);
        String legalauthenticatorpostalcode = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_POSTALCODE);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("\r\n");

        sb.append("<ClinicalDocument xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"urn:hl7-org:v3 CDA.xsd\">");
        sb.append("\r\n");
        sb.append("<typeId root=\"2.16.840.1.113883.1.3\" extension=\"POCD_MT000040\"/>");
        sb.append("\r\n");
        sb.append("<templateId root=\"1.3.6.1.4.1.19376.1.5.3.1.1.1\"/>");
        sb.append("\r\n");
        sb.append("<templateId root=\"1.3.6.1.4.1.19376.1.5.3.1.1.7\"/>");
        sb.append("\r\n");

        //String uuid = java.util.UUID.randomUUID().toString();
        String uuid = EpsosHelperService.getUniqueId();
        sb.append("<id extension=\"").append(uuid).append("\" root=\"").append(consentOid).append("\"/>");
        sb.append("\r\n");
        sb.append("<code codeSystemName=\"LOINC\" codeSystem=\"2.16.840.1.113883.6.1\" code=\"57016-8\" "
                + "displayName=\"Privacy Policy Acknowledgement Document\"/>");
        sb.append("\r\n");
        sb.append("<title>" + XML_CONSENT_TITLE + "</title>");
        sb.append("\r\n");
        sb.append("<effectiveTime value=\"").append(cda.getEffectiveTime()).append("\" />");
        sb.append("\r\n");
        sb.append("<confidentialityCode code=\"N\" codeSystem=\"2.16.840.1.113883.5.25\"/>");
        sb.append("\r\n");
        sb.append("<languageCode code=\"").append(cda.getLanguageCode().replaceAll("_", "-")).append("\"/>");
        sb.append("\r\n");
        // Record Target
        sb.append("<recordTarget typeCode=\"RCT\">");
        sb.append("\r\n");
        sb.append("<patientRole classCode=\"PAT\">");
        sb.append("\r\n");
        sb.append("<id extension=\"").append(cda.getPatientId()).append("\" root=\"").append(patientOid).append("\"/>");
        sb.append("\r\n");
        sb.append(addAddress(cda.getPatientAddress(), cda.getPatientCity(), cda.getPatientPostalCode(),
                cda.getPatientCountry(), cda.getPatientTelephone(), cda.getPatientEmail(), false));
        sb.append("<patient classCode=\"PSN\" determinerCode=\"INSTANCE\">");
        sb.append("\r\n");
        sb.append(addName(cda.getPatientFamilyName(), cda.getPatientPrefix(), cda.getPatientGivenName()));
        sb.append("\r\n");
        sb.append("<administrativeGenderCode code=\"").append(cda.getPatientSex()).append("\" codeSystem=\"2.16.840.1.113883.5.1\" />");
        sb.append("\r\n");
        sb.append(addTag("birthTime", getBirthTime(cda.getPatientBirthDate())));
        sb.append("\r\n");
        sb.append("<languageCommunication><languageCode code=\"").append(cda.getPatientLanguageCommunication()).append("\"/></languageCommunication>");
        sb.append("\r\n");
        sb.append("</patient>");
        sb.append("\r\n");
        sb.append("</patientRole>");
        sb.append("\r\n");
        sb.append("</recordTarget>");
        sb.append("\r\n");

        // author
        if (rolename.equals("doctor")) {
            sb.append(addDoctorAuthor(cda));
        }
        if (rolename.equals("pharmacist")) {
            sb.append(addPharmacistAuthor(cda));
        }

        // custodian
        sb.append(addCustodian(custodianOid, custodianName, edCountry));
        // Legal Authenticator
        sb.append(addLegalAuthenticator(legalOrgOid, legalauthenticatorfirstname, legalauthenticatorlastname,
                legalauthenticatorcity, legalauthenticatorpostalcode, edCountry));
        // Document of
        sb.append(addDocumentOf(cda.getConsentCode(), cda.getConsentDisplayName(), cda.getConsentStartDate(), cda.getConsentEndDate()));

        sb.append("<component>");
        sb.append("\r\n");
        sb.append("<structuredBody>");
        sb.append("\r\n");
        sb.append("<component>");
        sb.append("\r\n");
        sb.append("<section>");
        sb.append("\r\n");
        sb.append("<title>BPPC Description</title>");
        sb.append("\r\n");
        sb.append("<text>");
        sb.append("\r\n");
        sb.append("<br/>");
        sb.append("\r\n");
        sb.append("<content>Policy: ").append(cda.getConsentCode()).append(",Description: ").append(cda.getConsentDisplayName()).append(",EffectiveTime From: ").append(cda.getConsentStartDate()).append(", EffectiveTime To: ").append(cda.getConsentEndDate()).append("</content>");
        sb.append("\r\n");
        sb.append("</text>");
        sb.append("\r\n");
        sb.append("</section>");
        sb.append("\r\n");
        sb.append("</component>");
        sb.append("\r\n");
        sb.append("</structuredBody>");
        sb.append("\r\n");
        sb.append("</component>");
        sb.append("\r\n");
        sb.append("</ClinicalDocument>");
        sb.append("\r\n");
        return sb.toString();

    }

    private static String addDocumentOf(String consentCode, String consentDisplayName, String consentStartDate, String consentEndDate) {
        String uuid = EpsosHelperService.getUniqueId(); //java.util.UUID.randomUUID().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("<documentationOf typeCode=\"DOC\">" + "\r\n" + "<serviceEvent classCode=\"ACT\" moodCode=\"EVN\">" + "\r\n" + "<templateId root=\"1.3.6.1.4.1.19376.1.5.3.1.2.6\"/>" + "\r\n" + "<id root=\"").append(uuid).append("\"/>" + "\r\n" + "<code code=\"").append(consentCode).append("\" codeSystem=\"1.3.6.1.4.1.12559.11.10.1.3.2.4.1\" " + "codeSystemName=\"Connect-a-thon eventCodeList\" displayName=\"").append(consentDisplayName).append("\"/>" + "\r\n" + "<effectiveTime>" + "\r\n" + "<low value=\"").append(consentStartDate).append("\"/>" + "\r\n" + "<high value=\"").append(consentEndDate).append("\"/>" + "\r\n"
                + "</effectiveTime>" + "\r\n"
                + "</serviceEvent>" + "\r\n"
                + "</documentationOf>" + "\r\n");
        return sb.toString();
    }

    private static String addCustodian(String custodianOid, String custodianName, String country) {

        return ("<custodian typeCode=\"CST\">" + "\r\n" + "<assignedCustodian classCode=\"ASSIGNED\">" + "\r\n" +
                "<representedCustodianOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" + "\r\n" + "<id root=\"") + custodianOid + "\"/>" + "\r\n" + "<name>" + custodianName + "</name>" + "\r\n" + "<telecom nullFlavor=\"UNK\"/>" + "\r\n" + "<addr>" + "\r\n" + "<country>" + country + "</country>" + "\r\n"
                + "</addr>" + "\r\n"
                + "</representedCustodianOrganization>" + "\r\n"
                + "</assignedCustodian></custodian>" + "\r\n";
    }

    private static String addLegalAuthenticator(String legalOrgOid, String legalauthenticatorfirstname,
                                                String legalauthenticatorlastname, String legalauthenticatorcity,
                                                String legalauthenticatorpostalcode, String edCountry) {

        LOGGER.info("addLegalAuthenticator('{}', '{}', '{}', '{}', '{}', '{}')", legalOrgOid, legalauthenticatorfirstname,
                legalauthenticatorlastname, legalauthenticatorcity, legalauthenticatorpostalcode, edCountry);

        return ("<legalAuthenticator contextControlCode=\"OP\" typeCode=\"LA\">" + "\r\n" + "<time value=\"20120927112208\"/>"
                + "\r\n" + "<signatureCode code=\"S\"/>" + "\r\n" + "<assignedEntity classCode=\"ASSIGNED\">" + "\r\n"
                + "<id root=\"") +
                legalOrgOid + "\"/>" + "\r\n" + "<addr><streetAddressLine>4, Breydel Street" +
                "</streetAddressLine><city>Brussels</city><postalCode>B-1000</postalCode><state nullFlavor=\"UNK\"/><country>BE</country></addr><telecom nullFlavor=\"NI\"/>" + "\r\n"
                + "<assignedPerson>" + "\r\n" + "<name>" + "\r\n" + "<family>" +
                legalauthenticatorfirstname + "</family>"
                + "\r\n" + "<given>" +
                legalauthenticatorlastname + "</given>" + "\r\n" + "</name>" + "\r\n"
                + "</assignedPerson>" + "\r\n" + "<representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">"
                + "\r\n" + "<id root=\"" +
                legalOrgOid + "\"/>" + "\r\n" + "<name>Kansaneläkelaitos</name><telecom nullFlavor=\"UNK\"/>"
                + "\r\n" + "<addr use=\"PST\">" + "\r\n" + "<streetAddressLine>N/A</streetAddressLine>" + "\r\n" + "<city>" +
                legalauthenticatorcity + "</city>" + "\r\n" + "<postalCode>" + legalauthenticatorpostalcode +
                "</postalCode>" + "\r\n" + "<state nullFlavor=\"UNK\"/>" + "\r\n" + "<country>" + edCountry + "</country>" + "\r\n"
                + "</addr>" + "\r\n"
                + "</representedOrganization>" + "\r\n"
                + "</assignedEntity>" + "\r\n"
                + "</legalAuthenticator>" + "\r\n";
    }

    private static String CDAModelToEDXML(Document epDoc, CDAHeader cda, String eDuuid) {

        String edCountry = GetterUtil.getString(ConfigurationManagerFactory.getConfigurationManager().getProperty("ncp.country"), "");
        String pharmacistsOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_PHARMACIST_OID);
        String pharmaciesOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_PHARMACIES_OID);
        String custodianOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CUSTODIAN_OID);
        String custodianName = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CUSTODIAN_NAME);
        String legalOrgOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_ORG_OID);
        String legalauthenticatorfirstname = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_FIRSTNAME);
        String legalauthenticatorlastname = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_LASTNAME);
        String legalauthenticatorcity = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_CITY);
        String legalauthenticatorpostalcode = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_LEGAL_AUTHENTICATOR_POSTALCODE);
        String edOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DISPENSATION_OID);
        String entryOid = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_ENTRY_OID);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("\r\n");
        sb.append("<ClinicalDocument xmlns:epsos=\"urn:epsos-org:ep:medication\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:hl7-org:v3\" "
                + "xsi:schemaLocation=\"urn:hl7-org:v3\">");
        sb.append("\r\n");
        sb.append("<typeId root=\"2.16.840.1.113883.1.3\" extension=\"POCD_HD000040\"/>");
        sb.append("\r\n");
        sb.append("<templateId root=\"1.3.6.1.4.1.12559.11.10.1.3.1.1.2\"/>");
        sb.append("\r\n");
        sb.append("<templateId root=\"1.3.6.1.4.1.19376.1.5.3.1.1.1\"/>");
        sb.append("\r\n");
        //String uuid = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        sb.append("<id extension=\"").append(eDuuid).append("\" root=\"").append(edOid).append("\"/>");
        sb.append("\r\n");
        sb.append("<code codeSystemName=\"LOINC\" codeSystem=\"2.16.840.1.113883.6.1\" code=\"60593-1\" displayName=\"eDispensation\"/>");
        sb.append("\r\n");
        sb.append("<title>" + XML_DISPENSATION_TITLE + "</title>");
        sb.append("\r\n");
        sb.append("<effectiveTime value=\"").append(cda.getEffectiveTime()).append("\" />");
        sb.append("\r\n");
        sb.append("<confidentialityCode code=\"N\" codeSystem=\"2.16.840.1.113883.5.25\" codeSystemName=\"Confidentiality\" codeSystemVersion=\"913-20091020\" displayName=\"normal\"/>");
        sb.append("\r\n");
        sb.append("<languageCode code=\"").append(cda.getLanguageCode()).append("\"/>");
        sb.append("\r\n");
        sb.append(getRecordTargetFromEP(epDoc));
        sb.append("\r\n");
        // dispenser information
        sb.append("<author typeCode=\"AUT\">");
        sb.append("\r\n");
        sb.append("<functionCode code=\"" + "2262" + "\" displayName=\"Pharmacists\" codeSystem=\"2.16.840.1.113883.2.9.6.2.7\" codeSystemName=\"ISCO\"/>");
        sb.append("\r\n");
        sb.append("<time value=\"").append(cda.getEffectiveTime()).append("\"/>");
        sb.append("\r\n");
        sb.append("<assignedAuthor classCode=\"ASSIGNED\">");
        sb.append("\r\n");
        sb.append("<id extension=\"").append(cda.getPharmacistOrgId()).append("\" root=\"").append(pharmacistsOid).append("\"/>");
        sb.append("\r\n");
        sb.append(addAddress(cda.getPharmacistAddress(), cda.getPharmacistCity(), cda.getPharmacistPostalCode(),
                cda.getPharmacistCountry(), cda.getPharmacistTelephone(), cda.getPharmacistEmail(), false));
        sb.append("<assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\">");
        sb.append("\r\n");
        sb.append(addName(cda.getPharmacistFamilyName(), cda.getPharmacistPrefix(), cda.getPharmacistGivenName()));
        sb.append("\r\n");
        sb.append("</assignedPerson>");
        sb.append("\r\n");
        sb.append("<representedOrganization>");
        sb.append("\r\n");
        sb.append("<id root=\"").append(pharmaciesOid).append("\" extension=\"").append(cda.getPharmacistOrgId()).append("\"/>");
        sb.append("\r\n");
        sb.append("<name>").append(cda.getPharmacistOrgName()).append("</name>");
        sb.append("\r\n");
        sb.append(addAddress(cda.getPharmacistOrgAddress(), cda.getPharmacistOrgCity(), cda.getPharmacistOrgPostalCode(),
                cda.getPharmacistOrgCountry(), cda.getPharmacistOrgTelephone(), cda.getPharmacistOrgEmail(), true));
        sb.append("\r\n");
        sb.append("</representedOrganization>");
        sb.append("\r\n");
        sb.append("</assignedAuthor>");
        sb.append("\r\n");
        sb.append("</author>");
        sb.append("\r\n");
        //sb.append(getCustodianFromEP(epDoc));sb.append("\r\n");

        sb.append(addCustodian(custodianOid, custodianName, edCountry));
        sb.append(addLegalAuthenticator(legalOrgOid, legalauthenticatorfirstname, legalauthenticatorlastname,
                legalauthenticatorcity, legalauthenticatorpostalcode, edCountry));

        //sb.append(getLegalAuthFromEP(epDoc));sb.append("\r\n");
        String relRoot = getRelativePrescriptionRoot(epDoc);
        // Add relative prescription
        sb.append("<inFulfillmentOf>");
        sb.append("\r\n");
        sb.append("<order moodCode=\"RQO\">");
        sb.append("\r\n");
        sb.append(" <id extension=\"").append(cda.getPrescriptionBarcode()).append("\" root=\"").append(relRoot).append("\" />");
        sb.append("\r\n");
        sb.append("</order>");
        sb.append("\r\n");
        sb.append("</inFulfillmentOf>");
        sb.append("\r\n");

        // Add relative prescription
        sb.append("<relatedDocument typeCode=\"XFRM\">");
        sb.append("\r\n");
        sb.append("<parentDocument classCode=\"DOCCLIN\" >");
        sb.append("\r\n");
        sb.append(" <id extension=\"").append(cda.getPrescriptionBarcode()).append("\" root=\"").append(relRoot).append("\" />");
        sb.append("\r\n");
        sb.append("</parentDocument>");
        sb.append("\r\n");
        sb.append("</relatedDocument>");
        sb.append("\r\n");

        //prescription header
        sb.append("<component>");
        sb.append("\r\n");
        sb.append("<structuredBody>");
        sb.append("\r\n");
        sb.append("<component>");
        sb.append("\r\n");
        sb.append("<section>");
        sb.append("\r\n");
        // Required templateIds
        sb.append("<templateId root=\"2.16.840.1.113883.10.20.1.8\"/>");
        sb.append("\r\n");
        sb.append("<templateId root=\"1.3.6.1.4.1.12559.11.10.1.3.1.2.2\"/>");
        sb.append("\r\n");
        // Κωδικός εκτελεσμένης συνταγής
        sb.append(addIDRoot(edOid, cda.getDispensationId()));
        sb.append("\r\n");
        sb.append("<code code=\"60590-7\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" displayName=\"Medication dispensed\"/>");
        sb.append("\r\n");

        sb.append("<title>" + "Dispensation: ").append(cda.getDispensationId()).append("</title>");
        sb.append("\r\n");
        sb.append(getRelativePrescriptionText(epDoc));

        for (int i = 0; i < cda.getEDDetail().size(); i++) {
            EDDetail detail = (EDDetail) cda.getEDDetail().get(i);
            // prescription details
            sb.append("<entry typeCode=\"COMP\">");
            sb.append("\r\n");
            sb.append("<supply classCode=\"SPLY\" moodCode=\"EVN\">");
            sb.append("\r\n");

            sb.append("<templateId root=\"" + XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE1 + "\"/>");
            sb.append("\r\n");
            sb.append("<templateId root=\"" + XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE2 + "\"/>");
            sb.append("\r\n");
            sb.append("<templateId root=\"" + XML_DISPENSATION_ENTRY_SUPPLY_TEMPLATE3 + "\"/>");
            sb.append("\r\n");
            // Related prescription line
            sb.append(addIDRoot(entryOid, detail.getRelativePrescriptionLineId()));
            sb.append("\r\n");
            sb.append("<quantity value=\"").append(detail.getDispensedNumberOfPackages()).append("\" unit=\"1\" />");

            // Medicine
            sb.append("<product>");
            sb.append("\r\n");

            //Creating a clone reference to the ePrescription source
            org.dom4j.Document clone;
            try {
                //Cloning the ePrescription document
                String source = Utils.getDocumentAsXml(epDoc, true).replaceAll("xmlns=\"\"", "");
                clone = org.dom4j.DocumentHelper.parseText(source);

                //Getting the substituted fields, if any
                String id = detail.getRelativePrescriptionLineId();
                String product = detail.getMedicineCommercialName();
                String unit = detail.getDispensedQuantityUnit(); //Even in a substitution we getting null
                String quantity = detail.getDispensedQuantity();
                LOGGER.info("************************* EDDetails: {}", detail.toString());
                //Adding the relative product line updated with the substituted data, if any
                sb.append(getSubstitutedRelativeProductLineFromEP(clone, id, product, unit, quantity));

                sb.append("\r\n");
            } catch (Exception exc) {
                LOGGER.error("Error cloning ePrescription document " + exc.getMessage());
                LOGGER.error(ExceptionUtils.getStackTrace(exc));
            }
            sb.append("</product>");
            sb.append("\r\n");

            // Pharmacist info
            sb.append("<performer typeCode=\"PRF\">");
            sb.append("\r\n");
            sb.append("<time value=\"").append(cda.getEffectiveTime()).append("\" />");
            sb.append("\r\n");
            sb.append("<assignedEntity>");
            sb.append("\r\n");
            sb.append("<id root=\"").append(pharmacistsOid).append("\" extension=\"").append(cda.getPharmacistOrgId()).append("\"/>");
            sb.append("\r\n");
            sb.append(addAddress(cda.getPharmacistAddress(), cda.getPharmacistCity(), cda.getPharmacistPostalCode(),
                    cda.getPharmacistCountry(), cda.getPharmacistTelephone(), cda.getPharmacistEmail(), false));
            sb.append("\r\n");
            sb.append("<assignedPerson>");
            sb.append("\r\n");
            sb.append(addName(cda.getPharmacistFamilyName(), cda.getPharmacistPrefix(), cda.getPharmacistGivenName()));
            sb.append("\r\n");
            sb.append("</assignedPerson>");
            sb.append("\r\n");
            sb.append("<representedOrganization>");
            sb.append("\r\n");
            sb.append("<id root=\"").append(pharmaciesOid).append("\" extension=\"").append(cda.getPharmacistOrgId()).append("\"/>");
            sb.append("\r\n");
            sb.append("<name>").append(cda.getPharmacistOrgName()).append("</name>");
            sb.append("\r\n");
            sb.append(addAddress(cda.getPharmacistOrgAddress(), cda.getPharmacistOrgCity(), cda.getPharmacistOrgPostalCode(),
                    cda.getPharmacistOrgCountry(), cda.getPharmacistOrgTelephone(), cda.getPharmacistOrgEmail(), true));
            sb.append("</representedOrganization>");
            sb.append("\r\n");
            sb.append("</assignedEntity>");
            sb.append("\r\n");
            sb.append("</performer>");
            sb.append("\r\n");

            // Add participant
            sb.append("<participant typeCode=\"PRF\">");
            sb.append("\r\n");
            sb.append("<participantRole classCode=\"LIC\" >");
            sb.append("\r\n");
            sb.append(addIDRoot(entryOid, ""));
            sb.append("\r\n");
            sb.append("<scopingEntity classCode=\"ORG\" >");
            sb.append("\r\n");
            sb.append(addIDRoot(entryOid, ""));
            sb.append("\r\n");
            sb.append("</scopingEntity>");
            sb.append("\r\n");
            sb.append("</participantRole>");
            sb.append("\r\n");
            sb.append("</participant>");
            sb.append("\r\n");

            // get relative prescription line
            sb.append("<entryRelationship typeCode=\"REFR\">");
            sb.append(getRelativePrescriptionLineFromEP(epDoc, detail.getRelativePrescriptionLineId()));
            sb.append("\r\n");
            sb.append("</entryRelationship>");

            //TBD
            //Checking if performed a substitution
            if (detail.isSubstituted()) {
                //Opening a substitution relationship entry
                sb.append("<entryRelationship")
                        .append(" ")
                        .append("typeCode=\"COMP\"")
                        .append(">\r\n");

                //Adding an act entry
                sb.append("<act")
                        .append(" ")
                        .append("classCode=\"ACT\"")
                        .append(" ")
                        .append("moodCode=\"EVN\"")
                        .append(">\r\n");

                //Adding the code element
                sb.append("<code")
                        .append(" ")
                        .append("code=\"SUBST\"")
                        .append(" ")
                        .append("codeSystem=\"2.16.840.1.113883.5.6\"")
                        .append(" ")
                        .append("codeSystemName=\"ActClass\"")
                        .append(" ")
                        .append("displayName=\"Substitution\"")
                        .append("/>\r\n");

                //Closing the act entry
                sb.append("</act>\r\n");

                //Closing the substitution relationship entry
                sb.append("</entryRelationship>\r\n");
            }
            //TBD

            sb.append("</supply>");
            sb.append("\r\n");
            sb.append("</entry>");
            sb.append("\r\n");
        }

        sb.append("</section>");
        sb.append("\r\n");
        sb.append("</component>");
        sb.append("\r\n");
        sb.append("</structuredBody>");
        sb.append("\r\n");
        sb.append("</component>");
        sb.append("\r\n");
        sb.append("</ClinicalDocument>");
        sb.append("\r\n");

        return sb.toString();
    }

    private static String addDoctorAuthor(CDAHeader cda) {

        StringBuilder sb = new StringBuilder();
        sb.append("<author typeCode=\"AUT\">");
        sb.append("\r\n");
        sb.append("<functionCode code=\"" + "221" + "\" displayName=\"Medical doctors\" codeSystem=\"2.16.840.1.113883.2.9.6.2.7\" codeSystemName=\"epSOSHealthcareProfessionalRoles\"/>");
        sb.append("\r\n");
        sb.append("<time value=\"").append(cda.getEffectiveTime()).append("\"/>");
        sb.append("\r\n");
        sb.append("<assignedAuthor classCode=\"ASSIGNED\">");
        sb.append("\r\n");
        if (Validator.isNotNull(cda.getDoctorOid())) {
            sb.append("<id extension=\"").append(cda.getDoctorOrgId()).append("\" root=\"").append(cda.getDoctorOid()).append("\"/>");
            sb.append("\r\n");
        }
        sb.append(addAddress(cda.getDoctorAddress(), cda.getDoctorCity(), cda.getDoctorPostalCode(),
                cda.getDoctorCountry(), cda.getDoctorTelephone(), cda.getDoctorEmail(), false));
        sb.append("<assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\">");
        sb.append("\r\n");
        sb.append(addName(cda.getDoctorFamilyName(), cda.getDoctorPrefix(), cda.getDoctorGivenName()));
        sb.append("\r\n");
        sb.append("</assignedPerson>");
        sb.append("\r\n");

        if (cda.getDoctorUnit() != null) {
            sb.append("<representedOrganization>");
            sb.append("\r\n");
            sb.append("<id nullFlavor=\"NA\" />");
            sb.append("\r\n");
            sb.append("<name>").append(cda.getDoctorUnit()).append("</name>");
            sb.append("\r\n");
            sb.append(addAddress("", "", "", cda.getDoctorCountry(), "", "", false));
            sb.append("</representedOrganization>");
            sb.append("\r\n");
        }
        sb.append("</assignedAuthor>");
        sb.append("\r\n");
        sb.append("</author>");
        sb.append("\r\n");

        return sb.toString();
    }

    private static String addPharmacistAuthor(CDAHeader cda) {

        StringBuilder sb = new StringBuilder();
        // dispenser information
        sb.append("<author typeCode=\"AUT\">");
        sb.append("\r\n");
        sb.append("<functionCode code=\"" + "2262" + "\" displayName=\"Pharmacists\" codeSystem=\"2.16.840.1.113883.2.9.6.2.7\" codeSystemName=\"epSOSHealthcareProfessionalRoles\"/>");
        sb.append("\r\n");
        sb.append("<time value=\"").append(cda.getEffectiveTime()).append("\"/>");
        sb.append("\r\n");
        sb.append("<assignedAuthor classCode=\"ASSIGNED\">");
        sb.append("\r\n");
        if (Validator.isNotNull(cda.getPharmacistOid())) {
            sb.append("<id extension=\"").append(cda.getPharmacistOrgId()).append("\" root=\"").append(cda.getPharmacistOid()).append("\"/>");
            sb.append("\r\n");
        }
        sb.append(addAddress(cda.getPharmacistAddress(), cda.getPharmacistCity(), cda.getPharmacistPostalCode(),
                cda.getPharmacistCountry(), cda.getPharmacistTelephone(), cda.getPharmacistEmail(), false));
        sb.append("<assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\">");
        sb.append("\r\n");
        sb.append(addName(cda.getPharmacistFamilyName(), cda.getPharmacistPrefix(), cda.getPharmacistGivenName()));
        sb.append("\r\n");
        sb.append("</assignedPerson>");
        sb.append("\r\n");
        sb.append("</assignedAuthor>");
        sb.append("\r\n");
        sb.append("</author>");
        sb.append("\r\n");
        return sb.toString();
    }

    private static String addEntryRelationship(String code1, String value1) {

        StringBuilder sb = new StringBuilder();
        sb.append("<entryRelationship inversionInd=\"true\" typeCode=\"RSON\">");
        sb.append("\r\n");
        sb.append("<act classCode=\"ACT\" moodCode=\"EVN\">");
        sb.append("\r\n");
        sb.append("<templateId root=\"1.3.6.1.4.1.19376.1.5.3.1.4.4.1\" />");
        sb.append("\r\n");
        sb.append("<templateId root=\"2.16.840.1.113883.10.20.1.27\" />");
        sb.append("\r\n");
        if (value1 == null) {
            value1 = "";
        }
        if (value1.equals("")) {
            sb.append("<id nullFlavor=\"NI\" root=\"").append(code1).append("\" />");
            sb.append("\r\n");
        } else {
            sb.append("<id extension=\"").append(value1).append("\" root=\"").append(code1).append("\" />");
            sb.append("\r\n");
        }
        sb.append("<code></code>");
        sb.append("\r\n");
        sb.append("</act>");
        sb.append("\r\n");
        sb.append("</entryRelationship>");
        sb.append("\r\n");
        return sb.toString();

    }

    private static String addQuantity(String lowValue, String lowUnit, String highValue, String highUnit) {

        return "<epsos:quantity>" +
                "\r\n" +
                "<epsos:numerator xsi:type=\"epsos:PQ\" value=\"" + lowValue + "\" unit=\"" + lowUnit + "\"/>" +
                "\r\n" +
                "<epsos:denominator xsi:type=\"epsos:PQ\" value=\"" + lowValue + "\" unit=\"" + lowUnit + "\"/>" +
                "\r\n" +
                "</epsos:quantity>" +
                "\r\n";
    }

    private static String addDoseQuantity(String lowValue, String lowUnit, String highValue, String highUnit) {

        return "<doseQuantity>" +
                "\r\n" +
                "<low value=\"" + lowValue + "\" unit=\"" + lowUnit + "\"/>" +
                "\r\n" +
                "<high value=\"" + highValue + "\" unit=\"" + highUnit + "\"/>" +
                "\r\n" +
                "</doseQuantity>" +
                "\r\n";
    }

    /*
     * <effectiveTime xsi:type='PIVL_TS' institutionSpecified='false' operator='A'> <period value='8' unit='h' /></effectiveTime>
     */
    private static String addDoseFrequency(String freq) {

        String eff = "";
        if (freq.equals("5")) // Μια φορά την εβδομάδα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\">"
                    + "<period value=\"1\" unit=\"wk\" /></effectiveTime>";
        }
        if (freq.equals("1")) // Μια φορά την ημέρα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"1\" unit=\"d\" /></effectiveTime>";
        }
        if (freq.equals("6")) // Δύο φορές την εβδομάδα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"4\" unit=\"d\" /></effectiveTime>";
        }
        if (freq.equals("2")) // Δύο φορές την ημέρα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"12\" unit=\"h\" /></effectiveTime>";
        }
        if (freq.equals("7")) // Τρεις φορές την εβδομάδα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"3\" unit=\"d\" /></effectiveTime>";
        }
        if (freq.equals("3")) // Τρεις φορές την ημέρα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"8\" unit=\"h\" /></effectiveTime>";
        }
        if (freq.equals("4")) // Τέσσερις φορές την ημέρα
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"6\" unit=\"h\" /></effectiveTime>";
        }
        if (freq.equals("10")) // Κάθε 2 εβδομάδες
        {
            eff = "<effectiveTime xsi:type=\"PIVL_TS\" institutionSpecified=\"false\" operator=\"A\">"
                    + "<period value=\"2\" unit=\"wk\" /></effectiveTime>";
        }
        if (freq.equals("0")) // Ανευ
        {
            eff = "<effectiveTime nullFlavor=\"NI\" />";
        }
        if (freq.equals("9")) // Επί πόνου
        {
            eff = "<effectiveTime xsi:type=\"EIVL_TS\" operator=\"A\">"
                    + "<event code=\"PAIN\" />"
                    + "</effectiveTime>";
        }
        if (freq.equals("8")) // Εφάπαξ
        {
            eff = "<effectiveTime xsi:type=\"EIVL_TS\" operator=\"A\">"
                    + "<event code=\"ONCE\" />"
                    + "</effectiveTime>";
        }
        return eff;

    }

    private static String addEffectiveTime(String low, String high) {

        return "<effectiveTime xsi:type=\"IVL_TS\">" +
                "\r\n" +
                "<low value=\"" + low + "\" />" +
                "\r\n" +
                "<high value=\"" + high + "\" />" +
                "\r\n" +
                "</effectiveTime>";
    }

    private static String addTextTag(String tagName, String value) {

        if (Validator.isNull(value)) {
            value = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tagName);
        String ext = "";
        if (value.equals("")) {
            ext = " nullFlavor=\"NI\">";
        } else {
            ext = ">" + value;
        }
        sb.append(ext);
        sb.append("</").append(tagName).append(">");
        return sb.toString();
    }

    private static String addTag(String tagName, String value) {

        if (Validator.isNull(value)) {
            value = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tagName);
        String ext;
        if (value.equals("")) {
            ext = " nullFlavor=\"NI\"";
        } else {
            ext = " value=\"" + value + "\"";
        }
        sb.append(ext);
        sb.append(" />");
        return sb.toString();
    }

    private static String addName(String family, String prefix, String given) {

        return "<name>" +
                "\r\n" +
                addTextTag("family", family) +
                "\r\n" +
                addTextTag("given", given) +
                "\r\n" +
                "</name>";
    }

    private static String addAddress(String streetAddress, String city, String postalCode, String country, String tel,
                                     String email, boolean addressPointInStart) {

        LOGGER.info("method addAddress('{}', '{}', '{}', '{}', '{}', '{}', '{}', )", streetAddress, city, postalCode,
                country, tel, email, addressPointInStart);

        StringBuilder sb = new StringBuilder();
        if (addressPointInStart) {
            if (Validator.isNull(email) && Validator.isNull(tel)) {
                sb.append("<telecom nullFlavor=\"UNK\" />");
            }
            if (Validator.isNotNull(tel)) {
                sb.append("<telecom use=\"WP\" value=\"tel:+").append(tel).append("\" />");
            }
            if (Validator.isNotNull(email)) {
                sb.append("<telecom use=\"WP\" value=\"mailto:").append(email).append("\"/>");
            }
        }
        sb.append("<addr>");
        sb.append("\r\n");
        sb.append(addTextTag("streetAddressLine", streetAddress));
        sb.append("\r\n");
        sb.append(addTextTag("city", city));
        sb.append("\r\n");
        sb.append(addTextTag("state", ""));
        sb.append("\r\n");
        sb.append(addTextTag("postalCode", postalCode));
        sb.append("\r\n");
        sb.append(addTextTag("country", country));
        sb.append("\r\n");
        sb.append("</addr>");
        sb.append("\r\n");
        if (!addressPointInStart) {
            if (Validator.isNull(email) && Validator.isNull(tel)) {
                sb.append("<telecom nullFlavor=\"NI\" />");
            }
            if (Validator.isNotNull(tel)) {
                sb.append("<telecom use=\"WP\" value=\"tel:+").append(tel).append("\" />");
            }
            if (Validator.isNotNull(email)) {
                sb.append("<telecom use=\"WP\" value=\"mailto:").append(email).append("\"/>");
            }
        }
        return sb.toString();
    }

    private static String addIDRoot(String id, String extension) {

        String ext;
        String ret;
        if (Validator.isNull(extension)) {
            ext = "nullFlavor=\"NI\"";
            ret = "<id " + ext + " />";
        } else {
            ext = "extension=\"" + extension + "\"";
            ret = "<id root=\"" + id + "\" " + ext + " />";
        }
        return ret;
    }

    private static String addTemplateIDRoot(String id, String extension) {

        String ext;
        String ret;
        if (Validator.isNull(extension)) {
            ext = "nullFlavor=\"NI\"";
            ret = "<id " + ext + " />";
        } else {
            ext = "extension=\"" + extension + "\"";
            ret = "<templateId root=\"" + id + "\" " + ext + " />";
        }
        return ret;
    }

    private static String getTagValue(String sTag, Element eElement) {

        String ret = "";
        try {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
            Node nValue = nlList.item(0);
            ret = nValue.getNodeValue();
        } catch (Exception e) {
            LOGGER.error("Error getting value '{}'", e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return ret;
    }

    public static boolean checkPrescriptionBarcode(String dispensexml, String barcode) {

        boolean sameBarcode = false;
        try {
            Document doc = CDAUtils.readEpXML(dispensexml);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(new CDANameSpaceContext());

            XPathExpression execExpr = xpath.compile("//xsi:inFulfillmentOf/xsi:order/xsi:id[@root='2.16.724.4.8.10.200.20']/@extension");
            NodeList nl = (NodeList) execExpr.evaluate(doc, XPathConstants.NODESET);

            String refBarcode = "";
            if (nl.item(0) != null) {
                refBarcode = nl.item(0).getNodeValue();
            }
            sameBarcode = barcode.equals(refBarcode);
            LOGGER.debug("The reference prescription barcode is: '{}' and the requested is: '{}'. Comparison: '{}'", refBarcode, barcode, sameBarcode);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return sameBarcode;
    }

    private static String getBirthTime(String cdaBirthDate) {

        final DateFormat portalUserDateFormat = LiferayUtils.getPortalUserDateFormat();
        Date patientBirthDate = null;
        try {
            patientBirthDate = portalUserDateFormat.parse(cdaBirthDate);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(patientBirthDate);
    }
}
