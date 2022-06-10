package eu.europa.ec.sante.ehdsi.openncp.assertionvalidator.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

public class InsufficientRightsException extends Exception {

    private static final long serialVersionUID = -7973928727557097260L;

    private final EhdsiErrorCode ehdsiErrorCode;

    public InsufficientRightsException() {
        ehdsiErrorCode = EhdsiErrorCode.EHDSI_ERROR_4703;
    }

    @Override
    public String getMessage() {
        return ehdsiErrorCode.getDescription();
    }

    public String getCode() {
        return ehdsiErrorCode.getCode();
    }

    public EhdsiErrorCode getEhdsiCode() {
        return ehdsiErrorCode;
    }

}
