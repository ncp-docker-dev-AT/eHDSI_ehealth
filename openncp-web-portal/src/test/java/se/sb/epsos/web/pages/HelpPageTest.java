package se.sb.epsos.web.pages;

import org.apache.wicket.Localizer;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.sb.epsos.web.EpsosWebApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-application-context.xml"})
public class HelpPageTest {
    private WicketTester tester;
    private Localizer localizer;

    @Autowired
    private EpsosWebApplication epsosWebApplication;

    @Before
    public void setUp() {
        tester = new WicketTester(epsosWebApplication);
        localizer = tester.getApplication().getResourceSettings().getLocalizer();
    }

    @Test
    public void helpPageRender() {
        tester.startPage(HelpPage.class);
        tester.assertRenderedPage(HelpPage.class);
        tester.assertContains(localizer.getString("label.contact", null));
        tester.assertContains(localizer.getString("label.contactname", null));
        tester.assertContains(localizer.getString("label.contactemail", null));
        tester.assertNoErrorMessage();
    }
}
