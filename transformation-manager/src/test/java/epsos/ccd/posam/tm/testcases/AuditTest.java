package epsos.ccd.posam.tm.testcases;

import epsos.ccd.gnomon.auditmanager.*;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TM Junit test suite
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.2, 2010, 20 October
 */
public class AuditTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditTest.class);

    public void testAudit() {
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());

            EventLog logg = EventLog.createEventLogPivotTranslation(
                    TransactionName.epsosPivotTranslation, EventActionCode.EXECUTE,
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar),
                    EventOutcomeIndicator.FULL_SUCCESS,
                    "", "", "", "",
                    new byte[0], "",
                    new byte[0], "");
            logg.setEventType(EventType.epsosPivotTranslation);
            //AuditMessage am = AuditTrailUtils.getInstance().createAuditMessage(logg);
            boolean result = new AuditService().write(logg, "testfacility", "testseverity");
            assertTrue(result);
        } catch (DatatypeConfigurationException e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
    }
}
