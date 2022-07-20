package eu.europa.ec.sante.ehdsi.openncp.abusedetection;

public class AbuseDetectionException extends RuntimeException {

    public AbuseDetectionException(String message) {
        super(message);
    }

    public AbuseDetectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbuseDetectionException(Throwable cause) {
        super(cause);
    }
}
