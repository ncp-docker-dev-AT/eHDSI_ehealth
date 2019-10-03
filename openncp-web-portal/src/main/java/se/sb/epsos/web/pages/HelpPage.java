package se.sb.epsos.web.pages;

import org.apache.wicket.markup.html.basic.Label;
import se.sb.epsos.web.BasePage;


public class HelpPage extends BasePage {

    public HelpPage() {
        getSession().clearBreadCrumbList();
        add(new Label("contactname", getLocalizer().getString("label.contactname", this)));
        add(new Label("contactemail", getLocalizer().getString("label.contactemail", this)));
    }
}
