package eu.europa.ec.sante.ehdsi.openncp.gateway.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public final class DateTimeUtil {

    private DateTimeUtil() {
    }

    public static XMLGregorianCalendar timeUTC() throws DatatypeConfigurationException {

        return DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(
                        (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)));
    }
}
