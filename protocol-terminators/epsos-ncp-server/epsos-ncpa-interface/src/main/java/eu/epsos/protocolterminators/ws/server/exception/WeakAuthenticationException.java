package eu.epsos.protocolterminators.ws.server.exception;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

public class WeakAuthenticationException extends NIException {
    private static final long serialVersionUID = -3723212345611879399L;

    public WeakAuthenticationException(String message) {
        super(OpenNCPErrorCode.ERROR_WEAK_AUTHENTICATION, message);
    }
}
