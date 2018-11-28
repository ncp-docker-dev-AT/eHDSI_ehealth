package se.sb.epsos.web.util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

public class TestDateUtil {

    @Test
    public void testFormatDateToStringWithNewFormat() {
        String string = DateUtil.formatDate(new Date(), "yyyyMMdd");
        assertNotNull(string);
    }

    @Test
    public void testFormatDateToString() {
        String string = DateUtil.formatDate(new Date());
        assertNotNull(string);
    }
}
