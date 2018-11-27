package se.sb.epsos.web.util;

import junit.framework.TestCase;

public class TestNullFlavorManager extends TestCase {

    public void testGetNullFlavorValue() {
        String test = NullFlavorManager.getNullFlavor("NI");
        assertNotNull(test);
        assertEquals(test, "NoInformation");
    }
}
