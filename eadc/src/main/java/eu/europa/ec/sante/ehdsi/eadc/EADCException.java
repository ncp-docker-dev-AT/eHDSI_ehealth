package eu.europa.ec.sante.ehdsi.eadc;

public class EADCException extends Exception {
    
    public EADCException(String message) {
        super(message);
    }

    public EADCException(String message, Throwable cause) {
        super(message, cause);
    }
}
