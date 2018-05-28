package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a XsdMessage object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlType(propOrder = {"severity", "message", "columnNumber", "lineNumber"})
public class XsdMessage {

    /* PARAMETERS */
    private String severity;
    private String message;
    private String columnNumber;
    private String lineNumber;

    /* GETTERS AND SETTERS */

    /**
     * @return the severity
     */
    @XmlElement(name = "Severity")
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * @return the message
     */
    @XmlElement(name = "Message")
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the columnNumber
     */
    @XmlElement(name = "columnNumber")
    public String getColumnNumber() {
        return columnNumber;
    }

    /**
     * @param columnNumber the columnNumber to set
     */
    public void setColumnNumber(String columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * @return the lineNumber
     */
    @XmlElement(name = "lineNumber")
    public String getLineNumber() {
        return lineNumber;
    }

    /**
     * @param lineNumber the lineNumber to set
     */
    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }
}
