package tr.com.srdc.epsos.ws.server.xca.impl.eP;

import fi.kela.se.epsos.data.model.*;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory;
import org.junit.Test;
import tr.com.srdc.epsos.data.model.SubstitutionCodeEnum;
import tr.com.srdc.epsos.util.Constants;

import java.util.Date;


public class EPExtrinsicObjectBuilderTest {

    @Test
    public void test() {
        var objectFactoryRim = new ObjectFactory();
        var objectFactoryQuery = new oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory();
        var adHocQueryRequest = objectFactoryQuery.createAdhocQueryRequest();
        var eotXML = objectFactoryRim.createExtrinsicObjectType();
        var epDocumentMetaData = buildEPDocumentMetaData();

        final String uuid = EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
    }

    private EPDocumentMetaData buildEPDocumentMetaData() {
        var epsosDocumentMetaData = new EPSOSDocumentMetaDataImpl(
                "id",
                "patientid",
                1,
                new Date(),
                Constants.EP_CLASSCODE,
                "repositoryId",
                "ePrescription test",
                "author",
                new EPSOSDocumentMetaDataImpl.SimpleConfidentialityMetadata("N", "normal"),
                "en-EN");
        var epListParam = new EpListParam(
                true,
                "ATC_CODE",
                "ATC NAME",
                "DOSE_FORM_CODE",
                "DOSE_FORM_NAME",
                "STRENGTH",
                new EPDocumentMetaDataImpl.SimpleSubstitutionMetadata(SubstitutionCodeEnum.G));
        var epDocumentMetaData = new EPDocumentMetaDataImpl(epsosDocumentMetaData, "description", epListParam);
        return epDocumentMetaData;
    }
}
