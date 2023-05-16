package org.openhealthtools.openatna.anom;

/**
 * Exception to be thrown if there are errors in the message format or
 * (de)serialization.
 * <p/>
 * This may contain a message, but this is not guaranteed, as the exception may have
 * been thrown before a message was available. In this case, it may contain a string representation
 * of the xml, or an exception message if even this was not retrievable.
 * <p/>
 * Therefore, the message may be null
 */
public class AtnaException extends Exception {

    private static final long serialVersionUID = -8359738447751397693L;
    private final AtnaError error;

    public AtnaException(String s) {
        this(s, AtnaError.UNDEFINED);
    }

    public AtnaException(String s, Throwable throwable) {
        this(s, throwable, AtnaError.UNDEFINED);
    }

    public AtnaException(Throwable throwable) {
        this(throwable, AtnaError.UNDEFINED);
    }

    public AtnaException(String s, Throwable throwable, AtnaMessage message) {
        this(s, throwable, message, AtnaError.UNDEFINED);
    }

    public AtnaException(String s, Throwable throwable, AtnaError error) {
        this(s, throwable, null, error);
    }

    public AtnaException(String s, AtnaError error) {
        super(s);
        this.error = error;
    }

    public AtnaException(String s, Throwable throwable, AtnaMessage message, AtnaError error) {
        super(s, throwable);
        this.error = error;
    }

    public AtnaException(Throwable throwable, AtnaError error) {
        super(throwable);
        this.error = error;
    }

    public AtnaError getError() {
        return error;
    }

    public enum AtnaError {

        UNDEFINED,
        NO_MESSAGE,
        NO_EVENT,
        NO_EVENT_CODE,
        NO_EVENT_OUTCOME,
        NO_EVENT_TIMESTAMP,
        INVALID_EVENT_TIMESTAMP,
        NO_AUDIT_SOURCE,
        NO_AUDIT_SOURCE_ID,
        NO_ACTIVE_PARTICIPANT,
        NO_ACTIVE_PARTICIPANT_ID,
        NO_NETWORK_ACCESS_POINT_ID,
        NO_NETWORK_ACCESS_POINT_TYPE,
        NO_PARTICIPANT_OBJECT,
        NO_PARTICIPANT_OBJECT_ID,
        NO_PARTICIPANT_OBJECT_ID_TYPE_CODE,
        INVALID_OBJECT_DETAIL,
        INVALID_CODE,
        INVALID_MESSAGE
    }
}
