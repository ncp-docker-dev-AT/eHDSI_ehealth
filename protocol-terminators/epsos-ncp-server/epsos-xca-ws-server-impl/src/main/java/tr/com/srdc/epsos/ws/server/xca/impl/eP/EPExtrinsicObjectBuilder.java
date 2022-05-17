package tr.com.srdc.epsos.ws.server.xca.impl.eP;

import eu.epsos.util.IheConstants;
import eu.epsos.util.xca.XCAConstants;
import eu.epsos.util.xdr.XDRConstants;
import fi.kela.se.epsos.data.model.EPDocumentMetaData;
import fi.kela.se.epsos.data.model.EPSOSDocumentMetaData;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ClassificationType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import org.springframework.http.MediaType;
import tr.com.srdc.epsos.data.model.SubstitutionCodeEnum;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.DateUtil;
import tr.com.srdc.epsos.ws.server.xca.impl.ClassificationBuilder;
import tr.com.srdc.epsos.ws.server.xca.impl.SlotBuilder;

import java.util.UUID;

public class EPExtrinsicObjectBuilder {

    public static String build(AdhocQueryRequest request, ExtrinsicObjectType eot, EPDocumentMetaData document) {

        var ofRim = new oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory();

        var name = "eHDSI - ePrescription";
        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        boolean isPDF = document.getFormat() == EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF;

        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType(XCAConstants.XDS_DOC_ENTRY_CLASSIFICATION_NODE);

        // Status
        eot.setMimeType(MediaType.TEXT_XML_VALUE);

        // Name
        eot.setName(ofRim.createInternationalStringType());
        eot.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getName().getLocalizedString().get(0).setValue(name);

        // Description
        eot.setDescription(ofRim.createInternationalStringType());
        eot.getDescription().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getDescription().getLocalizedString().get(0).setValue(document.getDescription());

        // Version Info
        eot.setVersionInfo(ofRim.createVersionInfoType());
        eot.getVersionInfo().setVersionName("1");

        // Creation Date (optional)
        eot.getSlot().add(SlotBuilder.build("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", document.getEffectiveTime())));

        // Source Patient Id
        eot.getSlot().add(SlotBuilder.build("sourcePatientId", getDocumentEntryPatientId(request)));

        // LanguageCode (optional)
        String languageCode = document.getLanguage() == null ? Constants.LANGUAGE_CODE : document.getLanguage();
        eot.getSlot().add(SlotBuilder.build("languageCode", languageCode));

        // repositoryUniqueId (optional)
        eot.getSlot().add(SlotBuilder.build("repositoryUniqueId", document.getRepositoryId()));

