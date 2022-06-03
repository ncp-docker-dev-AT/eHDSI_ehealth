package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;

public class InsufficientRightsException extends Exception {

    private static final long serialVersionUID = -7973928727557097260L;

    private final EhdsiCode ehdsiCode;

    public InsufficientRightsException() {
        ehdsiCode = EhdsiCode.EHDSI_ERROR_4703;
    }

    @Override
    public String getMessage() {
        return ehdsiCode.getMessage();
    }

    public String getCode() {
        return ehdsiCode.getCodeToString();
    }

    public EhdsiCode getEhdsiCode() {
        return ehdsiCode;
    }

}
