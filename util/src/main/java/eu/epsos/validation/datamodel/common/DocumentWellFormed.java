package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a DocumentWellFormed object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlType(propOrder = {"result"})
public class DocumentWellFormed {

    /* PARAMETERS */
    private String result;

    /* GETTERS AND SETTERS */

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
}
