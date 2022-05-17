package eu.epsos.exceptions;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class NoPatientIdDiscoveredException extends ExceptionWithContext {

    public NoPatientIdDiscoveredException(String message) {
        super(message);
    }

    public NoPatientIdDiscoveredException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPatientIdDiscoveredException(Throwable cause) {
        super(cause);
    }

    public NoPatientIdDiscoveredException(String message, String context) {
        super(message, context);
    }

    public NoPatientIdDiscoveredException(String message, String context, Throwable cause) {
        super(message, context, cause);
    }
}
