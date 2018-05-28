package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a Document Valid XSD object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlType(propOrder = {"xsdMessage", "result", "nbOfErrors", "nbOfWarnings"})
public class DocumentValidXsd {

    /* PARAMETERS */
    private XsdMessage xsdMessage;
    private String result;
    private String nbOfErrors;
    private String nbOfWarnings;

    /* GETTERS AND SETTERS */

    /**
     * @return the xsdMessage
     */
    @XmlElement(name = "XSDMessage")
    public XsdMessage getXsdMessage() {
        return xsdMessage;
    }

    /**
     * @param xsdMessage the xsdMessage to set
     */
    public void setXsdMessage(XsdMessage xsdMessage) {
        this.xsdMessage = xsdMessage;
    }

    /**
     * @return the result
     */
    @XmlElement(name = "Result")
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the nbOffErros
     */
    @XmlElement(name = "nbOfErrors")
    public String getNbOfErrors() {
        return nbOfErrors;
    }

    /**
     * @param nbOfErrors the nbOffErros to set
     */
    public void setNbOfErrors(String nbOfErrors) {
        this.nbOfErrors = nbOfErrors;
    }

    /**
     * @return the nbOfWarnings
     */
    @XmlElement(name = "nbOfWarnings")
    public String getNbOfWarnings() {
        return nbOfWarnings;
    }

    /**
     * @param nbOfWarnings the nbOfWarnings to set
     */
    public void setNbOfWarnings(String nbOfWarnings) {
        this.nbOfWarnings = nbOfWarnings;
    }
}
