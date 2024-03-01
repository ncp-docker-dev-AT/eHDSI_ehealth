package epsos.ccd.gnomon.auditmanager.auditmessagebuilders;

import com.google.common.io.Resources;
import epsos.ccd.gnomon.auditmanager.*;
import eu.epsos.validation.datamodel.common.NcpSide;
import net.RFC3881.AuditMessage;
import org.apache.commons.io.IOUtils;
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
import java.util.Collections;
import java.util.Date;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class SMPAuditMessageBuilderTest extends XMLTestCase {

    @Test
    public void testBuild() throws Exception {

        EventLog eventLog = new EventLog();
        eventLog.setEventType(EventType.SMP_QUERY);
        eventLog.setNcpSide(NcpSide.NCP_A);
        eventLog.setEI_TransactionName(TransactionName.SMP_QUERY);
        eventLog.setEI_EventActionCode(EventActionCode.EXECUTE);
        eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        XMLGregorianCalendar now = DateUtil.getDateAsXMLGregorian(new Date());
        eventLog.setEI_EventDateTime(now);
        eventLog.setSC_UserID("ncp-ppt.pt.ehealth.testa.eu");
        eventLog.setSP_UserID("smp-test.publisher.ehealth.testa.eu");
        eventLog.setAS_AuditSourceId("PT-1");
        eventLog.setSourceip("10.200.137.161");
        eventLog.setTargetip("smp-cert-auth-test.publisher.ehealth.testa.eu");
        eventLog.setEventTargetParticipantObjectIds(Collections.singletonList("aHR0cHM6Ly9zbXAtdGVzdC5wdWJsaXNoZXIuZWhlYWx0aC50ZXN0YS5ldS9laGVhbHRoLXBhcnRpY2lwYW50aWQtcW5zJTNBJTNBdXJuJTNBZWhlYWx0aCUzQWF0JTNBbmNwLWlkcA=="));
        eventLog.setEM_ParticipantObjectDetail("errorMessage".getBytes());
        SMPAuditMessageBuilder smpAuditMessageBuilder = new SMPAuditMessageBuilder();
        AuditMessage generatedAuditMessage = smpAuditMessageBuilder.build(eventLog);
        URL url = Resources.getResource("smpqueryauditmessage.xml");
        AuditMessage expectedAuditMessage = AuditTrailUtils.convertXMLToAuditObject(IOUtils.toInputStream(Resources.toString(url, StandardCharsets.UTF_8)));
        expectedAuditMessage.getEventIdentification().setEventDateTime(now);
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(AuditTrailUtils.convertAuditObjectToXML(expectedAuditMessage), AuditTrailUtils.convertAuditObjectToXML(generatedAuditMessage));
    }

    @Test
    public void testBuildIncludingError() throws Exception {

        EventLog eventLog = new EventLog();
        eventLog.setEventType(EventType.SMP_QUERY);
        eventLog.setNcpSide(NcpSide.NCP_A);
        eventLog.setEI_TransactionName(TransactionName.SMP_QUERY);
        eventLog.setEI_EventActionCode(EventActionCode.EXECUTE);
        eventLog.setEI_EventOutcomeIndicator(EventOutcomeIndicator.FULL_SUCCESS);
        XMLGregorianCalendar now = DateUtil.getDateAsXMLGregorian(new Date());
        eventLog.setEI_EventDateTime(now);
        eventLog.setSC_UserID("ncp-ppt.pt.ehealth.testa.eu");
        eventLog.setSP_UserID("smp-test.publisher.ehealth.testa.eu");
        eventLog.setAS_AuditSourceId("PT-1");
        eventLog.setSourceip("10.200.137.161");
        eventLog.setTargetip("smp-cert-auth-test.publisher.ehealth.testa.eu");
        eventLog.setEventTargetParticipantObjectIds(Collections.singletonList("aHR0cHM6Ly9zbXAtdGVzdC5wdWJsaXNoZXIuZWhlYWx0aC50ZXN0YS5ldS9laGVhbHRoLXBhcnRpY2lwYW50aWQtcW5zJTNBJTNBdXJuJTNBZWhlYWx0aCUzQWF0JTNBbmNwLWlkcA=="));
        eventLog.setEM_ParticipantObjectID("aHR0cHM6Ly9zbXAtY2VydC1hdXRoLXRlc3QucHVibGlzaGVyLmVoZWFsdGgudGVzdGEuZXUvZWhlYWx0aC1wYXJ0aWNpcGFudGlkLXFuczo6dXJuOmVoZWFsdGg6cHQ6bmNwLWlkcC9zZXJ2aWNlcy9laGVhbHRoLXJlc2lkLXFuczo6dXJuOmVoZWFsdGg6cGF0aWVudGlkZW50aWZpY2F0aW9uYW5kYXV0aGVudGljYXRpb246OnhjcGQ6OmNyb3NzZ2F0ZXdheXBhdGllbnRkaXNjb3ZlcnklMjMlMjNpdGktNTU=");
        eventLog.setEM_ParticipantObjectDetail("errorMessage".getBytes());
        SMPAuditMessageBuilder smpAuditMessageBuilder = new SMPAuditMessageBuilder();
        AuditMessage generatedAuditMessage = smpAuditMessageBuilder.build(eventLog);
        URL url = Resources.getResource("smpqueryauditmessagewitherror.xml");
        AuditMessage expectedAuditMessage = AuditTrailUtils.convertXMLToAuditObject(IOUtils.toInputStream(Resources.toString(url, StandardCharsets.UTF_8)));
        expectedAuditMessage.getEventIdentification().setEventDateTime(now);
        XMLUnit.setIgnoreWhitespace(true);
        System.out.println(AuditTrailUtils.convertAuditObjectToXML(generatedAuditMessage));
        assertXMLEqual(AuditTrailUtils.convertAuditObjectToXML(expectedAuditMessage), AuditTrailUtils.convertAuditObjectToXML(generatedAuditMessage));
    }
}
