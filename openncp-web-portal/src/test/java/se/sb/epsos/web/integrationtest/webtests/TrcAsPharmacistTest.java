package se.sb.epsos.web.integrationtest.webtests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.integrationtest.EpsosWebIntegrationTestBase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author andreas
 */
public class TrcAsPharmacistTest extends EpsosWebIntegrationTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrcAsPharmacistTest.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        enterLoginCredentials("apotek", "1234");
        verifyQueryPersonSuccess(Roles.PHARMACIST);
    }

    @Test
    public void testTrcSuccess_PERSON_DETAILS() {
        LOGGER.info("Starting test:::testTrcSuccess_PERSON_DETAILS()");
        clickOnElement(driver.findElement(By.linkText(getProp("person.actions.details"))));
        ;
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("css=div.wicket-modal"));
        clickOnElement(driver.findElement(By.className("w_close")));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertFalse(selenium.isElementPresent("css=div.wicket-modal"));
        LOGGER.info("Finished test:::testTrcSuccess_PERSON_DETAILS()");
    }

    @Test
    public void testTrcSuccess_CONFIRM() {
        LOGGER.info("Starting test:::testTrcSuccess_CONFIRM()");
        clickOnElement(driver.findElement(By.linkText(getProp("person.actions.prescriptions"))));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        clickOnElement(driver.findElement(By.name("confirmButton")));
        assertTrue(waitForTextPresent("EP.pagetitle", 30000));
        LOGGER.info("Finished test:::testTrcSuccess_CONFIRM()");
    }
}
