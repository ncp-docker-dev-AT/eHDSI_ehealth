package epsos.ccd.netsmart.securitymanager.exceptions;

import javax.xml.crypto.dsig.XMLSignatureException;

/**
 * The Exception that is thrown by the NCP Signature Manager. Provides an error message.
 *
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public class SMgrException extends XMLSignatureException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of the exception
     *
     * @param exceptionMessage - the error message
     */
    public SMgrException(String exceptionMessage) {
        super(exceptionMessage);
    }


    public SMgrException(String exceptionMessage, Exception e) {
        super(exceptionMessage, e);
    }

}
