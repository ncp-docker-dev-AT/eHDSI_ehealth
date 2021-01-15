package eu.epsos.exceptions;

/**
 * Holds exceptions originated in the XDR Submit process.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class XDRException extends Exception {

    private static final long serialVersionUID = 1L;

    public XDRException(String message) {
        super(message);
    }

    public XDRException(String message, Throwable cause) {
        super(message, cause);
    }

    public XDRException(Throwable cause) {
        super(cause);
    }
}
