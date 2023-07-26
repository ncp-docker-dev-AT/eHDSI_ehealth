package epsos.ccd.gnomon.xslt.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class UITransformationException extends Exception {

    private final OpenNCPErrorCode openncpErrorCode;

    private final Exception originalException;

    public UITransformationException(Exception e) {
        openncpErrorCode = OpenNCPErrorCode.ERROR_UI_TRANSFORMATION;
        originalException = e;
    }

    public String getCode() {
        return openncpErrorCode.getCode();
    }

    public Exception getOriginalException() {
        return originalException;
    }

    public OpenNCPErrorCode getErrorCode() {
        return openncpErrorCode;
    }

}
