package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.openncp.util.error.EhdsiErrorCode;

public class InsufficientRightsException extends Exception {

    private static final long serialVersionUID = -7973928727557097260L;

    private final EhdsiErrorCode ehdsiErrorCode;

    public InsufficientRightsException() {
        ehdsiErrorCode = EhdsiErrorCode.EHDSI_ERROR_4703;
    }

    @Override
    public String getMessage() {
        return ehdsiErrorCode.getMessage();
    }

    public String getCode() {
        return ehdsiErrorCode.getCodeToString();
    }

    public EhdsiErrorCode getEhdsiCode() {
        return ehdsiErrorCode;
    }

}
