package eu.epsos.exceptions;

/**
 * This class represents an Exception occurred due to document transformation (translation/transcoding) issues.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class DocumentTransformationException extends Exception {

    private static long serialVersionUID = 1L;
    private String errorCode;
    private String codeContext;

    public DocumentTransformationException(String message) {
        super(message);
    }

    public DocumentTransformationException(String errorCode, String codeContext, String message) {
        super(message);
        this.errorCode = errorCode;
        this.codeContext = codeContext;
    }

    public DocumentTransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentTransformationException(Throwable cause) {
        super(cause);
    }

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @param aSerialVersionUID the serialVersionUID to set
     */
    public static void setSerialVersionUID(long aSerialVersionUID) {
        serialVersionUID = aSerialVersionUID;
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the codeContext
     */
    public String getCodeContext() {
        return codeContext;
    }

    /**
     * @param codeContext the codeContext to set
     */
    public void setCodeContext(String codeContext) {
        this.codeContext = codeContext;
    }
}
