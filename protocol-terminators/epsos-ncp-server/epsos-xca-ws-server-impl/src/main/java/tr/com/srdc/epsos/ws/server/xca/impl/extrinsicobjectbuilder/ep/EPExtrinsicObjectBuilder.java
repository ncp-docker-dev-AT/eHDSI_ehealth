package tr.com.srdc.epsos.ws.server.xca.impl.extrinsicobjectbuilder.ep;

import eu.epsos.util.IheConstants;
import eu.epsos.util.xca.XCAConstants;
import eu.europa.ec.sante.ehdsi.constant.codesystem.CodeSystem;
import fi.kela.se.epsos.data.model.EPDocumentMetaData;
import fi.kela.se.epsos.data.model.EPSOSDocumentMetaData;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ClassificationType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import org.apache.commons.lang.StringUtils;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.ws.server.xca.impl.ClassificationBuilder;
import tr.com.srdc.epsos.ws.server.xca.impl.SlotBuilder;
import tr.com.srdc.epsos.ws.server.xca.impl.extrinsicobjectbuilder.AbstractExtrinsicObjectBuilder;

import java.util.UUID;

public class EPExtrinsicObjectBuilder extends AbstractExtrinsicObjectBuilder {

    public static String build(AdhocQueryRequest request, ExtrinsicObjectType eot, EPDocumentMetaData documentMetaData) {

        var ofRim = new oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory();

        String uuid = Constants.UUID_PREFIX + UUID.randomUUID();
        boolean isPDF = documentMetaData.getFormat() == EPSOSDocumentMetaData.EPSOSDOCUMENT_FORMAT_PDF;

        var title = "eHDSI - ePrescription";
        build(request, eot, documentMetaData, ofRim, uuid, title);

        // Description
        eot.setDescription(ofRim.createInternationalStringType());
        eot.getDescription().getLocalizedString().add(ofRim.createLocalizedStringType());
        eot.getDescription().getLocalizedString().get(0).setValue(documentMetaData.getDescription());

        // Dispensable
        if (documentMetaData.isDispensable()) {
            ClassificationType dispensableClassification = ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, "urn:ihe:iti:xdw:2011:eventCode:open", "1.3.6.1.4.1.19376.1.2.3", "Open");
            eot.getClassification().add(dispensableClassification);
        } else {
            ClassificationType dispensableClassification = ClassificationBuilder.build("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
                    uuid, "urn:ihe:iti:xdw:2011:eventCode:closed", "1.3.6.1.4.1.19376.1.2.3", "Closed");
            eot.getClassification().add(dispensableClassification);
        }

        // ATC code (former Product element)
        if(StringUtils.isNotBlank(documentMetaData.getAtcCode())) {
            ClassificationType atcCodeClassification = ClassificationBuilder.build(
                    "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                    documentMetaData.getAtcCode(), CodeSystem.ATC.getOID(), documentMetaData.getAtcName());
            eot.getClassification().add(atcCodeClassification);
        }

        // Dose Form Code
        if(StringUtils.isNotBlank(documentMetaData.getDoseFormCode())) {
            ClassificationType doseFormClassification = ClassificationBuilder.build(
                    "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                    documentMetaData.getDoseFormCode(), "0.4.0.127.0.16.1.1.2.1", documentMetaData.getDoseFormName());
            eot.getClassification().add(doseFormClassification);
        }

        // Strength
        if(StringUtils.isNotBlank(documentMetaData.getStrength())) {
            ClassificationType strengthClassification = ClassificationBuilder.build(
                    "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                    documentMetaData.getStrength(), "eHDSI_Strength_CodeSystem", "Strength of medication");
            eot.getClassification().add(strengthClassification);
        }

        // Substitution
        EPDocumentMetaData.SubstitutionMetaData substitutionMetaData = documentMetaData.getSubstitution();
        if (substitutionMetaData != null) {
            ClassificationType substitutionClassification = ClassificationBuilder.build(
                    "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4", uuid,
                    substitutionMetaData.getSubstitutionCode(), "2.16.840.1.113883.5.1070", substitutionMetaData.getSubstitutionDisplayName());
            eot.getClassification().add(substitutionClassification);
        }

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

        // Author Person
        ClassificationType authorClassification = ClassificationBuilder.build(
                IheConstants.CLASSIFICATION_SCHEME_AUTHOR_UUID, uuid, "");
        authorClassification.getSlot().add(SlotBuilder.build(IheConstants.AUTHOR_PERSON_STR, documentMetaData.getAuthor()));
        eot.getClassification().add(authorClassification);

        return uuid;
    }
}
