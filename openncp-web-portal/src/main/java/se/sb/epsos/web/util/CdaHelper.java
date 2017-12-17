/*    Copyright 2011-2013 Apotekens Service AB <epsos@apotekensservice.se>
 *
 *    This file is part of epSOS-WEB.
 *
 *    epSOS-WEB is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *    epSOS-WEB is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License along with epSOS-WEB. If not, see http://www.gnu.org/licenses/.
 **/
package se.sb.epsos.web.util;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.sb.epsos.web.model.Ingredient;
import se.sb.epsos.web.model.Prescription;
import se.sb.epsos.web.model.PrescriptionRow;
import se.sb.epsos.web.model.QuantityVO;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CdaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdaHelper.class);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(new Locale("sv"));
    private static final String CHAR_FORWARD_SLASH = "/";
    private static final String CHAR_SPACE = " ";
    private static final String CHAR_ONE = "1";

    public void parsePrescriptionFromDocument(Prescription prescription) {

        ArrayList<PrescriptionRow> rows = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(prescription.getBytes()));

            XPath xpath = XPathFactory.newInstance().newXPath();

            xpath.setNamespaceContext(new CdaNamespaceContext());

            // for each prescription component, search for its entries and make up the list
            XPathExpression prescriptionIDExpr = xpath.compile(
                    "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[hl7:templateId/@root='1.3.6.1.4.1.12559.11.10.1.3.1.2.1']");

            NodeList prescriptionIDNodes = (NodeList) prescriptionIDExpr.evaluate(dom, XPathConstants.NODESET);
            if (prescriptionIDNodes != null && prescriptionIDNodes.getLength() > 0) {
                XPathExpression idExpr = xpath.compile("hl7:id");

                for (int p = 0; p < prescriptionIDNodes.getLength(); p++) {
                    String prescriptionID = "";
                    String prescriptionIDRoot = "";
                    Node sectionNode = prescriptionIDNodes.item(p);
                    Node pIDNode = (Node) idExpr.evaluate(sectionNode, XPathConstants.NODE);
                    if (pIDNode != null) {
                        if (pIDNode.getAttributes().getNamedItem("extension") != null) {
                            prescriptionID = pIDNode.getAttributes().getNamedItem("extension").getNodeValue();
                        } else {
                            prescriptionID = pIDNode.getAttributes().getNamedItem("root").getNodeValue();
                        }
                        prescriptionIDRoot = pIDNode.getAttributes().getNamedItem("root").getNodeValue();
                    }

                    XPathExpression prefixExpr = xpath.compile("hl7:author/hl7:assignedAuthor/hl7:assignedPerson/hl7:name/hl7:prefix");
                    XPathExpression givenNameExpr = xpath.compile("hl7:author/hl7:assignedAuthor/hl7:assignedPerson/hl7:name/hl7:given");
                    XPathExpression familyNameExpr = xpath.compile("hl7:author/hl7:assignedAuthor/hl7:assignedPerson/hl7:name/hl7:family");
                    String prescriber = handleAssignedPerson(sectionNode, prefixExpr, givenNameExpr, familyNameExpr);

                    XPathExpression performerPrefixExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:assignedPerson/hl7:name/hl7:prefix");
                    XPathExpression performerGivenNameExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:assignedPerson/hl7:name/hl7:given");
                    XPathExpression performerSurnameExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:assignedPerson/hl7:name/hl7:family");
                    String performer = handleAssignedPerson(dom, performerPrefixExpr, performerGivenNameExpr, performerSurnameExpr);
                    if (prescriber == null || prescriber.isEmpty()) {
                        prescriber = performer;
                    }

                    // prescription header information
                    prescription.setPerformer(performer);

                    List<String> list = handlePrescriberTelecom(dom, xpath);

                    if (list != null && !list.isEmpty()) {
                        if (list.size() == 2) {
                            prescription.setContact2(list.get(1));
                        }
                        prescription.setContact1(list.get(0));
                    }

                    prescription.setCreateDate(handlePrescriptionDate(dom, xpath));
                    prescription.setProfession(handleProfession(dom, xpath));
                    prescription.setFacility(handleFacility(dom, xpath));
                    prescription.setAddress(handleAdress(dom, xpath));
                    prescription.setCountry(handleCountryPrescriber(dom, xpath));

                    // PRESCRIPTION ITEMS
                    XPathExpression entryExpr = xpath.compile("hl7:entry");
                    NodeList entryList = (NodeList) entryExpr.evaluate(sectionNode, XPathConstants.NODESET);
                    if (entryList != null && entryList.getLength() > 0) {
                        for (int i = 0; i < entryList.getLength(); i++) {
                            PrescriptionRow row = new PrescriptionRow();

                            Node entryNode = entryList.item(i);

                            String materialID = "";

                            XPathExpression productIdExpr = xpath.compile(
                                    "hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/hl7:code");
                            Node materialIDNode = (Node) productIdExpr.evaluate(entryNode, XPathConstants.NODE);
                            if ((materialIDNode != null) && (materialIDNode.getAttributes() != null)) {
                                if (materialIDNode.getAttributes().getNamedItem("nullFlavor") != null) {
                                    materialID = NullFlavorManager.getNullFlavor(materialIDNode.getAttributes().getNamedItem("nullFlavor").getNodeValue());
                                } else if (materialIDNode.getAttributes().getNamedItem("code") != null) {
                                    materialID = materialIDNode.getAttributes().getNamedItem("code").getNodeValue();
                                }
                            }

                            XPathExpression nameExpr = xpath.compile(
                                    "hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/hl7:name");
                            Node materialName = (Node) nameExpr.evaluate(entryNode, XPathConstants.NODE);
                            String name = materialName.getTextContent().trim();

                            String formCode = null;
                            String packsString = "";
                            XPathExpression doseFormExpr = xpath.compile(
                                    "hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:formCode");
                            Node doseForm = (Node) doseFormExpr.evaluate(entryNode, XPathConstants.NODE);
                            if (doseForm != null) {
                                Node displayNameNode = doseForm.getAttributes().getNamedItem("displayName");
                                if (displayNameNode != null) {
                                    packsString = displayNameNode.getNodeValue();
                                }
                                Node codeNode = doseForm.getAttributes().getNamedItem("code");
                                if (codeNode != null) {
                                    formCode = codeNode.getNodeValue();
                                }
                            }

                            String atcCode = null;
                            XPathExpression atcCodeExpr = xpath.compile("hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:asSpecializedKind[@classCode='GEN']/epsos:generalizedMedicineClass[@classCode='MMAT']/epsos:code");
                            Node atcCodeNode = (Node) atcCodeExpr.evaluate(entryNode, XPathConstants.NODE);
                            if (atcCodeNode != null) {
                                atcCode = atcCodeNode.getAttributes().getNamedItem("code").getNodeValue();
                            }

                            String atcName = null;
                            XPathExpression atcNameExpr = xpath.compile("hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:asSpecializedKind[@classCode='GEN']/epsos:generalizedMedicineClass[@classCode='MMAT']/epsos:name");
                            Node atcNameNode = (Node) atcNameExpr.evaluate(entryNode, XPathConstants.NODE);
                            if (atcNameNode != null) {
                                atcName = atcNameNode.getTextContent().trim();
                            }

                            String strength = null;
                            XPathExpression strengthExpr = xpath.compile("hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:desc");
                            Node strengthNode = (Node) strengthExpr.evaluate(entryNode, XPathConstants.NODE);
                            if (strengthNode != null) {
                                strength = strengthNode.getTextContent().trim();
                            }

                            row.setIngredient(handleIngredients(xpath, entryNode));

                            String doseString = "";
                            XPathExpression doseExpr = xpath.compile("hl7:substanceAdministration/hl7:doseQuantity");
                            Node dose = (Node) doseExpr.evaluate(entryNode, XPathConstants.NODE);
                            if (dose != null) {
                                if (dose.getAttributes().getNamedItem("value") != null) {
                                    doseString = dose.getAttributes().getNamedItem("value").getNodeValue();
                                    if (dose.getAttributes().getNamedItem("unit") != null) {
                                        String unit = dose.getAttributes().getNamedItem("unit").getNodeValue();
                                        if (unit != null && !unit.equals(CHAR_ONE)) {
                                            doseString += CHAR_SPACE + unit;
                                        }
                                    }
                                } else {

                                    StringBuilder lowString = new StringBuilder();
                                    StringBuilder highString = new StringBuilder();
                                    XPathExpression doseExprLow = xpath.compile("hl7:low");
                                    Node lowDoseNode = (Node) doseExprLow.evaluate(dose, XPathConstants.NODE);
                                    if (lowDoseNode != null && lowDoseNode.getAttributes().getNamedItem("value") != null) {
                                        lowString.append(lowDoseNode.getAttributes().getNamedItem("value").getNodeValue().trim());
                                        if (lowDoseNode.getAttributes().getNamedItem("unit") != null) {
                                            String unit = lowDoseNode.getAttributes().getNamedItem("unit").getNodeValue();
                                            if (unit != null && !unit.equals(CHAR_ONE)) {
                                                lowString.append(CHAR_SPACE).append(unit);
                                            }
                                        }
                                    }
                                    XPathExpression doseExprHigh = xpath.compile("hl7:high");
                                    Node highDoseNode = (Node) doseExprHigh.evaluate(dose, XPathConstants.NODE);
                                    if (highDoseNode != null && highDoseNode.getAttributes().getNamedItem("value") != null) {
                                        highString.append(highDoseNode.getAttributes().getNamedItem("value").getNodeValue().trim());
                                        if (highDoseNode.getAttributes().getNamedItem("unit") != null) {
                                            String unit = highDoseNode.getAttributes().getNamedItem("unit").getNodeValue();
                                            if (unit != null && !unit.equals(CHAR_ONE)) {
                                                highString.append(CHAR_SPACE).append(unit);
                                            }
                                        }
                                    }

                                    doseString = !Validator.isNull(lowString.toString()) ? lowString.toString() : "";
                                    if (!Validator.isNull(highString.toString()) && !lowString.equals(highString)) {
                                        doseString = !Validator.isNull(doseString) ? doseString + " - " + highString : highString.toString();
                                    }

                                    if (Validator.isNull(doseString)) {
                                        doseString = dose.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                                        if (!Validator.isNull(doseString)) {
                                            doseString = NullFlavorManager.getNullFlavor(doseString);
                                        }
                                    }
                                }
                            }

                            XPathExpression substituteInstrExpr = xpath.compile(
                                    "hl7:substanceAdministration/hl7:entryRelationship[@typeCode='SUBJ'][@inversionInd='true']/hl7:observation[@classCode='OBS']/hl7:value");
                            Node substituteNode = (Node) substituteInstrExpr.evaluate(entryNode, XPathConstants.NODE);
                            SubstitutionPermitted sp = handleSubstitution(substituteNode);

                            row.setProductName(name);
                            row.setProductId(materialID);

                            row.setFormName(packsString);
                            row.setFormCode(formCode);

                            row.setAtcCode(atcCode);
                            row.setAtcName(atcName);
                            row.setStrength(strength);

                            row.setDosage(doseString);

                            XPathExpression packageSize = xpath.compile(
                                    "hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:asContent/epsos:containerPackagedMedicine/epsos:capacityQuantity");
                            row.setPackageSize(handleQuantity(packageSize, entryNode));

                            XPathExpression nrOfPacksExpr = xpath.compile("hl7:substanceAdministration/hl7:entryRelationship/hl7:supply/hl7:quantity");
                            row.setNbrPackages(handleQuantity(nrOfPacksExpr, entryNode));
                            row.setTypeOfPackage(handleTypeOfPackage(xpath, entryNode));

                            row.setFrequency(handleEffectiveTimeTypePivl_Ts(xpath, entryNode));
                            row.setRoute(handleRoute(xpath, entryNode));

                            handleEffectiveTime_IVL_TS(xpath, row, entryNode);

                            handleInstructions(prescription, row, xpath, entryNode);

                            row.setPrescriber(prescriber);

                            // entry header information
                            row.setPrescriptionId(prescriptionID);
                            row.setPrescriptionIdRoot(prescriptionIDRoot);
                            row.setMaterialId(materialID);
                            row.setSubstitutionPermittedText(sp.getSubstitutionPermittedText());
                            row.setSubstitutionPermitted(sp.isSubstitutionPermitted());

                            rows.add(row);
                        }
                    }
                    prescription.setRows(rows);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Exception: '{}'", e.getMessage(), e);
        }
    }

    private void handleInstructions(Prescription prescription, PrescriptionRow row, XPath xpath, Node entryNode)
            throws UnsupportedEncodingException, XPathExpressionException, TransformerFactoryConfigurationError,
            TransformerException {

        String patientString = "";
        XPathExpression patientInstrEexpr = xpath.compile(
                "hl7:substanceAdministration/hl7:entryRelationship/hl7:act/hl7:code[@code='PINSTRUCT']/../hl7:text/hl7:reference[@value]");
        Node patientInfo = (Node) patientInstrEexpr.evaluate(entryNode, XPathConstants.NODE);
        if (patientInfo != null) {
            patientString = patientInfo.getAttributes().getNamedItem("value").getNodeValue();
        }

        if (patientString.startsWith("#")) {
            patientString = handleReferenceXPath(xpath, entryNode, patientString);
            row.setPatientInstructions(patientString);
        } else {
            row.setPatientInstructions(patientString);
        }

        String fillerString = "";
        XPathExpression fillerInstrEexpr = xpath.compile(
                "hl7:substanceAdministration/hl7:entryRelationship/hl7:act/hl7:code[@code='FINSTRUCT']/../hl7:text/hl7:reference[@value]");
        Node fillerInfo = (Node) fillerInstrEexpr.evaluate(entryNode, XPathConstants.NODE);
        if (fillerInfo != null) {
            fillerString = fillerInfo.getAttributes().getNamedItem("value").getNodeValue();
        }

        if (fillerString.startsWith("#")) {
            fillerString = handleReferenceXPath(xpath, entryNode, fillerString);
            row.setPharmacistInstructions(fillerString);
        } else {
            row.setPharmacistInstructions(fillerString);
        }
    }

    private void handleEffectiveTime_IVL_TS(XPath xpath, PrescriptionRow row, Node entryNode) throws XPathExpressionException {

        XPathExpression lowExpr = xpath
                .compile("hl7:substanceAdministration/hl7:effectiveTime[@xsi:type='IVL_TS']/hl7:low");
        Node nodeLow = (Node) lowExpr.evaluate(entryNode, XPathConstants.NODE);
        Date dateLow = getDate(nodeLow);
        if (dateLow != null) {
            row.setStartDate(DateUtil.formatDate(dateLow, "yyyy-MM-dd"));
        } else {
            if (nodeLow != null) {
                Node nodeLowNullFlavor = nodeLow.getAttributes().getNamedItem("nullFlavor");
                if (nodeLowNullFlavor != null) {
                    String nodeLowString = nodeLowNullFlavor.getNodeValue();
                    if (!Validator.isNull(nodeLowString)) {
                        nodeLowString = NullFlavorManager.getNullFlavor(nodeLowString);
                    }
                    row.setStartDate(nodeLowString);
                }
            }
        }

        XPathExpression highExpr = xpath.compile("hl7:substanceAdministration/hl7:effectiveTime[@xsi:type='IVL_TS']/hl7:high");
        Node nodeHigh = (Node) highExpr.evaluate(entryNode, XPathConstants.NODE);
        Date dateHigh = getDate(nodeHigh);
        if (dateHigh != null) {
            row.setEndDate(DateUtil.formatDate(dateHigh, "yyyy-MM-dd"));
        } else {
            if (nodeHigh != null) {
                Node nodeHighNullFlavor = nodeHigh.getAttributes().getNamedItem("nullFlavor");
                if (nodeHighNullFlavor != null) {
                    String nodeHighString = nodeHighNullFlavor.getNodeValue();
                    if (!Validator.isNull(nodeHighString)) {
                        nodeHighString = NullFlavorManager.getNullFlavor(nodeHighString);
                    }
                    row.setEndDate(nodeHighString);
                }
            }
        }
    }

    private List<Ingredient> handleIngredients(XPath xpath, Node entryNode) throws XPathExpressionException {

        List<Ingredient> ingredientList = new ArrayList<>();
        XPathExpression ingredientRowExpr = xpath.compile(
                "hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:ingredient[@classCode='ACTI']");
        NodeList ingredientRowNodeList = (NodeList) ingredientRowExpr.evaluate(entryNode, XPathConstants.NODESET);
        if (ingredientRowNodeList != null && ingredientRowNodeList.getLength() > 0) {
            LOGGER.info("IngredientRow Length: '{}'", ingredientRowNodeList.getLength());
            for (int a = 0; a < ingredientRowNodeList.getLength(); a++) {
                Ingredient ingredientVO = new Ingredient();

                Node ingredientNode = ingredientRowNodeList.item(a);

                handleIngredient(ingredientVO, xpath, ingredientNode);
                ingredientVO.setStrength(handleStrength(xpath, ingredientNode));
                ingredientList.add(ingredientVO);
            }
        }
        return ingredientList;
    }

    private String handleStrength(XPath xpath, Node ingredientNode) throws XPathExpressionException {

        String strength = "";
        XPathExpression quantityExpr = xpath.compile("epsos:quantity");
        Node quantityNode = (Node) quantityExpr.evaluate(ingredientNode, XPathConstants.NODE);
        if (quantityNode != null && !quantityNode.hasAttributes()) {
            XPathExpression numeratorExpression = xpath.compile("epsos:numerator");
            XPathExpression denumeratorExpression = xpath.compile("epsos:denominator");
            Node numeratorNode = (Node) numeratorExpression.evaluate(quantityNode, XPathConstants.NODE);
            Node denumeratorNode = (Node) denumeratorExpression.evaluate(quantityNode, XPathConstants.NODE);
            if (numeratorNode != null && denumeratorNode != null && numeratorNode.getAttributes().getNamedItem("nullFlavor") == null) {
                String value = numeratorNode.getAttributes().getNamedItem("value").getNodeValue();
                String unit = numeratorNode.getAttributes().getNamedItem("unit").getNodeValue();
                if (unit != null && !unit.equals(CHAR_ONE) && value != null && value.length() > 0) {
                    unit = translateStrengthUnit(unit);
                    LOGGER.debug("value (before numberFormat): '{}'", value);
                    strength = NUMBER_FORMAT.format(new Double(value)) + CHAR_SPACE + unit;
                }

                if (denumeratorNode.getAttributes().getNamedItem("nullFlavor") == null) {
                    String dNValue = denumeratorNode.getAttributes().getNamedItem("value").getNodeValue();
                    String dNUnit = denumeratorNode.getAttributes().getNamedItem("unit").getNodeValue();
                    if (StringUtils.isNotBlank(dNUnit) && !dNUnit.equals(CHAR_ONE)) {
                        dNUnit = translateStrengthUnit(dNUnit);
                        if (dNValue != null && !dNValue.equals(CHAR_ONE)) {
                            LOGGER.debug("dNValue (before numberFormat): '{}'", dNValue);
                            strength += CHAR_FORWARD_SLASH + NUMBER_FORMAT.format(new Double(dNValue)) + CHAR_SPACE + dNUnit;
                        } else {
                            strength += CHAR_FORWARD_SLASH + dNUnit;
                        }
                    }
                } else {
                    String nullflavor = denumeratorNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                    if (!Validator.isNull(nullflavor)) {
                        strength += CHAR_FORWARD_SLASH + NullFlavorManager.getNullFlavor(nullflavor);
                    }
                }
            } else {
                if (numeratorNode != null) {
                    strength = numeratorNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                }
                if (!Validator.isNull(strength)) {
                    strength = NullFlavorManager.getNullFlavor(strength);
                }
            }
        } else {
            if (quantityNode != null) {
                strength = quantityNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
            }
            if (!Validator.isNull(strength)) {
                strength = NullFlavorManager.getNullFlavor(strength);
            }
        }
        LOGGER.debug("strength: '{}'", strength);

        return strength;
    }

    private String translateStrengthUnit(String dNUnit) {

        String translated = new StringResourceModel("strength.unit." + dNUnit, null, dNUnit).getString();
        LOGGER.debug("Strength unit original '{}', translated: '{}'", dNUnit, translated);
        return translated;
    }

    private void handleIngredient(Ingredient ingredientVO, XPath xpath, Node ingredientNode) throws XPathExpressionException {

        XPathExpression ingredientExpression = xpath.compile("epsos:ingredient/epsos:code");
        Node ingrNode = (Node) ingredientExpression.evaluate(ingredientNode, XPathConstants.NODE);
        if (ingrNode != null) {
            Node nf = ingrNode.getAttributes().getNamedItem("nullFlavor");
            if (nf != null) {
                ingredientVO.setActiveIngredient(NullFlavorManager.getNullFlavor(nf.getNodeValue()));
            } else {
                ingredientVO.setActiveIngredient(ingrNode.getAttributes().getNamedItem("code").getNodeValue()
                        + " - " + ingrNode.getAttributes().getNamedItem("displayName").getNodeValue());
            }
        }
        XPathExpression nameExpression = xpath.compile("epsos:ingredient/epsos:name");
        Node nameNode = (Node) nameExpression.evaluate(ingredientNode, XPathConstants.NODE);
        if (nameNode != null) {
            Node nf = nameNode.getAttributes().getNamedItem("nullFlavor");
            if (nf != null) {
                ingredientVO.setActiveIngredientName(NullFlavorManager.getNullFlavor(nf.getNodeValue()));
            } else {
                ingredientVO.setActiveIngredientName(nameNode.getTextContent());
            }
        }
    }

    private QuantityVO handleQuantity(XPathExpression exp, Node inNode) throws XPathExpressionException {

        String quantityValue = "";
        String quantityUnit = "";
        String quantityUnitUcum = "";
        Node node = (Node) exp.evaluate(inNode, XPathConstants.NODE);
        if (node != null) {
            if (node.getAttributes().getNamedItem("value") != null) {
                quantityValue = node.getAttributes().getNamedItem("value").getNodeValue();
                LOGGER.debug("Value: '{}'", quantityValue);
                if (quantityValue.contains(",")) {
                    quantityValue = quantityValue.replace(",", ".");
                } else if (quantityValue.contains(".")) {
                    double d = Double.parseDouble(quantityValue);
                    DecimalFormat df = new DecimalFormat("#0.0#");
                    quantityValue = df.format(d).replace(",", ".");
                }
                LOGGER.debug("Formatted value: '{}'", quantityValue);
            }

            if (node.getAttributes().getNamedItem("unit") != null) {
                quantityUnitUcum = node.getAttributes().getNamedItem("unit").getNodeValue();
                LOGGER.debug("Unit in UCUM: '{}'", quantityUnitUcum);
                quantityUnit = translateQuantityUnit(quantityUnitUcum);
            }
        }
        return new QuantityVO(quantityValue, quantityUnit, quantityUnitUcum);
    }

    private String translateQuantityUnit(String cdaQU) {

        String translated = new StringResourceModel("quantity.unit." + cdaQU, null, cdaQU).getString();
        LOGGER.debug("Translated unit: '{}'", translated);
        return translated;
    }

    private String handleAssignedPerson(Node sectionNode, XPathExpression prefixExpr, XPathExpression givenNameExpr,
                                        XPathExpression familyNameExpr) throws XPathExpressionException {

        StringBuilder returnString = new StringBuilder();

        Node prefix = (Node) prefixExpr.evaluate(sectionNode, XPathConstants.NODE);
        if (prefix != null) {
            returnString.append(prefix.getTextContent().trim()).append(CHAR_SPACE);
        }

        Node familyName = (Node) familyNameExpr.evaluate(sectionNode, XPathConstants.NODE);
        if (familyName != null) {
            returnString.append(familyName.getTextContent().trim()).append(",").append(CHAR_SPACE);
        }

        NodeList givenNames = (NodeList) givenNameExpr.evaluate(sectionNode, XPathConstants.NODESET);
        if (givenNames != null) {
            for (int i = 0; i < givenNames.getLength(); i++) {
                returnString.append(givenNames.item(i).getTextContent().trim()).append(CHAR_SPACE);
            }
        }

        return returnString.toString().trim();
    }

    private List<String> handlePrescriberTelecom(Document dom, XPath xpath) throws XPathExpressionException {

        String prescriberContact = "";
        List<String> list = new ArrayList<>();
        XPathExpression telecomExpression = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:telecom");
        NodeList prescriberTelecomNodeList = (NodeList) telecomExpression.evaluate(dom, XPathConstants.NODESET);
        if (prescriberTelecomNodeList != null && prescriberTelecomNodeList.getLength() > 0) {
            for (int i = 0; i < prescriberTelecomNodeList.getLength(); i++) {
                if (prescriberTelecomNodeList.item(i).getAttributes().getNamedItem("value") != null) {
                    prescriberContact = prescriberTelecomNodeList.item(i).getAttributes().getNamedItem("value").getTextContent();
                }

                if (Validator.isNull(prescriberContact)) {
                    prescriberContact = prescriberTelecomNodeList.item(i).getAttributes().getNamedItem("nullFlavor").getNodeValue();
                    if (!Validator.isNull(prescriberContact)) {
                        if (i == 0) {
                            prescriberContact = NullFlavorManager.getNullFlavor(prescriberContact);
                        } else {
                            prescriberContact = "";
                        }
                    }
                }
                list.add(prescriberContact);
                prescriberContact = "";
            }
        }
        return list;
    }

    private Date handlePrescriptionDate(Document dom, XPath xpath) throws XPathExpressionException {

        Date prescriptionDate = null;
        XPathExpression prescriptionDateExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:time");
        Node prescrDate = (Node) prescriptionDateExpr.evaluate(dom, XPathConstants.NODE);
        if (prescrDate != null) {
            prescriptionDate = getDate(prescrDate);
        }
        return prescriptionDate;
    }

    private String handleFacility(Document dom, XPath xpath) throws XPathExpressionException {

        String facility = "";
        XPathExpression facilityNameExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:name");
        Node facilityNode = (Node) facilityNameExpr.evaluate(dom, XPathConstants.NODE);
        if (facilityNode != null) {
            facility = facilityNode.getTextContent().trim();
            if (Validator.isNull(facility)) {
                facility = facilityNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                if (!Validator.isNull(facility)) {
                    facility = NullFlavorManager.getNullFlavor(facility);
                }
            }
        }
        return facility;
    }

    private String handleAdress(Document dom, XPath xpath) throws XPathExpressionException {

        String address = "";
        XPathExpression facilityAddressStreetExpr = xpath.compile(
                "/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:addr/hl7:streetAddressLine");
        Node street = (Node) facilityAddressStreetExpr.evaluate(dom, XPathConstants.NODE);
        if (street != null) {
            address += street.getTextContent().trim();
        }

        XPathExpression facilityAddressZipExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:addr/hl7:postalCode");
        Node zip = (Node) facilityAddressZipExpr.evaluate(dom, XPathConstants.NODE);
        if (zip != null) {
            address += ", " + zip.getTextContent().trim();
        }

        XPathExpression facilityAddressCityExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:addr/hl7:city");
        Node city = (Node) facilityAddressCityExpr.evaluate(dom, XPathConstants.NODE);
        if (city != null) {
            address += ", " + city.getTextContent().trim();
        }
        String country = handleCountryPrescriber(dom, xpath);
        if (country != null) {
            address += ", " + country;
        }

        return address;
    }

    private String handleCountryPrescriber(Document dom, XPath xpath) throws XPathExpressionException {

        String country = "";
        XPathExpression facilityAddressCountryExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:addr/hl7:country");
        Node countryNode = (Node) facilityAddressCountryExpr.evaluate(dom, XPathConstants.NODE);
        if (countryNode != null) {
            country = countryNode.getTextContent().trim();
        }
        return country;
    }

    private String handleEffectiveTimeTypePivl_Ts(XPath xpath, Node entryNode) throws XPathExpressionException {

        XPathExpression effectiveTime = xpath.compile("hl7:substanceAdministration/hl7:effectiveTime[@xsi:type='PIVL_TS']");
        XPathExpression period = xpath.compile("hl7:substanceAdministration/hl7:effectiveTime[@xsi:type='PIVL_TS']/hl7:period");
        String freqString = "";
        Node effectiveTimeNode = (Node) effectiveTime.evaluate(entryNode, XPathConstants.NODE);
        if (effectiveTimeNode != null) {
            Node periodNode = (Node) period.evaluate(entryNode, XPathConstants.NODE);
            if (periodNode != null && periodNode.getAttributes().getNamedItem("value") != null
                    && periodNode.getAttributes().getNamedItem("unit") != null) {
                freqString = periodNode.getAttributes().getNamedItem("value").getNodeValue() + CHAR_SPACE
                        + periodNode.getAttributes().getNamedItem("unit").getNodeValue();
            } else {
                if (effectiveTimeNode.getAttributes().getNamedItem("nullFlavor") != null) {
                    freqString = effectiveTimeNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                } else if (periodNode != null && periodNode.getAttributes().getNamedItem("nullFlavor") != null) {
                    freqString = periodNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                }
                if (!Validator.isNull(freqString)) {
                    freqString = NullFlavorManager.getNullFlavor(freqString);
                }
            }
        }
        return freqString;
    }

    private String handleTypeOfPackage(XPath xpath, Node entryNode) throws XPathExpressionException {

        String typeOfPackage = "";
        XPathExpression packTypeExpr = xpath.compile(
                "hl7:substanceAdministration/hl7:consumable/hl7:manufacturedProduct/hl7:manufacturedMaterial/epsos:asContent/epsos:containerPackagedMedicine/epsos:formCode");
        Node packType = (Node) packTypeExpr.evaluate(entryNode, XPathConstants.NODE);
        if (packType != null) {
            if (packType.getAttributes().getNamedItem("displayName") != null) {
                typeOfPackage = packType.getAttributes().getNamedItem("displayName").getNodeValue();
            }

            if (Validator.isNull(typeOfPackage)) {
                typeOfPackage = packType.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                if (!Validator.isNull(typeOfPackage)) {
                    typeOfPackage = NullFlavorManager.getNullFlavor(typeOfPackage);
                }
            }
        }
        return typeOfPackage;
    }

    private String handleProfession(Document dom, XPath xpath) throws XPathExpressionException {

        String profession = "";
        XPathExpression professionExpr = xpath.compile("/hl7:ClinicalDocument/hl7:author/hl7:functionCode");
        Node professionNode = (Node) professionExpr.evaluate(dom, XPathConstants.NODE);
        if (professionNode != null) {
            profession = professionNode.getAttributes().getNamedItem("displayName").getNodeValue();
            if (Validator.isNull(profession)) {
                profession = professionNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                if (!Validator.isNull(profession)) {
                    profession = NullFlavorManager.getNullFlavor(profession);
                }
            }
        }
        return profession;
    }

    private String handleRoute(XPath xpath, Node entryNode) throws XPathExpressionException {

        String routeString = "";
        XPathExpression routeExpr = xpath.compile("hl7:substanceAdministration/hl7:routeCode");
        Node route = (Node) routeExpr.evaluate(entryNode, XPathConstants.NODE);
        if (route != null) {
            if (route.getAttributes().getNamedItem("displayName") != null) {
                routeString = route.getAttributes().getNamedItem("displayName").getNodeValue();
            }
            if (Validator.isNull(routeString)) {
                routeString = route.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                if (!Validator.isNull(routeString)) {
                    routeString = NullFlavorManager.getNullFlavor(routeString);
                }
            }
        }
        return routeString;
    }

    private SubstitutionPermitted handleSubstitution(Node substituteNode) {

        String substituteValue = "";

        if (substituteNode != null) {
            if (substituteNode.getAttributes().getNamedItem("code") != null) {
                substituteValue = substituteNode.getAttributes().getNamedItem("code").getNodeValue();
            }
            if (Validator.isNull(substituteValue)) {
                substituteValue = substituteNode.getAttributes().getNamedItem("nullFlavor").getNodeValue();
                if (!Validator.isNull(substituteValue)) {
                    return new SubstitutionPermitted(false, NullFlavorManager.getNullFlavor(substituteValue));
                }
            }
        }

        if (StringUtils.equals(substituteValue, "TE")) {
            return new SubstitutionPermitted(true, new StringResourceModel("prescription.substitute.therapeutic", null, "").getString());
        } else if (StringUtils.equals(substituteValue, "G")) {
            return new SubstitutionPermitted(true, new StringResourceModel("prescription.substitute.generic", null, "").getString());
        } else {
            return new SubstitutionPermitted(false, new StringResourceModel("prescription.substitute.null", null, "").getString());
        }
    }

    private Date getDate(Node node) {

        Date date = null;
        if (node != null) {
            if (node.getAttributes().getNamedItem("value") != null) {
                try {
                    String str = node.getAttributes().getNamedItem("value").getNodeValue();
                    date = DateUtil.formatStringToDate(str.substring(0, 8));
                } catch (Exception e) {
                    LOGGER.error("Error formating node from string to date.", e);
                }
            } else {
                return null;
            }
        }
        return date;
    }

    private String handleReferenceXPath(XPath xpath, Node entryNode, String reference) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {

        String refValue = "";
        if (reference != null && reference.length() > 1) {
            XPathExpression referenceExpr = xpath.compile("//*[@ID='" + reference.substring(1) + "']");
            Node refValueNode = (Node) referenceExpr.evaluate(entryNode, XPathConstants.NODE);
            refValue = transformNodeContentToString(refValueNode);
            LOGGER.info("Reference('{}') found: '{}'", reference, refValue);
        }
        return refValue;
    }

    private String transformNodeContentToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {

        if (node != null && node.hasChildNodes()) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            StringWriter stw = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(stw));
            String nodeContent = stw.toString();
            return nodeContent.substring(nodeContent.indexOf('>') + 1, nodeContent.lastIndexOf('<'));
        } else {
            return "";
        }
    }

    public static class Validator {

        public static boolean isNull(String str) {
            return (str == null || str.isEmpty());
        }
    }

    private class SubstitutionPermitted {

        private String substitutionPermittedText;
        private boolean substitutionPermitted;

        private SubstitutionPermitted(boolean substitutionPermitted, String substitutionPermittedText) {

            this.substitutionPermitted = substitutionPermitted;
            this.substitutionPermittedText = substitutionPermittedText;
        }

        public String getSubstitutionPermittedText() {
            return substitutionPermittedText;
        }

        public boolean isSubstitutionPermitted() {
            return substitutionPermitted;
        }
    }
}
