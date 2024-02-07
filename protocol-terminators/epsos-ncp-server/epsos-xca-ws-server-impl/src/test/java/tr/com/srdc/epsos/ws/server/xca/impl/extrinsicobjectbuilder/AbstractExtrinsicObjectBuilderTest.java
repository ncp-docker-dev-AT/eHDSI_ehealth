package tr.com.srdc.epsos.ws.server.xca.impl.extrinsicobjectbuilder;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.domain.Property;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.AdhocQueryType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.BeforeClass;
import tr.com.srdc.epsos.ws.server.xca.impl.SlotBuilder;

public class AbstractExtrinsicObjectBuilderTest {

    protected static final ObjectFactory OBJECT_FACTORY_RIM = new ObjectFactory();
    protected static final oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory OBJECT_FACTORY_QUERY = new oasis.names.tc.ebxml_regrep.xsd.query._3.ObjectFactory();

    @BeforeClass
    public static void beforeTest() {
        final String EPSOS_PROP_KEY = "EPSOS_PROPS_PATH";
        final String epsosPropsPath = System.getenv(EPSOS_PROP_KEY);
        if (epsosPropsPath == null) {
            System.setProperty(EPSOS_PROP_KEY, "/opt/test");
        }

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
        session.persist(createProperty("ABUSE_UNIQUE_PATIENT_REQUEST_THRESHOLD", "200"));
        session.persist(createProperty("ABUSE_SCHEDULER_ENABLE", "false"));
        session.persist(createProperty("ABUSE_SCHEDULER_TIME_INTERVAL", "200"));
        session.persist(createProperty("ABUSE_ALL_REQUEST_REFERENCE_REQUEST_PERIOD", "200"));
        session.persist(createProperty("ABUSE_ALL_REQUEST_THRESHOLD", "200"));
        session.persist(createProperty("ABUSE_UNIQUE_POC_REFERENCE_REQUEST_PERIOD", "200"));
        session.persist(createProperty("ABUSE_UNIQUE_POC_REQUEST_THRESHOLD", "200"));
        session.persist(createProperty("ABUSE_UNIQUE_PATIENT_REFERENCE_REQUEST_PERIOD", "200"));

        session.getTransaction().commit();
    }

    private static Property createProperty(String key, String value) {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    protected AdhocQueryRequest buildAdhocQueryRequest() {
        var adHocQueryRequest = OBJECT_FACTORY_QUERY.createAdhocQueryRequest();
        var patientIdSlot = SlotBuilder.build("$XDSDocumentEntryPatientId", "123");
        var adhocQueryType = new AdhocQueryType();
        adhocQueryType.getSlot().add(patientIdSlot);
        adHocQueryRequest.setAdhocQuery(adhocQueryType);
        return adHocQueryRequest;
    }
}
