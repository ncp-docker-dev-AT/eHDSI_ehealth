package epsos.ccd.netsmart.securitymanager.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;

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
    private final EhdsiErrorCode ehdsiErrorCode = EhdsiErrorCode.EHDSI_ERROR_SEC_DATA_INTEGRITY_NOT_ENSURED;

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

    public EhdsiErrorCode getErrorCode(){
        return ehdsiErrorCode;
    }

}
