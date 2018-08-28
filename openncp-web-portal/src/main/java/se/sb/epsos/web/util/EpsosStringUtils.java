package se.sb.epsos.web.util;

/**
 * @author andreas
 */
public class EpsosStringUtils {

    private EpsosStringUtils() {
    }

    public static boolean nullSafeCompare(String s1, String s2) {

        boolean test = false;
        if (s1 != null && s2 != null) {
            test = s1.equals(s2);
        } else if (s1 == null && s2 == null) {
            test = true;
        }
        return test;
    }
}
