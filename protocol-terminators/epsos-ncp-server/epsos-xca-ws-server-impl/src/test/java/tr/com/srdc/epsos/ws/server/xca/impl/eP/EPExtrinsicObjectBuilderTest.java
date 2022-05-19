package tr.com.srdc.epsos.ws.server.xca.impl.eP;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.domain.Property;
import fi.kela.se.epsos.data.model.*;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tr.com.srdc.epsos.data.model.SubstitutionCodeEnum;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.ws.server.xca.impl.SlotBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.util.Date;


public class EPExtrinsicObjectBuilderTest {

    public static final String ATC_CODE_SYSTEM_OID = "2.16.840.1.113883.6.73";
    public static final String EDQM_CODE_SYSTEM_OID = "0.4.0.127.0.16.1.1.2.1";

    public static final ObjectFactory OBJECT_FACTORY_RIM = new ObjectFactory();
    public static final oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory OBJECT_FACTORY_QUERY = new oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory();

    @BeforeClass
    public static void beforeTest() {
        Configuration CONFIGURATION = new Configuration()
            .addAnnotatedClass(Property.class)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.current_session_context_class", "thread")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "create");
        SessionFactory sessionFactory = CONFIGURATION.buildSessionFactory();
        ConfigurationManagerFactory.setSessionFactory(sessionFactory);

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        session.persist(createProperty("SERVER_IP", "127.0.0.1"));
        session.persist(createProperty("HOME_COMM_ID", "1.3.6.1.4.1.48336"));
        session.persist(createProperty("COUNTRY_CODE", "BE"));
        session.persist(createProperty("COUNTRY_NAME", "Belgium"));
        session.persist(createProperty("COUNTRY_PRINCIPAL_SUBDIVISION", "BE-1"));
        session.persist(createProperty("LANGUAGE_CODE", "nl-BE"));
        session.persist(createProperty("TRUSTSTORE_PATH", "/opt/openncp-configuration/keystore/eu-truststore.jks"));
        session.persist(createProperty("TRUSTSTORE_PASSWORD", "changeit"));
        session.persist(createProperty("SP_KEYSTORE_PATH", "/opt/openncp-configuration/keystore/gazelle-service-provider-keystore.jks"));
        session.persist(createProperty("SP_KEYSTORE_PASSWORD", "gazelle"));
        session.persist(createProperty("SP_PRIVATEKEY_ALIAS", "gazelle.ncp-sp.openncp.dg-sante.eu"));
        session.persist(createProperty("SP_PRIVATEKEY_PASSWORD", "gazelle"));
        session.persist(createProperty("SC_KEYSTORE_PATH", "/opt/openncp-configuration/keystore/gazelle-service-consumer-keystore.jks"));
        session.persist(createProperty("SC_KEYSTORE_PASSWORD", "gazelle"));
        session.persist(createProperty("SC_PRIVATEKEY_ALIAS", "gazelle.ncp-sc.openncp.dg-sante.eu"));
        session.persist(createProperty("SC_PRIVATEKEY_PASSWORD", "gazelle"));
        session.persist(createProperty("NCP_SIG_KEYSTORE_PATH", "/opt/openncp-configuration/keystore/gazelle-signature-keystore.jks"));
        session.persist(createProperty("NCP_SIG_KEYSTORE_PASSWORD", "gazelle"));
        session.persist(createProperty("NCP_SIG_PRIVATEKEY_ALIAS", "gazelle.ncp-signature.openncp.dg-sante.eu"));
        session.persist(createProperty("NCP_SIG_PRIVATEKEY_PASSWORD", "gazelle"));

        session.getTransaction().commit();
    }

