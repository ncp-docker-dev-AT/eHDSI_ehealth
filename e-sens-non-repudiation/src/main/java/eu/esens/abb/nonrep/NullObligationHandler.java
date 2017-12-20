package eu.esens.abb.nonrep;

import org.w3c.dom.Document;

public class NullObligationHandler implements ObligationHandler {

    @Override
    public void discharge() {
    }

    @Override
    public Document getMessage() {
        return null;
    }
}
