package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public final class DateTimeUtil {

    private static final DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException();
        }
    }

    private DateTimeUtil() {
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, day, hour, min);
        return DATATYPE_FACTORY.newXMLGregorianCalendar(gregorianCalendar);
    }

    public static XMLGregorianCalendar timeUTC() {

        return DATATYPE_FACTORY.newXMLGregorianCalendar(new GregorianCalendar(TimeZone.getTimeZone(ZoneOffset.UTC)));
    }

    public static String formatTimeInMillis(long time) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSZ");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(time);
        return format.format(date);
    }
}
