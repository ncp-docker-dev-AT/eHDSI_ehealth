package se.sb.epsos.web.integrationtest.webtests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.integrationtest.EpsosWebIntegrationTestBase;

import static org.junit.Assert.assertTrue;

public class LoginTest extends EpsosWebIntegrationTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginTest.class);

    @Test
    public void testApplicationIsResponding() throws Exception {
        LOGGER.info("Starting test:::testApplicationIsResponding()");
        selenium.open("/");
        selenium.waitForPageToLoad("3000");
        assertTrue(selenium.isElementPresent("epsos"));
        assertTrue(selenium.isElementPresent("userinfo"));
        assertTrue(selenium.isElementPresent("loginForm"));
        assertTrue(selenium.isTextPresent(getProp("message.notloggedin")));
        LOGGER.info("Finished test:::testApplicationIsResponding()");
    }

    @Test
    public void testLoginSuccess_PHARMACIST() throws Exception {
        LOGGER.info("Starting test:::testLoginSuccess_PHARMACIST()");
        enterLoginCredentials("apotek", "1234");
        assertSuccessfulLogin("Apo Tekare");
        LOGGER.info("Finished test:::testLoginSuccess_PHARMACIST()");
    }

    @Test
    public void testLoginSuccess_DOCTOR() throws Exception {
        LOGGER.info("Starting test:::testLoginSuccess_DOCTOR()");
        enterLoginCredentials("doktor", "1234");
        assertSuccessfulLogin("Doktor Kosmos");
        LOGGER.info("Finished test:::testLoginSuccess_DOCTOR()");
    }

    @Test
    public void testLoginSuccess_NURSE() throws Exception {
        LOGGER.info("Starting test:::testLoginSuccess_NURSE()");
        enterLoginCredentials("syster", "1234");
        assertSuccessfulLogin("Syster Yster");
        LOGGER.info("Finished test:::testLoginSuccess_NURSE()");
    }

    @Test
    public void testLoginFail() throws Exception {
        LOGGER.info("Starting test:::testLoginFail()");
        enterLoginCredentials("hacker", "badpassword");
        selenium.waitForPageToLoad("3000");
        assertTrue(selenium.isElementPresent("userinfo"));
        assertTrue(selenium.isElementPresent("loginForm"));
        assertTrue(selenium.isTextPresent(getProp("error.login")));
        LOGGER.info("Finished test:::testLoginFail()");
    }

    private void assertSuccessfulLogin(String name) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            selenium.waitForPageToLoad("3000");
        } else {
            selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        }
        assertTrue(selenium.isTextPresent(name));
        assertTrue(selenium.isElementPresent("commonName"));
        assertTrue(selenium.isElementPresent("organizationName"));
        assertTrue(selenium.isElementPresent("roles"));
        assertTrue(selenium.isElementPresent("role"));
        assertTrue(selenium.isElementPresent("logoutLink"));
    }

}
