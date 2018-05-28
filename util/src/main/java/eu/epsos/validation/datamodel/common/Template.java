package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * This class represents a Template object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlType(propOrder = {"validation", "templateIds", "template"})
public class Template {

    /* PARAMETERS */
    private String location;
    private String type;
    private String validation;
    private List<TemplateId> templateIds;
    private Template template;

    /* GETTERS AND SETTERS */

    /**
     * @return the location
     */
    @XmlAttribute
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the type
     */
    @XmlAttribute
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the validation
     */
    @XmlAttribute
    public String getValidation() {
        return validation;
    }

    /**
     * @param validation the validation to set
     */
    public void setValidation(String validation) {
        this.validation = validation;
    }

    /**
     * @return the templateIds
     */
    @XmlElement(name = "templateId")
    public List<TemplateId> getTemplateIds() {
        return templateIds;
    }

    /**
     * @param templateIds the templateIds to set
     */
    public void setTemplateIds(List<TemplateId> templateIds) {
        this.templateIds = templateIds;
    }

    /**
     * @return the template
     */
    @XmlElement(name = "Template")
    public Template getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(Template template) {
        this.template = template;
    }
}
