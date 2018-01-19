package epsos.ccd.posam.tm.util;

import org.jaxen.dom.DocumentNavigator;

public class NoNsNavigator extends DocumentNavigator {

    @Override
    public String getElementNamespaceUri(Object obj) {
        return null;
    }
}
