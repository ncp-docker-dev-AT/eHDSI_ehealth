package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import org.springframework.http.HttpStatus;

public enum ExceptionType {

    HTTP_MESSAGE_NOT_READABLE(HttpStatus.INTERNAL_SERVER_ERROR, "Http message not readable."),
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.INTERNAL_SERVER_ERROR, "Method argument is nog valid."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred."),

    // Gateway Admin module
    PWD_INVALID_FORMAT(HttpStatus.FORBIDDEN, "Invalid password : Length should between 8 and 30 characters with at least one uppercase letter, one lowercase letter, one number and one special character and no white spaces"),
    PWD_NOT_MATCHING(HttpStatus.FORBIDDEN, "Invalid password : Current password does not match");

    private final HttpStatus status;
    private final String message;

    ExceptionType(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
