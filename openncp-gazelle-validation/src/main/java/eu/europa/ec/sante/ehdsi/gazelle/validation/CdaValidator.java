package eu.europa.ec.sante.ehdsi.gazelle.validation;

import eu.epsos.validation.datamodel.common.NcpSide;

public interface CdaValidator {

    boolean validateDocument(String document, String validator, NcpSide ncpSide);

    boolean validateBase64Document(String base64Document, String validator, NcpSide ncpSide);
}
