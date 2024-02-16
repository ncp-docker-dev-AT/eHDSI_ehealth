package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import com.google.common.io.Resources;
import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import net.RFC3881.AuditMessage;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.util.DateUtil;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class SMPAuditMessageBuilderTest extends XMLTestCase {

    @Test
    public void testBuild() throws JAXBException, IOException, SAXException {

        EventLog eventLog = new EventLog();
        eventLog.setEventType(EventType.SMP_QUERY);
        eventLog.setNcpSide(NcpSide.NCP_A);
        eventLog.setEI_TransactionName(TransactionName.SMP_QUERY);
        eventLog.setEI_EventActionCode(EventActionCode.EXECUTE);
        eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        eventLog.setEI_EventDateTime(DateUtil.getDateAsXMLGregorian(new Date()));
        eventLog.setSC_UserID("ncp-ppt.pt.ehealth.testa.eu");
        eventLog.setSP_UserID("smp-test.publisher.ehealth.testa.eu");
        eventLog.setAS_AuditSourceId("PT-1");
        eventLog.setSourceip("10.200.137.161");
        eventLog.setTargetip("smp-cert-auth-test.publisher.ehealth.testa.eu");
        eventLog.setEM_ParticipantObjectDetail("errorMessage".getBytes());
        SMPAuditMessageBuilder smpAuditMessageBuilder = new SMPAuditMessageBuilder();
        AuditMessage auditMessage = smpAuditMessageBuilder.build(eventLog);
        String generatedAuditMessage = AuditTrailUtils.convertAuditObjectToXML(auditMessage);
        URL url = Resources.getResource("smpqueryauditmessage.xml");
        String expectedAuditMessage = Resources.toString(url, StandardCharsets.UTF_8);
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(expectedAuditMessage, generatedAuditMessage);
    }

    @Test
    public void testBuildIncludingError() throws JAXBException {

        EventLog eventLog = new EventLog();
        eventLog.setEventType(EventType.SMP_QUERY);
        eventLog.setNcpSide(NcpSide.NCP_A);
        eventLog.setEI_TransactionName(TransactionName.SMP_QUERY);
        eventLog.setEI_EventActionCode(EventActionCode.EXECUTE);
        eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        eventLog.setEI_EventDateTime(DateUtil.getDateAsXMLGregorian(new Date()));
        eventLog.setSC_UserID("ncp-ppt.pt.ehealth.testa.eu");
        eventLog.setSP_UserID("smp-test.publisher.ehealth.testa.eu");
        eventLog.setAS_AuditSourceId("PT-1");
        eventLog.setSourceip("10.200.137.161");
        eventLog.setTargetip("smp-cert-auth-test.publisher.ehealth.testa.eu");
        eventLog.setEM_ParticipantObjectID("aHR0cHM6Ly9zbXAtY2VydC1hdXRoLXRlc3QucHVibGlzaGVyLmVoZWFsdGgudGVzdGEuZXUvZWhlYWx0aC1wYXJ0aWNpcGFudGlkLXFuczo6dXJuOmVoZWFsdGg6cHQ6bmNwLWlkcC9zZXJ2aWNlcy9laGVhbHRoLXJlc2lkLXFuczo6dXJuOmVoZWFsdGg6cGF0aWVudGlkZW50aWZpY2F0aW9uYW5kYXV0aGVudGljYXRpb246OnhjcGQ6OmNyb3NzZ2F0ZXdheXBhdGllbnRkaXNjb3ZlcnklMjMlMjNpdGktNTU=");
        eventLog.setEM_ParticipantObjectDetail("errorMessage".getBytes());
        SMPAuditMessageBuilder smpAuditMessageBuilder = new SMPAuditMessageBuilder();
        AuditMessage auditMessage = smpAuditMessageBuilder.build(eventLog);
        String generatedAuditMessage = AuditTrailUtils.convertAuditObjectToXML(auditMessage);
        System.out.println(generatedAuditMessage);
    }
}
