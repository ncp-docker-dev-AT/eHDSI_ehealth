package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;
import eu.europa.ec.sante.ehdsi.constant.error.EhiErrorCode;

/**
 * This class represents an Exception occurred due to document transformation (translation/transcoding) issues.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class DocumentTransformationException extends Exception {

    private static long serialVersionUID = 1L;

    private EhiErrorCode ehiErrorCode;

    private EhdsiErrorCode ehdsiErrorCode;

    private String codeContext;

    public DocumentTransformationException(EhiErrorCode ehiErrorCode, EhdsiErrorCode ehdsiErrorCode, String codeContext, String message) {
        super(message);
        this.ehiErrorCode = ehiErrorCode;
        this.ehdsiErrorCode = ehdsiErrorCode;
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

    public EhiErrorCode getEhiCode() {
        return ehiErrorCode;
    }

    public EhdsiErrorCode getEhdsiCode() {
        return ehdsiErrorCode;
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