        eot.getClassification().add(
                ClassificationBuilder.build(
                        XDRConstants.EXTRINSIC_OBJECT.CLASS_CODE_SCHEME, uuid,
                        Constants.EP_CLASSCODE, "2.16.840.1.113883.6.1", name));
        // Type code (not written in 3.4.2)
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
                uuid, Constants.EP_CLASSCODE, "2.16.840.1.113883.6.1", name));

        // Dispensable
        if (document.isDispensable()) {
            ClassificationType dispensableClassification = ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, "urn:ihe:iti:xdw:2011:eventCode:open", "1.3.6.1.4.1.19376.1.2.3", "Open");
            dispensableClassification.getSlot().add(SlotBuilder.build("dispensable", "Open"));
            eot.getClassification().add(dispensableClassification);
        } else {
            ClassificationType dispensableClassification = ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, "urn:ihe:iti:xdw:2011:eventCode:closed", "1.3.6.1.4.1.19376.1.2.3", "Closed");
            dispensableClassification.getSlot().add(SlotBuilder.build("dispensable", "Closed"));
            eot.getClassification().add(dispensableClassification);
        }

        // ATC code (former Product element)
        ClassificationType atcCodeClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                document.getAtcCode(), "2.16.840.1.113883.6.73", document.getAtcName());
        eot.getClassification().add(atcCodeClassification);

        // Dose Form Code
        ClassificationType doseFormClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                document.getDoseFormCode(), "0.4.0.127.0.16.1.1.2.1", document.getDoseFormName());
        eot.getClassification().add(doseFormClassification);

        // Strength
        ClassificationType strengthClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                document.getStrength(), "eHDSI_Strength_CodeSystem", "Strength of medication");
        eot.getClassification().add(strengthClassification);

        // Substitution
        String substitutionCode = document.getSubstitution() != null
                ? document.getSubstitution().getSubstitutionCode()
                : SubstitutionCodeEnum.G.name();
        String substitutionDisplay = document.getSubstitution() != null
                ? document.getSubstitution().getSubstitutionDisplayName()
                : SubstitutionCodeEnum.G.getDisplayName();
        ClassificationType substitutionClassification = ClassificationBuilder.build(
                "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                substitutionCode, "2.16.840.1.113883.5.1070", substitutionDisplay);
        eot.getClassification().add(substitutionClassification);

        // Confidentiality Code
        String confidentialityCode = document.getConfidentiality() != null
                && document.getConfidentiality().getConfidentialityCode() != null
                ? document.getConfidentiality().getConfidentialityCode()
                : "N";
        String confidentialityDisplay = document.getConfidentiality() != null
                && document.getConfidentiality().getConfidentialityDisplay() != null
                ? document.getConfidentiality().getConfidentialityDisplay()
                : "Normal";
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
                uuid, confidentialityCode, "2.16.840.1.113883.5.25", confidentialityDisplay));
        // FormatCode
        if (isPDF) {
            eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                    uuid, XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.PdfSourceCoded.NODE_REPRESENTATION, "IHE PCC",
                    XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.PdfSourceCoded.DISPLAY_NAME));
        } else {
            eot.getClassification().add(ClassificationBuilder.build(IheConstants.FORMAT_CODE_SCHEME,
                    uuid, XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.NODE_REPRESENTATION,
                    XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.CODING_SCHEME,
                    XCAConstants.EXTRINSIC_OBJECT.FormatCode.EPrescription.EpsosPivotCoded.DISPLAY_NAME));
        }
        // Healthcare facility code
        // TODO: Get healthcare facility info from national implementation
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
                uuid, Constants.COUNTRY_CODE, "1.0.3166.1", Constants.COUNTRY_NAME));

        // Practice Setting code
        eot.getClassification().add(ClassificationBuilder.build("urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
                uuid, "Not Used", "eHDSI Practice Setting Codes-Not Used", "Not Used"));

        // Author Person
        ClassificationType authorClassification = ClassificationBuilder.build(
                IheConstants.CLASSIFICATION_SCHEME_AUTHOR_UUID, uuid, "");
        authorClassification.getSlot().add(SlotBuilder.build(IheConstants.AUTHOR_PERSON_STR, document.getAuthor()));
        eot.getClassification().add(authorClassification);

        // External Identifiers
        eot.getExternalIdentifier().add(makeExternalIdentifier("urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
                uuid, getDocumentEntryPatientId(request), "XDSDocumentEntry.patientId"));

        eot.getExternalIdentifier().add(makeExternalIdentifier(XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_SCHEME,
                uuid, document.getId(), XDRConstants.EXTRINSIC_OBJECT.XDSDOC_UNIQUEID_STR));

        return uuid;
    }

    /**
     * Extracts the XDS patient ID from the XCA query
     */
    private static String getDocumentEntryPatientId(AdhocQueryRequest request) {

        for (SlotType1 sl : request.getAdhocQuery().getSlot()) {
            if (sl.getName().equals("$XDSDocumentEntryPatientId")) {
                String patientId = sl.getValueList().getValue().get(0);
                patientId = patientId.substring(1, patientId.length() - 1);
                return patientId;
            }
        }
        return "$XDSDocumentEntryPatientId Not Found!";
    }

    private static ExternalIdentifierType makeExternalIdentifier(String identificationScheme, String registryObject, String value, String name) {

        var ofRim = new oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory();
        var uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        var externalIdentifierType = ofRim.createExternalIdentifierType();
        externalIdentifierType.setId(uuid);
        externalIdentifierType.setIdentificationScheme(identificationScheme);
        externalIdentifierType.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier");
        externalIdentifierType.setRegistryObject(registryObject);
        externalIdentifierType.setValue(value);

        externalIdentifierType.setName(ofRim.createInternationalStringType());
        externalIdentifierType.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        externalIdentifierType.getName().getLocalizedString().get(0).setValue(name);
        return externalIdentifierType;
    }
}
