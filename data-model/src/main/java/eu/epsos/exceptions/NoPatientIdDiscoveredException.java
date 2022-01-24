package eu.epsos.exceptions;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class NoPatientIdDiscoveredException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoPatientIdDiscoveredException(String message) {
        super(message);
    }

    public NoPatientIdDiscoveredException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPatientIdDiscoveredException(Throwable cause) {
        super(cause);
    }
}
