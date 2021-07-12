package eu.epsos.dts.xds;

import eu.epsos.util.IheConstants;
import eu.epsos.util.xdr.XDRConstants;
import fi.kela.se.epsos.data.model.OrCDDocumentMetaData;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.*;
import org.apache.commons.lang3.StringUtils;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;
import tr.com.srdc.epsos.data.model.xds.XDSDocumentAssociation;
import tr.com.srdc.epsos.util.Constants;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents a Data Transfer Service, used by XCA and AdhocQueryResponse messages.
 */
public final class AdhocQueryResponseConverter {

    /**
     * Private constructor to avoid instantiation.
     */
    private AdhocQueryResponseConverter() {
    }

    /**
     * Transforms a AdhocQueryResponse in a QueryResponse.
     *
     * @param response - in AdhocQueryResponse format
     * @return a QueryResponse object.
     */
    public static QueryResponse convertAdhocQueryResponse(AdhocQueryResponse response) {

        var queryResponse = new QueryResponse();

        if (response.getRegistryObjectList() != null) {
            Map<String, String> documentAssociationsMap = new TreeMap<>();
            List<XDSDocument> documents = new ArrayList<>();
            String str;

            for (var i = 0; i < response.getRegistryObjectList().getIdentifiable().size(); i++) {
                JAXBElement<?> o = response.getRegistryObjectList().getIdentifiable().get(i);
                String declaredTypeName = o.getDeclaredType().getSimpleName();
                // TODO A.R. What should we do with Association?
                if ("ExtrinsicObjectType".equals(declaredTypeName)) {
                    var xdsDocument = new XDSDocument();
                    JAXBElement<ExtrinsicObjectType> eo;
                    eo = (JAXBElement<ExtrinsicObjectType>) response.getRegistryObjectList().getIdentifiable().get(i);

                    //Set id
                    xdsDocument.setId(eo.getValue().getId());

                    //Set hcid
                    xdsDocument.setHcid(eo.getValue().getHome());

                    // Set name
                    xdsDocument.setName(eo.getValue().getName().getLocalizedString().get(0).getValue());

                    // Set mimeType
                    xdsDocument.setMimeType(eo.getValue().getMimeType());

                    // Set documentUniqueId
                    for (ExternalIdentifierType externalIdentifierType : eo.getValue().getExternalIdentifier()) {
                        if (externalIdentifierType.getName().getLocalizedString().get(0).getValue()
                                .equalsIgnoreCase(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_STR)) {
                            xdsDocument.setDocumentUniqueId(externalIdentifierType.getValue());
                        }
                    }

                    for (SlotType1 slotType : eo.getValue().getSlot()) {
                        switch (slotType.getName()) {
                            case "creationTime":
                                xdsDocument.setCreationTime(slotType.getValueList().getValue().get(0));
                                break;
                            case "serviceStartTime":
                                xdsDocument.setEventTime(slotType.getValueList().getValue().get(0));
                                break;
                            case "size":
                                xdsDocument.setSize(slotType.getValueList().getValue().get(0));
                                break;
                            case "repositoryUniqueId":
                                xdsDocument.setRepositoryUniqueId(slotType.getValueList().getValue().get(0));
                                break;
                        }
                    }
                    var documentClassCodeType = "";
                    for (var j = 0; j < eo.getValue().getClassification().size(); j++) {
                        str = eo.getValue().getClassification().get(j).getClassificationScheme();
                        //Set isPDF
                        if (StringUtils.equals(str, IheConstants.FORMAT_CODE_SCHEME)) {
                            xdsDocument.setPDF(eo.getValue().getClassification().get(j).getNodeRepresentation().equals("urn:ihe:iti:xds-sd:pdf:2008"));
                            // Set FormatCode
                            xdsDocument.setFormatCode(eo.getValue().getClassification().get(j).getSlot().get(0).getValueList().getValue().get(0), eo.getValue().getClassification().get(j).getNodeRepresentation());
                        }

                        // Set healthcareFacility
                        if (str.equals("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1")) {
                            xdsDocument.setHealthcareFacility(eo.getValue().getClassification().get(j).getName().getLocalizedString().get(0).getValue());
                        }

                        // Set ClassCode
                        if (str.equals(XDRConstants.EXTRINSIC_OBJECT.CLASS_CODE_SCHEME)) {
                            documentClassCodeType = eo.getValue().getClassification().get(j).getNodeRepresentation();
                            xdsDocument.setClassCode(eo.getValue().getClassification().get(j).getSlot().get(0).getValueList().getValue().get(0), documentClassCodeType);
                        }

                        // Set AuthorPerson
                        if (str.equals(IheConstants.CLASSIFICATION_SCHEME_AUTHOR_UUID) && eo.getValue().getClassification().get(j).getSlot() != null) {
                            var author = new OrCDDocumentMetaData.Author();
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals(IheConstants.AUTHOR_PERSON_STR) && slot.getValueList().getValue().get(0) != null) {
                                    author.setAuthorPerson(slot.getValueList().getValue().get(0));
                                }
                            }
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals(IheConstants.AUTHOR_SPECIALITY_STR) && !slot.getValueList().getValue().isEmpty()) {
                                    author.setAuthorSpeciality(slot.getValueList().getValue());
                                }
                            }
                            xdsDocument.getAuthors().add(author);
                        }

                        // Set ATC Code
                        if (str.equals(IheConstants.CLASSIFICATION_EVENT_CODE_LIST) && eo.getValue().getClassification().get(j).getSlot() != null) {
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals("atcCode") && slot.getValueList().getValue().get(0) != null) {
                                    xdsDocument.setAtcCode(slot.getValueList().getValue().get(0));
                                    xdsDocument.setAtcText(slot.getValueList().getValue().get(0));
                                }
                            }
                        }

                        // Set Dose Form Code
                        if (str.equals(IheConstants.CLASSIFICATION_EVENT_CODE_LIST) && eo.getValue().getClassification().get(j).getSlot() != null) {
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals("doseFormCode") && slot.getValueList().getValue().get(0) != null) {
                                    xdsDocument.setDoseFormCode(slot.getValueList().getValue().get(0));
                                    xdsDocument.setDoseFormText(slot.getValueList().getValue().get(0));
                                }
                            }
                        }

                        // Set Strength
                        if (str.equals(IheConstants.CLASSIFICATION_EVENT_CODE_LIST) && eo.getValue().getClassification().get(j).getSlot() != null) {
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals("strength") && slot.getValueList().getValue().get(0) != null) {
                                    xdsDocument.setStrength(slot.getValueList().getValue().get(0));
                                }
                            }
                        }

                        // Set Substitution
                        if (str.equals(IheConstants.CLASSIFICATION_EVENT_CODE_LIST) && eo.getValue().getClassification().get(j).getSlot() != null) {
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals("substitution") && slot.getValueList().getValue().get(0) != null) {
                                    xdsDocument.setSubstitution(slot.getValueList().getValue().get(0));
                                }
                            }
                        }

                        // Set Dispensable
                        if (eo.getValue().getDescription() != null && !eo.getValue().getDescription().getLocalizedString().isEmpty()) {
                            boolean dispensable = false;
                            if (StringUtils.equals(documentClassCodeType, Constants.EP_CLASSCODE)) {
                                List<ClassificationType> classificationTypeList = eo.getValue().getClassification();
                                for (ClassificationType classificationType : classificationTypeList) {
                                    if (StringUtils.equals(classificationType.getClassificationScheme(), "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4")) {
                                        if (StringUtils.equals(classificationType.getNodeRepresentation(), "urn:ihe:iti:xdw:2011:eventCode:open")
                                                && StringUtils.equals(classificationType.getSlot().get(0).getValueList().getValue().get(0), "1.3.6.1.4.1.19376.1.2.3")) {
                                            dispensable = true;
                                        } else if (StringUtils.equals(classificationType.getNodeRepresentation(), "urn:ihe:iti:xdw:2011:eventCode:closed")
                                                && StringUtils.equals(classificationType.getSlot().get(0).getValueList().getValue().get(0), "1.3.6.1.4.1.19376.1.2.3")) {
                                            dispensable = false;
                                        }
                                    }
                                }
                            }
                            xdsDocument.setDispensable(dispensable);
                        }

                        // Set Reason of Hospitalisation
                        if (str.equals(IheConstants.CLASSIFICATION_EVENT_CODE_LIST) && eo.getValue().getClassification().get(j) != null) {
                            String code = eo.getValue().getClassification().get(j).getNodeRepresentation();
                            String text = eo.getValue().getClassification().get(j).getName().getLocalizedString().get(0).getValue();
                            String codingScheme = null;
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals("codingScheme") && slot.getValueList().getValue().get(0) != null) {
                                    codingScheme = slot.getValueList().getValue().get(0);
                                }
                            }
                            xdsDocument.setReasonOfHospitalisation(new OrCDDocumentMetaData.ReasonOfHospitalisation(code, codingScheme, text));
                            if (codingScheme.trim().equals("1.3.6.1.4.1.12559.11.10.1.3.1.44.2")) {
                                xdsDocument.setReasonOfHospitalisation(new OrCDDocumentMetaData.ReasonOfHospitalisation(code, codingScheme, text));
                            }
                        }
                    }

                    // Set description
                    /*
                    if (eo.getValue().getDescription() != null && !eo.getValue().getDescription().getLocalizedString().isEmpty()) {

                        if (StringUtils.equals(documentClassCodeType, Constants.EP_CLASSCODE)) {

                            var status = "N/A";
                            var code = "N/A";
                            List<ClassificationType> classificationTypeList = eo.getValue().getClassification();
                            for (ClassificationType classificationType : classificationTypeList) {

                                if (StringUtils.equals(classificationType.getClassificationScheme(), "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4")) {

                                    if (StringUtils.equals(classificationType.getNodeRepresentation(), "urn:ihe:iti:xdw:2011:eventCode:open")
                                            && StringUtils.equals(classificationType.getSlot().get(0).getValueList().getValue().get(0), "1.3.6.1.4.1.19376.1.2.3")) {
                                        status = "Dispensable";
                                    } else if (StringUtils.equals(classificationType.getNodeRepresentation(), "urn:ihe:iti:xdw:2011:eventCode:closed")
                                            && StringUtils.equals(classificationType.getSlot().get(0).getValueList().getValue().get(0), "1.3.6.1.4.1.19376.1.2.3")) {
                                        status = "Not Dispensable";
                                    } else {
                                        code = classificationType.getNodeRepresentation();
                                    }
                                }
                            }
                            xdsDocument.setDescription("(ATC: " + code + ") - " + eo.getValue().getDescription().getLocalizedString().get(0).getValue() + " / " + status);
                        } else {
                            xdsDocument.setDescription(eo.getValue().getDescription().getLocalizedString().get(0).getValue());
                        }
                    }
                    */
                    if (eo.getValue().getDescription() != null && !eo.getValue().getDescription().getLocalizedString().isEmpty()) {
                        xdsDocument.setDescription(eo.getValue().getDescription().getLocalizedString().get(0).getValue());
                    }

                    documents.add(xdsDocument);

                } else if ("AssociationType1".equals(declaredTypeName)) {
                    JAXBElement<AssociationType1> eo;
                    eo = (JAXBElement<AssociationType1>) response.getRegistryObjectList().getIdentifiable().get(i);
                    if (eo.getValue().getAssociationType().equals("urn:ihe:iti:2007:AssociationType:XFRM")) {
                        documentAssociationsMap.put(eo.getValue().getSourceObject(), eo.getValue().getTargetObject());
                    }
                }
            }

            List<XDSDocumentAssociation> documentAssociations = new ArrayList<>();
            for (Map.Entry<String, String> entry : documentAssociationsMap.entrySet()) {

                String sourceObjectId = entry.getKey();
                String targetObjectId = entry.getValue();

                XDSDocument sourceObject = null;
                XDSDocument targetObject = null;

                for (XDSDocument doc : documents) {
                    if (doc.getId().matches(targetObjectId) && doc.getId().matches(sourceObjectId)) {
                        //OrCD
                        sourceObject = doc;
                        targetObject = doc;
                    } else if (doc.getId().matches(sourceObjectId)) {
                        sourceObject = doc;
                    } else if (doc.getId().matches(targetObjectId)) {
                        targetObject = doc;
                    } else {
                        continue;
                    }

                    if (sourceObject != null && targetObject != null) {
                        break;
                    }
                }

                if (sourceObject != null && targetObject != null) {
                    var xdsDocumentAssociation = new XDSDocumentAssociation();

                    if (sourceObject.isPDF()) {
                        xdsDocumentAssociation.setCdaPDF(sourceObject);
                    } else {
                        xdsDocumentAssociation.setCdaXML(sourceObject);
                    }

                    if (targetObject.isPDF()) {
                        xdsDocumentAssociation.setCdaPDF(targetObject);
                    } else {
                        xdsDocumentAssociation.setCdaXML(targetObject);
                    }

                    documentAssociations.add(xdsDocumentAssociation);
                }

                documents.remove(sourceObject);
                documents.remove(targetObject);

            }

            for (XDSDocument xdsDocument : documents) {
                var xdsDocumentAssociation = new XDSDocumentAssociation();
                xdsDocumentAssociation.setCdaPDF(xdsDocument.isPDF() ? xdsDocument : null);
                xdsDocumentAssociation.setCdaXML(xdsDocument.isPDF() ? null : xdsDocument);

                documentAssociations.add(xdsDocumentAssociation);
            }

            queryResponse.setDocumentAssociations(documentAssociations);
        }

        if (response.getRegistryErrorList() != null) {
            List<String> errors = new ArrayList<>(response.getRegistryErrorList().getRegistryError().size());

            for (var i = 0; i < response.getRegistryErrorList().getRegistryError().size(); i++) {
                errors.add(response.getRegistryErrorList().getRegistryError().get(i).getCodeContext());
            }

            queryResponse.setFailureMessages(errors);
        }

        return queryResponse;
    }
}
