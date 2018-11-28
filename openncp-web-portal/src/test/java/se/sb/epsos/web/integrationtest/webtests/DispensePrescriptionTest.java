package se.sb.epsos.web.integrationtest.webtests;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import se.sb.epsos.web.integrationtest.EpsosWebIntegrationTestBase;

import static org.junit.Assert.assertTrue;

/**
 * @author danielgronberg
 */
public class DispensePrescriptionTest extends EpsosWebIntegrationTestBase {

    @Before
    public void prepare() throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            enterLoginCredentials("apotek", "1234");
            verifyQueryPersonSuccess(Roles.PHARMACIST);
            clickOnElement(driver.findElement(By.linkText(getProp("person.actions.prescriptions"))));
            selenium.waitForPageToLoad("3000");
            acceptTrc();
        }
    }

    @Test
    @Ignore
    public void testDispensePrescriptionPageIsAvailable() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            clickDispensePrescription();
            assertTrue(selenium.isTextPresent(getProp("dispensation.prescriptioninfo")));
        }
    }

    @Test
    @Ignore
    public void testDispensePrescriptionAfterBackButton() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            clickDispensePrescription();
            selenium.goBack();

            selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");

            clickDispensePrescription();
            assertTrue(selenium.isTextPresent(getProp("dispensation.prescriptioninfo")));
        }
    }
}
