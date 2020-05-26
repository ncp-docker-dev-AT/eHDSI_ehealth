package se.sb.epsos.web.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private DateUtil() {
    }

    public static String formatDate(Date date) {

        return formatDate(date, EpsosWebConstants.DATEFORMAT);
    }

    public static String formatDate(Date date, String format) {

        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Date formatStringToDate(String dateString) throws Exception {

        try {
            return parseDateString(dateString, EpsosWebConstants.DATEFORMAT);
        } catch (ParseException e) {
            return parseDateString(dateString, EpsosWebConstants.DATEFORMATSEC);
        }
    }

    private static Date parseDateString(String dateString, String format) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        return sdf.parse(dateString);
    }
}
