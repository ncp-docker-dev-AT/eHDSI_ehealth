package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class represents a TemplateId object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class TemplateId {

    /* PARAMETERS */
    private String name;
    private String id;

    /* GETTERS AND SETTERS */

    /**
     * @return the name
     */
    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
