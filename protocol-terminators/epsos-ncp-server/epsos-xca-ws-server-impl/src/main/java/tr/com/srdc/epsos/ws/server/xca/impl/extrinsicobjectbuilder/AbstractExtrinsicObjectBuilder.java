package tr.com.srdc.epsos.ws.server.xca.impl.extrinsicobjectbuilder;

import eu.epsos.util.IheConstants;
import eu.epsos.util.xca.XCAConstants;
import eu.europa.ec.sante.ehdsi.constant.codesystem.CodeSystem;
import eu.europa.ec.sante.ehdsi.constant.ihe.ClassificationScheme;
import eu.europa.ec.sante.ehdsi.constant.ihe.XDSMetaData;
import fi.kela.se.epsos.data.model.EPSOSDocumentMetaData;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import org.springframework.http.MediaType;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.DateUtil;
import tr.com.srdc.epsos.ws.server.xca.impl.ClassificationBuilder;
import tr.com.srdc.epsos.ws.server.xca.impl.SlotBuilder;

import java.util.UUID;

public abstract class AbstractExtrinsicObjectBuilder {

    protected static ExtrinsicObjectType build(AdhocQueryRequest request, ExtrinsicObjectType eot, EPSOSDocumentMetaData documentMetaData, ObjectFactory ofRim, String uuid, String title) {

        // Set Extrinsic Object
        eot.setStatus(IheConstants.REGREP_STATUSTYPE_APPROVED);
        eot.setHome(Constants.OID_PREFIX + Constants.HOME_COMM_ID);
        eot.setId(uuid);
        eot.setLid(uuid);
        eot.setObjectType(XCAConstants.XDS_DOC_ENTRY_CLASSIFICATION_NODE);

        // MimeType
        eot.setMimeType(MediaType.TEXT_XML_VALUE);

        // Source Patient Id
        eot.getSlot().add(SlotBuilder.build("sourcePatientId", getDocumentEntryPatientId(request)));

        // Size
        // In the case of an On Demand document generation, no information on the size is available at the time of the XCA List
        if (documentMetaData.getSize() != null) {
            eot.getSlot().add(SlotBuilder.build("size", String.valueOf(documentMetaData.getSize())));
        }

        // Hash
        // In the case of an On Demand document generation, no information on the hash is available at the time of the XCA List
        if (documentMetaData.getHash() != null) {
            eot.getSlot().add(SlotBuilder.build("hash", String.valueOf(documentMetaData.getHash())));
        }

        // Creation Date (optional)
        eot.getSlot().add(SlotBuilder.build("creationTime", DateUtil.getDateByDateFormat("yyyyMMddHHmmss", documentMetaData.getEffectiveTime())));

        // repositoryUniqueId (optional)
        eot.getSlot().add(SlotBuilder.build("repositoryUniqueId", documentMetaData.getRepositoryId()));

        // LanguageCode (optional)
        String languageCode = documentMetaData.getLanguage() == null ? Constants.LANGUAGE_CODE : documentMetaData.getLanguage();
        eot.getSlot().add(SlotBuilder.build("languageCode", languageCode));

        // ConfidentialityCode
        String confidentialityCode = documentMetaData.getConfidentiality() == null
                || documentMetaData.getConfidentiality().getConfidentialityCode() == null ? "N"
                : documentMetaData.getConfidentiality().getConfidentialityCode();
        String confidentialityDisplay = documentMetaData.getConfidentiality() == null
                || documentMetaData.getConfidentiality().getConfidentialityDisplay() == null ? "Normal"
                : documentMetaData.getConfidentiality().getConfidentialityDisplay();
        eot.getClassification().add(ClassificationBuilder.build(ClassificationScheme.CONFIDENTIALITY.getUuid(),
                uuid, confidentialityCode, CodeSystem.HL7_CONFIDENTIALITY.getOID(), confidentialityDisplay));

        // Version Info
        eot.setVersionInfo(ofRim.createVersionInfoType());
        eot.getVersionInfo().setVersionName("1.1");

        // Patient ID
        eot.getExternalIdentifier().add(makeExternalIdentifier(ClassificationScheme.PATIENT_ID.getUuid(),
                uuid, getDocumentEntryPatientId(request), XDSMetaData.PATIENT_ID.getName()));
        // Unique ID
        eot.getExternalIdentifier().add(makeExternalIdentifier(ClassificationScheme.UNIQUE_ID.getUuid(),
                uuid, documentMetaData.getId(), XDSMetaData.UNIQUE_ID.getName()));

        // Name
        eot.setName(ofRim.createInternationalStringType());
        eot.getName().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getName().getLocalizedString().get(0).setValue(title);

        // Class code
        eot.getClassification().add(
                ClassificationBuilder.build(ClassificationScheme.CLASS_CODE.getUuid(), uuid,
                        documentMetaData.getClassCode().getCode(), CodeSystem.LOINC.getOID(), title));

        // Type code
        eot.getClassification().add(ClassificationBuilder.build(ClassificationScheme.TYPE_CODE.getUuid(),
                uuid, documentMetaData.getClassCode().getCode(), CodeSystem.LOINC.getOID(), title));

        // Healthcare facility code
        // TODO: Get healthcare facility info from national implementation
        eot.getClassification().add(ClassificationBuilder.build(ClassificationScheme.HEALTHCARE_FACILITY_CODE.getUuid(),
                uuid, Constants.COUNTRY_CODE, CodeSystem.ISO_COUNTRY_CODES.getOID(), Constants.COUNTRY_NAME));

        // Practice Setting code
        eot.getClassification().add(ClassificationBuilder.build(ClassificationScheme.PRACTICE_SETTING_CODE.getUuid(),
                uuid, "Not Used", "eHDSI Practice Setting Codes-Not Used", "Not Used"));

        return eot;
    }

    /**
     * Extracts the XDS patient ID from the XCA query
     */
    protected static String getDocumentEntryPatientId(AdhocQueryRequest request) {

        for (SlotType1 sl : request.getAdhocQuery().getSlot()) {
            if (sl.getName().equals("$XDSDocumentEntryPatientId")) {
                String patientId = sl.getValueList().getValue().get(0);
                patientId = patientId.substring(1, patientId.length() - 1);
                return patientId;
            }
        }
        return "$XDSDocumentEntryPatientId Not Found!";
    }

    protected static ExternalIdentifierType makeExternalIdentifier(String identificationScheme, String registryObject,
                                                                   String value, String name) {

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
