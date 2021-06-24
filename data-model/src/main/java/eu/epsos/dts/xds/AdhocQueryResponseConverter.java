package eu.epsos.dts.xds;

import eu.epsos.util.IheConstants;
import eu.epsos.util.xdr.XDRConstants;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AdhocQueryResponseConverter.class);

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

        QueryResponse queryResponse = new QueryResponse();

        if (response.getRegistryObjectList() != null) {
            Map<String, String> documentAssociationsMap = new TreeMap<>();
            List<XDSDocument> documents = new ArrayList<>();
            String str;

            for (int i = 0; i < response.getRegistryObjectList().getIdentifiable().size(); i++) {
                JAXBElement<?> o = response.getRegistryObjectList().getIdentifiable().get(i);
                String declaredTypeName = o.getDeclaredType().getSimpleName();
                // TODO A.R. What should we do with Association?
                if ("ExtrinsicObjectType".equals(declaredTypeName)) {
                    XDSDocument document = new XDSDocument();
                    JAXBElement<ExtrinsicObjectType> eo;
                    eo = (JAXBElement<ExtrinsicObjectType>) response.getRegistryObjectList().getIdentifiable().get(i);

                    //Set id
                    document.setId(eo.getValue().getId());

                    //Set hcid
                    document.setHcid(eo.getValue().getHome());

                    // Set name
                    document.setName(eo.getValue().getName().getLocalizedString().get(0).getValue());

                    // Set documentUniqueId
                    for (ExternalIdentifierType idenType : eo.getValue().getExternalIdentifier()) {
                        if (idenType.getName().getLocalizedString().get(0).getValue().equalsIgnoreCase(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_STR)) {
                            document.setDocumentUniqueId(idenType.getValue());
                        }
                    }

                    for (int j = 0; j < eo.getValue().getSlot().size(); j++) {
                        str = eo.getValue().getSlot().get(j).getName();

                        // Set creationTime
                        if (str.equals("creationTime")) {
                            document.setCreationTime(eo.getValue().getSlot().get(j).getValueList().getValue().get(0));
                        }

                        // Set serviceStartTime
                        if (str.equals("serviceStartTime")) {
                            document.setEventTime(eo.getValue().getSlot().get(j).getValueList().getValue().get(0));
                        }

                        // Set size
                        if (str.equals("size")) {
                            document.setSize(eo.getValue().getSlot().get(j).getValueList().getValue().get(0));
                        }

                        // Set repositoryUniqueId
                        if (str.equals("repositoryUniqueId")) {
                            document.setRepositoryUniqueId(eo.getValue().getSlot().get(j).getValueList().getValue().get(0));
                        }
                    }
                    String documentClassCodeType = "";
                    for (int j = 0; j < eo.getValue().getClassification().size(); j++) {
                        str = eo.getValue().getClassification().get(j).getClassificationScheme();
                        //Set isPDF
                        if (StringUtils.equals(str, IheConstants.FORMAT_CODE_SCHEME)) {
                            if (eo.getValue().getClassification().get(j).getNodeRepresentation().equals("urn:ihe:iti:xds-sd:pdf:2008")) {
                                document.setPDF(true);
                            } else {
                                document.setPDF(false);
                            }
                            // Set FormatCode
                            document.setFormatCode(eo.getValue().getClassification().get(j).getSlot().get(0).getValueList().getValue().get(0), eo.getValue().getClassification().get(j).getNodeRepresentation());
                        }

                        // Set healthcareFacility
                        if (str.equals("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1")) {
                            document.setHealthcareFacility(eo.getValue().getClassification().get(j).getName().getLocalizedString().get(0).getValue());
                        }

                        // Set ClassCode
                        if (str.equals(XDRConstants.EXTRINSIC_OBJECT.CLASS_CODE_SCHEME)) {
                            documentClassCodeType = eo.getValue().getClassification().get(j).getNodeRepresentation();
                            document.setClassCode(eo.getValue().getClassification().get(j).getSlot().get(0).getValueList().getValue().get(0), documentClassCodeType);
                        }

                        // Set AuthorPerson
                        if (str.equals(IheConstants.CLASSIFICATION_SCHEME_AUTHOR_UUID) && eo.getValue().getClassification().get(j).getSlot() != null) {
                            for (SlotType1 slot : eo.getValue().getClassification().get(j).getSlot()) {
                                if (slot.getName().equals("authorPerson") && slot.getValueList().getValue().get(0) != null) {
                                    document.setAuthorPerson(slot.getValueList().getValue().get(0));
                                }
                            }
                        }
                    }

                    // Set description
                    if (eo.getValue().getDescription() != null && !eo.getValue().getDescription().getLocalizedString().isEmpty()) {

                        if (StringUtils.equals(documentClassCodeType, Constants.EP_CLASSCODE)) {

                            String status = "N/A";
                            String code = "N/A";
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
                            document.setDescription("(ATC: " + code + ") - " + eo.getValue().getDescription().getLocalizedString().get(0).getValue() + " / " + status);
                        } else {
                            document.setDescription(eo.getValue().getDescription().getLocalizedString().get(0).getValue());
                        }
                    }
                    documents.add(document);

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
                    if(doc.getId().matches(targetObjectId) && doc.getId().matches(sourceObjectId)) {
                        //OrCD
                        sourceObject = doc;
                        targetObject = doc;
                    }else if (doc.getId().matches(sourceObjectId)) {
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
                    XDSDocumentAssociation xdsDocumentAssociation = new XDSDocumentAssociation();

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

            for (XDSDocument doc : documents) {
                XDSDocumentAssociation xdsDocumentAssociation = new XDSDocumentAssociation();
                xdsDocumentAssociation.setCdaPDF(doc.isPDF() ? doc : null);
                xdsDocumentAssociation.setCdaXML(doc.isPDF() ? null : doc);

                documentAssociations.add(xdsDocumentAssociation);
            }

            queryResponse.setDocumentAssociations(documentAssociations);
        }

        if (response.getRegistryErrorList() != null) {
            List<String> errors = new ArrayList<>(response.getRegistryErrorList().getRegistryError().size());

            for (int i = 0; i < response.getRegistryErrorList().getRegistryError().size(); i++) {
                errors.add(response.getRegistryErrorList().getRegistryError().get(i).getCodeContext());
            }

            queryResponse.setFailureMessages(errors);
        }

        return queryResponse;
    }
}
