package eu.europa.ec.sante.ehdsi.gazelle.validation;

import eu.epsos.validation.datamodel.common.NcpSide;

public interface HL7v3Validator {

    String validateDocument(String document, String validator, NcpSide ncpSide);

    String validateBase64Document(String base64Document, String validator, NcpSide ncpSide);
}
