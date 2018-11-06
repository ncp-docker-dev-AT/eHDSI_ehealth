package se.sb.epsos.web.auth;

import org.apache.wicket.Localizer;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.sb.epsos.web.EpsosWebApplication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
public class LoginPageTest {

    Localizer localizer;
    private WicketTester tester;
    @Autowired
    private EpsosWebApplication epsosWebApplication;

    @Before
    public void setUp() {
        tester = new WicketTester(epsosWebApplication);
        localizer = tester.getApplication().getResourceSettings().getLocalizer();
    }

    @Test
    @Ignore
    public void testSetupSSL() {
        epsosWebApplication.setSSL();
        assertNotNull(System.getProperty("javax.net.ssl.trustStore"));
        assertNotNull(System.getProperty("javax.net.ssl.trustStorePassword"));
        assertNotNull(System.getProperty("javax.net.ssl.keyStore"));
        assertNotNull(System.getProperty("javax.net.ssl.keyStorePassword"));
        assertNotNull(System.getProperty("javax.net.ssl.key.alias"));
        assertNotNull(System.getProperty("javax.net.ssl.privateKeyPassword"));
    }

    @Test
    public void testLoginPageRendersSuccessfully() {
        //start and render the test page
        tester.startPage(LoginPage.class);
        //assert promt for login
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testLoginSucess() {
        //start and render the test page
        tester.startPage(LoginPage.class);
        // perform login
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "doktor");
        formTester.setValue("password", "1234");
        tester.executeAjaxEvent("loginForm:submitButton", "onclick");
        //assert home page is rendered
        tester.assertRenderedPage(epsosWebApplication.getHomePage());
    }

    @Test
    public void testLogout() {
        testLoginSucess();
        try {
            tester.clickLink("userinfo:logoutLink");
        } catch (Exception e) {
            assertTrue(e instanceof RestartResponseAtInterceptPageException);
        }
    }

    @Test
    public void testLoginFailure() {
        //start and render the test page
        tester.startPage(LoginPage.class);
        // perform login
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "doktor");
        formTester.setValue("password", "xxxx");
        tester.executeAjaxEvent("loginForm:submitButton", "onclick");
        //assert home page is rendered
        tester.assertRenderedPage(LoginPage.class);
        tester.assertErrorMessages(new String[]{localizer.getString("error.login", null)});
    }
}
