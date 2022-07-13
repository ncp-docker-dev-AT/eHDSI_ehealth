package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.OpenNCPErrorCode;

/**
 * This class represents an Exception occurred due to document transformation (translation/transcoding) issues.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class DocumentTransformationException extends Exception {

    private static long serialVersionUID = 1L;

    private OpenNCPErrorCode openncpErrorCode;

    private String codeContext;

    public DocumentTransformationException(OpenNCPErrorCode openncpErrorCode, String codeContext, String message) {
        super(message);
        this.openncpErrorCode = openncpErrorCode;
        this.codeContext = codeContext;
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


    public OpenNCPErrorCode getErrorCode() {
        return openncpErrorCode;
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
