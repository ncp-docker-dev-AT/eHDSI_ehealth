package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a Report object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */

@XmlType(propOrder = {"test", "location", "description"})
public class Report {

    /* PARAMETERS */
    private String test;
    private String location;
    private String description;
    /* GETTERS AND SETTERS */

    /**
     * @return the test
     */
    @XmlElement(name = "Test")
    public String getTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(String test) {
        this.test = test;
    }

    /**
     * @return the location
     */
    @XmlElement(name = "Location")
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
     * @return the description
     */
    @XmlElement(name = "Description")
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}