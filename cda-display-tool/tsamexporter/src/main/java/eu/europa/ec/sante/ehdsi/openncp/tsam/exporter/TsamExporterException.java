package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter;

public class TsamExporterException extends RuntimeException {

    public TsamExporterException(String message) {
        super(message);
    }

    public TsamExporterException(String message, Throwable cause) {
        super(message, cause);
    }
}
