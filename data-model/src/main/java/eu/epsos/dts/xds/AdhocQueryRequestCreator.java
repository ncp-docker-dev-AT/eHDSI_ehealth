package eu.epsos.dts.xds;

import eu.epsos.util.xca.XCAConstants;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.ResponseOptionType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.AdhocQueryType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ValueListType;
import org.apache.commons.lang3.StringUtils;
import tr.com.srdc.epsos.data.model.FilterParams;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;

import java.util.List;

public class AdhocQueryRequestCreator {

    /**
     *
     */
    private AdhocQueryRequestCreator() {
    }

    /**
     * @param extension
     * @param root
     * @param documentCodes
     * @return
     */
    public static AdhocQueryRequest createAdhocQueryRequest(String extension, String root,
                                                            List<GenericDocumentCode> documentCodes, FilterParams filterParams) {


        AdhocQueryRequest adhocQueryRequest = new AdhocQueryRequest();

        // Set AdhocQueryRequest/ResponseOption
        ResponseOptionType rot = new ResponseOptionType();
        rot.setReturnComposedObjects(true);
        rot.setReturnType(XCAConstants.AdHocQueryRequest.RESPONSE_OPTIONS_RETURN_TYPE);
        adhocQueryRequest.setResponseOption(rot);

        // Create AdhocQueryRequest
        adhocQueryRequest.setAdhocQuery(new AdhocQueryType());
        adhocQueryRequest.getAdhocQuery().setId(XCAConstants.AdHocQueryRequest.ID);

        // Set XDSDocumentEntryPatientId Slot
        SlotType1 patientId = new SlotType1();
        patientId.setName(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_PATIENTID_SLOT_NAME);
        ValueListType v1 = new ValueListType();
        v1.getValue().add("'" + extension + "^^^&" + root + "&" + "ISO'");
        patientId.setValueList(v1);
        adhocQueryRequest.getAdhocQuery().getSlot().add(patientId);

        // Set XDSDocumentEntryStatus Slot
        SlotType1 entryStatus = new SlotType1();
        entryStatus.setName(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_STATUS_SLOT_NAME);
        ValueListType v2 = new ValueListType();
        v2.getValue().add(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_STATUS_SLOT_VALUE);
        entryStatus.setValueList(v2);
        adhocQueryRequest.getAdhocQuery().getSlot().add(entryStatus);

        // Set XDSDocumentEntryClassCode Slot
        SlotType1 entryClassCode = new SlotType1();
        entryClassCode.setName(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_CLASSCODE_SLOT_NAME);
        ValueListType v3 = new ValueListType();
        String documentEntryClassCode = "('";
        for(GenericDocumentCode documentCode : documentCodes) {

            //v3.getValue().add("('" + documentCode.getValue() + "^^" + documentCode.getSchema() + "')");
            if(StringUtils.length(documentEntryClassCode) > 2) {
                documentEntryClassCode += ",";
            }
            documentEntryClassCode += documentCode.getValue() + "^^" + documentCode.getSchema();
        }
        documentEntryClassCode += "')";

        v3.getValue().add(documentEntryClassCode);
        entryClassCode.setValueList(v3);
        adhocQueryRequest.getAdhocQuery().getSlot().add(entryClassCode);


        //FilterParameters
        // Set XDSDocumentEntryFilterMaximumSize  Slot
        if(filterParams.getMaximumSize() != null) {
            SlotType1 entryFilterMaximumSize = new SlotType1();
            entryFilterMaximumSize.setName(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_FILTERMAXIMUMSIZE_SLOT_NAME);
            ValueListType v4 = new ValueListType();
            v4.getValue().add(String.valueOf(filterParams.getMaximumSize()));
            entryFilterMaximumSize.setValueList(v4);
            adhocQueryRequest.getAdhocQuery().getSlot().add(entryFilterMaximumSize);
        }

        // Set XDSDocumentEntryFilterMaximumSize  Slot
        if(filterParams.getCreatedBefore() != null) {
            SlotType1 entryFilterCreatedBefore = new SlotType1();
            entryFilterCreatedBefore.setName(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_FILTERCREATEDBEFORE_SLOT_NAME);
            ValueListType v5 = new ValueListType();
            v5.getValue().add(String.valueOf(filterParams.getMaximumSize()));
            entryFilterCreatedBefore.setValueList(v5);
            adhocQueryRequest.getAdhocQuery().getSlot().add(entryFilterCreatedBefore);
        }

        // Set XDSDocumentEntryFilterMaximumSize  Slot
        if(filterParams.getCreatedAfter() != null) {
            SlotType1 entryFilterCreatedAfter = new SlotType1();
            entryFilterCreatedAfter.setName(XCAConstants.AdHocQueryRequest.XDS_DOCUMENT_ENTRY_FILTERCREATEDAFTER_SLOT_NAME);
            ValueListType v6 = new ValueListType();
            v6.getValue().add(String.valueOf(filterParams.getMaximumSize()));
            entryFilterCreatedAfter.setValueList(v6);
            adhocQueryRequest.getAdhocQuery().getSlot().add(entryFilterCreatedAfter);
        }


        return adhocQueryRequest;
    }
}
