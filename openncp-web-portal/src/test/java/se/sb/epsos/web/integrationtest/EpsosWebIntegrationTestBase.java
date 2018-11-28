package se.sb.epsos.web.integrationtest;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.Wait;
import com.thoughtworks.selenium.Wait.WaitTimedOutException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class EpsosWebIntegrationTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpsosWebIntegrationTestBase.class);

    ;
    protected static PropertiesConfiguration config;
    protected Selenium selenium;
    protected WebDriver driver;
    protected String targetUrl;

    public static String getProp(String property) {
        return config.getProperty(property).toString();
    }

    public boolean waitForTextPresent(final String text, long timeout) {
        Wait wait = new Wait() {

            @Override
            public boolean until() {
                try {
                    return selenium.isTextPresent(getProp(text));
                } catch (SeleniumException e) {
                    return false;
                }
            }
        };
        try {
            wait.wait("Error: text " + text + " not present in the page", timeout, 50);
        } catch (NumberFormatException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (WaitTimedOutException e) {
            LOGGER.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Before
    public void setUp() throws Exception {
        LOGGER.info("Starting Selenium:");
        this.driver = System.getProperty("os.name").startsWith("Windows") ? new InternetExplorerDriver() : new FirefoxDriver();

        targetUrl = System.getProperty("integrationtest.targetUrl");
        if (targetUrl == null || targetUrl.length() == 0) {
            targetUrl = "http://localhost:8080";
        }
        LOGGER.info("Initiating with selenium: " + (selenium != null ? ("provided " + selenium.getClass().getSimpleName()) : "not provided, creating new"));
        this.selenium = selenium == null ? new WebDriverBackedSelenium(this.driver, this.targetUrl) : selenium;
        LOGGER.info("Selenium started::: with base URL: " + targetUrl);

        String path = System.getProperty("epsos-web-application-properties");
        LOGGER.debug("Path: " + path);
        if (path == null || path.length() == 0) {
            path = "/ApotekensService/epsos/workspace/salar-epsos-web/target/epsos-web/WEB-INF/classes/se/sb/epsos/web/EpsosWebApplication_en.properties";
        }
        File iSearchFile = new File(path);
        LOGGER.debug("Path: " + iSearchFile.getPath());
        config = new PropertiesConfiguration(iSearchFile);
    }

    @After
    public void tearDown() throws Exception {
        if (driver != null) {
            driver.close();
        }
        LOGGER.info("Selenium stopped");
    }

    protected void clickOnElement(WebElement element) {
        //Click() does not work with InternetExplorerDriver. Send Enter to the element instead.
        if (System.getProperty("os.name").startsWith("Windows")) {
            driver.switchTo().window(driver.getWindowHandle());
            element.sendKeys(Keys.ENTER);
        } else {
            element.click();
        }
    }

    protected void enterLoginCredentials(String userName, String pwd) {
        selenium.open("/");
        selenium.waitForPageToLoad("3000");
        selenium.type("username", userName);
        selenium.type("password", pwd);
        clickOnElement(driver.findElement(By.name("submitButton")));
        if (System.getProperty("os.name").startsWith("Windows")) {
            selenium.waitForPageToLoad("3000");
        } else {
            selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        }
    }

    protected void assertQueryPageCountries() {
        LOGGER.info("Starting test:::assertQueryPageCountries()");
        selenium.waitForPageToLoad("3000");
        selenium.select("id=country", "label=" + getProp("country.SE"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        selenium.select("id=country", "label=" + getProp("country.DK"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        selenium.select("id=country", "label=" + getProp("country.FR"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        selenium.select("id=country", "label=" + getProp("country.IT"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:1:idTextField"));
        selenium.select("id=country", "label=" + getProp("country.ES"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:1:idTextField"));
        selenium.select("id=country", "label=" + getProp("country.CZ"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:1:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:2:idTextField"));
        selenium.select("id=country", "label=" + getProp("country.AT"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:1:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:2:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:3:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:4:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:5:idTextField"));
        assertTrue(selenium.isElementPresent("patientIds:6:idTextField"));
        LOGGER.info("Finished test:::assertQueryPageCountries()");
    }

    protected void validateQueryPageInvalidPersonId() {
        LOGGER.info("Starting test:::validateQueryPageInvalidPersonId()");
        selenium.waitForPageToLoad("3000");
        selenium.select("id=country", "label=" + getProp("country.SE"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        selenium.type("patientIds:0:idTextField", "123");
        clickOnElement(driver.findElement(By.id("submit")));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
//		assertTrue(selenium.isTextPresent("måste innehålla mellan 12 och 12 tecken"));
        LOGGER.info("Finished test:::validateQueryPageInvalidPersonId()");
    }

    protected void verifyQueryPersonSuccess(Roles requestingRole) {
        LOGGER.info("Starting test:::verifyQueryPersonSuccess()");
        selenium.waitForPageToLoad("3000");
        selenium.select("id=country", "label=" + getProp("country.SE"));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
        assertTrue(selenium.isElementPresent("patientIds:0:idTextField"));
        String prescription = getProp("person.actions.prescriptions");
        String patient_summary = getProp("person.actions.patientsummary");
        switch (requestingRole) {
            case DOCTOR:
                selenium.type("patientIds:0:idTextField", "192405038569");
                clickOnElement(driver.findElement(By.id("submit")));
                selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
                assertTrue(selenium.isTextPresent(patient_summary));
                assertFalse(selenium.isTextPresent(prescription));
                assertTrue(selenium.isTextPresent("Ofelia"));
                assertTrue(selenium.isTextPresent("Ordinationslista"));
                break;
            case PHARMACIST:
                selenium.type("patientIds:0:idTextField", "193508249079");
                clickOnElement(driver.findElement(By.id("submit")));
                selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
                assertFalse(selenium.isTextPresent(patient_summary));
                assertTrue(selenium.isTextPresent(prescription));
                assertTrue(selenium.isTextPresent("Aivars"));
                assertTrue(selenium.isTextPresent("Brun"));
                break;
        }

        assertTrue(selenium.isTextPresent(getProp("person.actions.details")));
        LOGGER.info("Finished test:::verifyQueryPersonSuccess()");
    }

    protected void acceptTrc() {
        clickOnElement(driver.findElement(By.name("confirmButton")));
        selenium.waitForPageToLoad("3000");
    }

    protected void clickDispensePrescription() {
        assertTrue(selenium.isTextPresent(getProp("EP.pagetitle")));
        clickOnElement(driver.findElement(By.linkText(getProp("document.actions.dispense"))));
        selenium.waitForCondition("!selenium.browserbot.getCurrentWindow().wicketAjaxBusy()", "3000");
    }

    protected enum Roles {DOCTOR, PHARMACIST}
}
