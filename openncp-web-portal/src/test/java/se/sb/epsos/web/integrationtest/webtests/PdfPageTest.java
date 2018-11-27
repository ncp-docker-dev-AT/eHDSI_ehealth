package se.sb.epsos.web.integrationtest.webtests;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import se.sb.epsos.web.integrationtest.EpsosWebIntegrationTestBase;

import static org.junit.Assert.assertTrue;

/**
 * @author danielgronberg
 */
public class PdfPageTest extends EpsosWebIntegrationTestBase {

    @Test
    @Ignore
    public void testPdfPageShowOriginalEP() throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            enterLoginCredentials("apotek", "1234");
            verifyQueryPersonSuccess(Roles.PHARMACIST);
            clickOnElement(driver.findElement(By.linkText("Recept")));
            selenium.waitForPageToLoad("3000");
            acceptTrc();
            clickDispensePrescription();
            clickOnElement(driver.findElement(By.linkText("Visa Originalrecept")));
            selenium.waitForPageToLoad("3000");
            assertTrue(selenium.isTextPresent("Original"));
        }
    }

    @Test
    @Ignore
    public void testPdfPageShowOriginalEPAfterBackButton() throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            testPdfPageShowOriginalEP();
            selenium.goBack();
            clickOnElement(driver.findElement(By.linkText("Visa Originalrecept")));
            selenium.waitForPageToLoad("3000");
            assertTrue(selenium.isTextPresent("Original"));
        }
    }

    @Test
    @Ignore
    public void testPdfPageShowOriginalPS() throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            enterLoginCredentials("doktor", "1234");
            verifyQueryPersonSuccess(Roles.DOCTOR);
            clickOnElement(driver.findElement(By.linkText("Patientöversikt")));
            selenium.waitForPageToLoad("3000");
            acceptTrc();
            clickOnElement(driver.findElement(By.linkText("Visa original")));
            selenium.waitForPageToLoad("3000");
            assertTrue(selenium.isTextPresent("Original"));
        }
    }

    @Test
    @Ignore
    public void testPdfPageShowOriginalPSAfterBackButton() throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            testPdfPageShowOriginalPS();
            selenium.goBack();
            clickOnElement(driver.findElement(By.linkText("Visa original")));
            selenium.waitForPageToLoad("3000");
            assertTrue(selenium.isTextPresent("Original"));
        }
    }
}
