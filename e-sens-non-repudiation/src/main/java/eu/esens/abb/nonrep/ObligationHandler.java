package eu.esens.abb.nonrep;

import org.w3c.dom.Document;

public interface ObligationHandler {

    void discharge() throws ObligationDischargeException;

    Document getMessage();
}
