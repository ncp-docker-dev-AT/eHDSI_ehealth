package fi.kela.se.epsos.data.model;

import eu.europa.ec.sante.ehdsi.constant.ClassCode;

/**
 * EPSOSDocument interface. Includes EPSOSDocumentMetaData and includes DOM Document.
 *
 * @author mimyllyv
 */
public interface EPSOSDocument {

    String getPatientId();

    ClassCode getClassCode();

    org.w3c.dom.Document getDocument();

    boolean matchesCriteria(SearchCriteria sc);
}
