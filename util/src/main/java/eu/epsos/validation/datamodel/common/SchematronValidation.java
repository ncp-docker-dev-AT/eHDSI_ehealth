package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * This class represents a SchematronValidation object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlType(propOrder = {"warnings", "reports", "result", "validationCounters"})
public class SchematronValidation {

    /* PARAMETERS */
    private List<Warning> warnings;
    private List<Report> reports;
    private String result;
    private ValidationCounters validationCounters;


    /* GETTERS AND SETTERS */

    /**
     * @return the warnings
     */
    @XmlElement(name = "Warning")
    public List<Warning> getWarnings() {
        return warnings;
    }

    /**
     * @param warnings the warnings to set
     */
    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }

    /**
     * @return the reports
     */
    @XmlElement(name = "Report")
    public List<Report> getReports() {
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void setReports(List<Report> reports) {
        this.reports = reports;
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
     * @return the validationCounters
     */
    @XmlElement(name = "ValidationCounters")
    public ValidationCounters getValidationCounters() {
        return validationCounters;
    }

    /**
     * @param validationCounters the validationCounters to set
     */
    public void setValidationCounters(ValidationCounters validationCounters) {
        this.validationCounters = validationCounters;
    }
}