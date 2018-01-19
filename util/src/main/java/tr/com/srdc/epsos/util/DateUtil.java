package tr.com.srdc.epsos.util;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class DateUtil {

    public static final String TIME_DATE_FORMAT = "yyyyMMddHHmmss.SSSZZZZ";
    public static final String DATE_FORMAT = "yyyyMMdd";
    private static final int RAND_LIMIT = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);
    private static Random rand = new Random();

    private DateUtil() {
    }

    // Returns the current time in the local time zone
    public static String getCurrentTimeLocal() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String currentTime;
        dateFormat.setTimeZone(TimeZone.getDefault());
        currentTime = dateFormat.format(new Date());
        return currentTime;
    }

    // Returns the current time in the GMT
    public static String getCurrentTimeGMT() {

        String currentTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        currentTime = dateFormat.format(new Date());
        return currentTime;
    }

    // Returns the current time in given format
    public static String getDateByDateFormat(String dateFormatString) {

        String currentTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        currentTime = dateFormat.format(new Date());
        return currentTime;
    }

    // Returns the given date in given format
    public static String getDateByDateFormat(String dateFormatString, Date date) {

        String currentTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        currentTime = dateFormat.format(date);
        return currentTime;
    }

    // uses local time zone
    public static String generateUniqueIdExtension() {

        String uniqueIdExt = DateUtil.getCurrentTimeLocal();
        uniqueIdExt += "." + rand.nextInt(RAND_LIMIT);
        return uniqueIdExt;
    }

    public static Date parseDateFromString(String date, String pattern) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(date);
    }

    public static DateTime gregorianCalendarToJodaTime(XMLGregorianCalendar cal) {

        DateTime dt = new DateTime(cal.toGregorianCalendar().getTime());
        LOGGER.info("Date: '{}'", dt);
        return dt;
    }
}
