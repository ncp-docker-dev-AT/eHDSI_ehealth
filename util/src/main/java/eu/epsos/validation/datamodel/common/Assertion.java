package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class represents a Assertion object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class Assertion {

    /* PARAMETERS */
    private String assertionId;
    private String idScheme;

    /* GETTERS AND SETTERS */

    /**
     * @return the assertionId
     */
    @XmlAttribute(name = "assertionId")
    public String getAssertionId() {
        return assertionId;
    }

    /**
     * @param assertionId the assertionId to set
     */
    public void setAssertionId(String assertionId) {
        this.assertionId = assertionId;
    }

    /**
     * @return the idScheme
     */
    @XmlAttribute(name = "idScheme")
    public String getIdScheme() {
        return idScheme;
    }

    /**
     * @param idScheme the idScheme to set
     */
    public void setIdScheme(String idScheme) {
        this.idScheme = idScheme;
    }
}
