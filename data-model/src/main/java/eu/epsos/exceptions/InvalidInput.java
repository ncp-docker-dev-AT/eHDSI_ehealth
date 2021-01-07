package eu.epsos.exceptions;

/**
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class InvalidInput extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidInput(String message) {
        super(message);
    }

    public InvalidInput(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInput(Throwable cause) {
        super(cause);
    }
}
