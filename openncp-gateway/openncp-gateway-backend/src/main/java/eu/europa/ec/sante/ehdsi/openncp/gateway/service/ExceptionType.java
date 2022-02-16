package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import org.springframework.http.HttpStatus;

public enum ExceptionType {

    HTTP_MESSAGE_NOT_READABLE(HttpStatus.INTERNAL_SERVER_ERROR, "Http message not readable."),
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.INTERNAL_SERVER_ERROR, "Method argument is nog valid."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred."),

    // Gateway Admin module
    PWD_INVALID_FORMAT(HttpStatus.FORBIDDEN, "Invalid password : Length should between 8 and 30, one Uppercase and no white spaces");

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
