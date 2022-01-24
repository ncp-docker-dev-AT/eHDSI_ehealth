package eu.epsos.exceptions;

/**
 * Holds exceptions originated in the XCA Query / Retrieve process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XCAException extends ExceptionWithContext {

    public XCAException(String message) {
        super(message);
    }

    public XCAException(String message, Throwable cause) {
        super(message, cause);
    }

    public XCAException(Throwable cause) {
        super(cause);
    }

    public XCAException(String message, String context) {
        super(message, context);
    }

    public XCAException(String message, String context, Throwable cause) {
        super(message, context, cause);
    }
}
