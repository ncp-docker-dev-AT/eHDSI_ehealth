package eu.epsos.exceptions;

import eu.europa.ec.sante.ehdsi.openncp.util.security.EhdsiCode;
import eu.europa.ec.sante.ehdsi.openncp.util.security.EhiCode;

/**
 * This class represents an Exception occurred due to document transformation (translation/transcoding) issues.
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
public class DocumentTransformationException extends Exception {

    private static long serialVersionUID = 1L;

    private EhiCode ehiCode;

    private EhdsiCode ehdsiCode;

    private String codeContext;

    public DocumentTransformationException(EhiCode ehiCode, EhdsiCode ehdsiCode, String codeContext, String message) {
        super(message);
        this.ehiCode = ehiCode;
        this.ehdsiCode = ehdsiCode;
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

    public EhiCode getEhiCode() {
        return ehiCode;
    }

    public EhdsiCode getEhdsiCode() {
        return ehdsiCode;
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