    @Test
    public void testATCNormalFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var strength = "100 U/ml";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, doseFormCode, doseFormName, strength, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
            classificationType.getNodeRepresentation().equals(atcCode) &&
            classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals(ATC_CODE_SYSTEM_OID) &&
            classificationType.getName().getLocalizedString().iterator().next().getValue().equals(atcName)) {
                found = true;
                var je =  OBJECT_FACTORY_RIM.createClassification(classificationType);
                System.out.println(toXml(je));
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testATCNullFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var strength = "100 U/ml";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(null, null, doseFormCode, doseFormName, strength, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals(ATC_CODE_SYSTEM_OID)) {
                found = true;
            }
        }
        Assert.assertFalse(found);
    }

    @Test
    public void testDoseFormNormalFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var strength = "100 U/ml";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, doseFormCode, doseFormName, strength, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getNodeRepresentation().equals(doseFormCode) &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals(EDQM_CODE_SYSTEM_OID) &&
                    classificationType.getName().getLocalizedString().iterator().next().getValue().equals(doseFormName)) {
                found = true;
                var je =  OBJECT_FACTORY_RIM.createClassification(classificationType);
                System.out.println(toXml(je));
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testDoseFormNullFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var strength = "100 U/ml";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, null, null, strength, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals(EDQM_CODE_SYSTEM_OID)) {
                found = true;
            }
        }
        Assert.assertFalse(found);
    }

    @Test
    public void testStrengthNormalFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var strength = "100 U/ml";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, doseFormCode, doseFormName, strength, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getNodeRepresentation().equals(strength) &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals("eHDSI_Strength_CodeSystem")) {
                found = true;
                JAXBElement<ClassificationType> je =  OBJECT_FACTORY_RIM.createClassification(classificationType);
                System.out.println(toXml(je));
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testStrengthNullFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, doseFormCode, doseFormName, null, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals("eHDSI_Strength_CodeSystem")) {
                found = true;
            }
        }
        Assert.assertFalse(found);
    }

    @Test
    public void testSubstitutionCodeNormalFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var strength = "100 U/ml";
        var substitutionCode = SubstitutionCodeEnum.G;
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, doseFormCode, doseFormName, strength, substitutionCode);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getNodeRepresentation().equals(substitutionCode.name()) &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals("2.16.840.1.113883.5.1070") &&
                    classificationType.getName().getLocalizedString().iterator().next().getValue().equals(substitutionCode.getDisplayName())) {
                found = true;
                JAXBElement<ClassificationType> je =  OBJECT_FACTORY_RIM.createClassification(classificationType);
                System.out.println(toXml(je));
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testSubstitutionCodeNullFlow() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        var eotXML = OBJECT_FACTORY_RIM.createExtrinsicObjectType();
        var atcCode = "A10AE04";
        var atcName = "insulin glargine";
        var doseFormCode = "10219000";
        var doseFormName = "Tablet";
        var strength = "100 U/ml";
        var epDocumentMetaData = buildEPDocumentMetaData(atcCode, atcName, doseFormCode, doseFormName, strength, null);

        EPExtrinsicObjectBuilder.build(adHocQueryRequest, eotXML, epDocumentMetaData);
        var extrinsicObject = OBJECT_FACTORY_RIM.createExtrinsicObject(eotXML);
        boolean found = false;
        for (ClassificationType classificationType: extrinsicObject.getValue().getClassification()) {
            if (classificationType.getClassificationScheme().equals("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4") &&
                    classificationType.getNodeRepresentation().equals(SubstitutionCodeEnum.G.name()) &&
                    classificationType.getSlot().iterator().next().getValueList().getValue().iterator().next().equals("2.16.840.1.113883.5.1070") &&
                    classificationType.getName().getLocalizedString().iterator().next().getValue().equals(SubstitutionCodeEnum.G.getDisplayName())) {
                found = true;
            }
        }
        Assert.assertFalse(found);
    }

    private EPDocumentMetaData buildEPDocumentMetaData(String atcCode,
                                                       String atcName,
                                                       String doseFormCode,
                                                       String doseFormName,
                                                       String strength,
                                                       SubstitutionCodeEnum substitutionCode) {
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
                atcCode,
                atcName,
                doseFormCode,
                doseFormName,
                strength,
                substitutionCode == null ? null : new EPDocumentMetaDataImpl.SimpleSubstitutionMetadata(substitutionCode));
        return new EPDocumentMetaDataImpl(epsosDocumentMetaData, "description", epListParam);
    }

    private String toXml(JAXBElement element) {
        try {
            JAXBContext jc = JAXBContext.newInstance(element.getValue().getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(element, baos);
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static Property createProperty(String key, String value) {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }
}
