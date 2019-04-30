package eu.epsos.exceptions;

/**
 * Holds exceptions originated in the XCA Query / Retrieve process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XCAException extends Exception {

    private static final long serialVersionUID = 1L;

    public XCAException(String message) {
        super(message);
    }

    public XCAException(String message, Throwable cause) {
        super(message, cause);
    }

    public XCAException(Throwable cause) {
        super(cause);
    }
}
