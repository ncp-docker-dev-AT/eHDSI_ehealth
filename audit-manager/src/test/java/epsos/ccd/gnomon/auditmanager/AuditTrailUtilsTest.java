package epsos.ccd.gnomon.auditmanager;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditService;
import eu.europa.ec.sante.ehdsi.openncp.audit.AuditServiceFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author karkaletsis
 */
public class AuditTrailUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditTrailUtilsTest.class);

    public AuditTrailUtilsTest() {
    }

    /**
     * Initialize Hibernate Configuration Manager file.
     */
    @BeforeClass
    public static void setUpClass() {
        //  HibernateConfigFile.name = "src/test/resources/configmanager.hibernate.xml";
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public void writeXMLToFile(String am, String filename) {

        try {
            // Create file
            FileWriter fstream = new FileWriter("/home/karkaletsis/Documents/projects/epsos/" + filename);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(am);
            // Close the output stream
            out.close();
        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage(), e);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosPACService() {

        LOGGER.info("[TEST] createAuditMessage for PAC");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }
        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(TransactionName.PAC_RETRIEVE, EventActionCode.QUERY,
                date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "AT",
                "dentist", "Vienna", "USER", "AS-12", "aaa",
                "aaa", new byte[1], "aaa", "aaa", new byte[1],
                "aaa", new byte[1], "1.2.3.4", "1.2.3.4");

        eventLog1.setEventType(EventType.PAC_RETRIEVE);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosConsentServicePin() {

        LOGGER.info("[TEST] createAuditMessage for Consent Service PIN");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogConsentPINdny(TransactionName.CONSENT_SERVICE_PIN,
                EventActionCode.READ, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "Massi",
                "doctor", "MassimilianoMasi<saml:massi@saml:test.fr>", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "AS-12", "22", "11", new byte[1], "22",
                new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.CONSENT_SERVICE_PIN);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosIdentificationServiceFindIdentityByTraits() {

        LOGGER.info("[TEST] createAuditMessage XCPD");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogPatientMapping(TransactionName.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS,
                EventActionCode.EXECUTE, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "dentist", "Massimiliano Masi", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "AS-12", "AbCD^^122333443",
                "0", null, new byte[0], "aa", "aa",
                new byte[1], "aa", new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.IDENTIFICATION_SERVICE_FIND_IDENTITY_BY_TRAITS);
        asd.write(eventLog1, "13", "2");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosPatientService() {

        LOGGER.info("[TEST] createAuditMessage for patient list");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }
        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(TransactionName.PATIENT_SERVICE_LIST, EventActionCode.QUERY,
                date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "AT",
                "dentist", "Vienna", "USER", "AS-12", "aaa",
                "aaa", new byte[1], "aaa", "aaa", new byte[1],
                "aaa", new byte[1], "1.2.3.4", "1.2.3.4");

        eventLog1.setEventType(EventType.PATIENT_SERVICE_LIST);
        asd.write(eventLog1, "13", "1");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosOrderService() {

        LOGGER.info("[TEST] createAuditMessage for Order Service");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(TransactionName.ORDER_SERVICE_LIST, EventActionCode.READ,
                date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "dentist", "MassimilianoMasi<saml:massi@saml:test.fr>", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "AS-12", "22", "333", new byte[1], "patienttarget^^^",
                "11", new byte[1], "22", new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.ORDER_SERVICE_LIST);
        asd.write(eventLog1, "13", "2");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosDispensationServiceInit() {

        LOGGER.info("[TEST] createAuditMessage for Dispensation Service Initialize");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: ", ex);
        }

        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(TransactionName.DISPENSATION_SERVICE_INITIALIZE,
                EventActionCode.UPDATE, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "Massimiliano",
                "dentist", "MassimilianoMasi<saml:massi@saml:test.fr>", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "AS-13", "22", "333", new byte[1], "patienttarget^^^",
                "11", new byte[1], "22", new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.DISPENSATION_SERVICE_INITIALIZE);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosDispensationServiceDiscard() {

        LOGGER.info("[TEST] createAuditMessage for Dispensation Service Discard");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(TransactionName.DISPENSATION_SERVICE_DISCARD,
                EventActionCode.DELETE, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "Massi",
                "dentist", "MassimilianoMasi<saml:massi@saml:test.fr>", "SMassimilianoMasi<saml:massi@saml:test.fr>",
                "AS-12", "22", "333", new byte[1],
                "patienttarget^^^", "11", new byte[1], "22",
                new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.DISPENSATION_SERVICE_DISCARD);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosConsentServicePut() {

        LOGGER.info("[TEST] createAuditMessage for Consent Service Put");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(TransactionName.CONSENT_SERVICE_PUT,
                EventActionCode.UPDATE, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "Massi",
                "dentist", "MassimilianoMasi<saml:massi@saml:test.fr>", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "AS-12", "22", "333", new byte[1], "patienttarget^^^",
                "11", new byte[1], "22", new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.CONSENT_SERVICE_PUT);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosConsentServiceDiscard() {

        LOGGER.info("[TEST] createAuditMessage for Consent Service Discard");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogHCPAssurance(

                TransactionName.CONSENT_SERVICE_DISCARD, EventActionCode.DELETE, date2, EventOutcomeIndicator.FULL_SUCCESS,
                "MassimilianoMasi<saml:massi@saml:test.fr>", "Hospital", "Massimiliano Masi",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "dentist", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "AS-12", "22", "333",
                new byte[1], "patienttarget^^^", "11", new byte[1], "22",
                new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.CONSENT_SERVICE_DISCARD);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosHCPIdentity() {

        LOGGER.info("[TEST] createAuditMessage for HCP Identity");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogHCPIdentity(TransactionName.HCP_AUTHENTICATION,
                EventActionCode.EXECUTE, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "Hospital", "MassimilianoMasi<saml:massi@saml:test.fr>", "dentist",
                "dentdsdsdsist", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "AS-12", "ssasa",
                "aa", new byte[1], "aaa", new byte[1], "AA", "AA", NcpSide.NCP_B);
        eventLog1.setEventType(EventType.HCP_AUTHENTICATION);

        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosTRCA() {

        LOGGER.info("[TEST] createAuditMessage for TRCA");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogTRCA(TransactionName.TRC_ASSERTION, EventActionCode.EXECUTE, date2,
                EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>", "Hospital",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "dentist", "massi",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "MassimilianoMasi<saml:massi@saml:test.fr>",
                "AS-12", "PS_PatricipantObjectID", "ET_ObjectID^^^",
                "11", new byte[1], "22", new byte[1], "194.219.31.2",
                "222.33.33.3", NcpSide.NCP_B);

        eventLog1.setEventType(EventType.TRC_ASSERTION);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosNCPTrustedServiceList() {

        LOGGER.info("[TEST] createAuditMessage for NCPTrustedServiceList");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogNCPTrustedServiceList(TransactionName.NCP_TRUSTED_SERVICE_LIST,
                EventActionCode.EXECUTE, date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "MassimilianoMasi<saml:massi@saml:test.fr>", "ET_ObjectID^^^", "11",
                new byte[1], "22", new byte[1], "194.219.31.2", "222.33.33.3");

        eventLog1.setEventType(EventType.NCP_TRUSTED_SERVICE_LIST);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }

    @Test
    public void testCreateAuditMessage_epsosPivotTranslation() {

        LOGGER.info("[TEST] createAuditMessage for PivotTranslation");
        AuditService asd = AuditServiceFactory.getInstance();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("DatatypeConfigurationException: '{}'", ex.getMessage(), ex);
        }

        EventLog eventLog1 = EventLog.createEventLogPivotTranslation(TransactionName.PIVOT_TRANSLATION, EventActionCode.EXECUTE,
                date2, EventOutcomeIndicator.FULL_SUCCESS, "MassimilianoMasi<saml:massi@saml:test.fr>",
                "ET_ObjectID^^^", "22", "11", new byte[1], "22",
                new byte[1], "194.219.31.2");
        eventLog1.setEventType(EventType.PIVOT_TRANSLATION);
        asd.write(eventLog1, "13", "2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.error(null, ex);
        }
    }
}
