package se.sb.epsos.web.integrationtest.webtests;

import org.junit.Test;
import se.sb.epsos.web.integrationtest.EpsosWebIntegrationTestBase;

/**
 * The purpose of this class is to get the test suite working with Internet Explorer. For some reason, the
 * submit button on the login page is not clickable the first 3 times when running the suite. This test takes care
 * of these 3 and the rest of the test cases will be able to run. This is kinda ugly but the Internet Explorer driver
 * is not as stable as one could hope.
 * <p>
 * See:See: http://code.google.com/p/selenium/issues/detail?id=2864
 *
 * @author Daniel Grönberg
 */
public class IEHack extends EpsosWebIntegrationTestBase {

    @Test
    public void loginAsDoctorFirstTime() throws Exception {
        enterLoginCredentials("doktor", "1234");
    }

    @Test
    public void loginAsDoctorSecondTime() throws Exception {
        enterLoginCredentials("doktor", "1234");
    }

    @Test
    public void loginAsDoctorThirdTime() throws Exception {
        enterLoginCredentials("doktor", "1234");
    }
}
