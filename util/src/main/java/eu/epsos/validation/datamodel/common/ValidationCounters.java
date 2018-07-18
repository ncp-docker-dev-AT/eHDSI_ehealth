package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a ValidationCounters object.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlType(propOrder = {"nrOfChecks", "nrOfValidationErros", "nrOfValidationWarnings", "nrOfValidationNotes", "nrOfValidationReports", "nrOfValidationUnknown"})
public class ValidationCounters {

    /* PARAMETERS */
    private String nrOfChecks;
    private String nrOfValidationErros;
    private String nrOfValidationWarnings;
    private String nrOfValidationNotes;
    private String nrOfValidationReports;
    private String nrOfValidationUnknown;

    /* GETTERS AND SETTERS */

    /**
     * @return the nrOfChecks
     */
    @XmlElement(name = "NrOfChecks")
    public String getNrOfChecks() {
        return nrOfChecks;
    }

    /**
     * @param nrOfChecks the nrOfChecks to set
     */
    public void setNrOfChecks(String nrOfChecks) {
        this.nrOfChecks = nrOfChecks;
    }

    /**
     * @return the nrOfValidationErros
     */
    @XmlElement(name = "NrOfValidationErrors")
    public String getNrOfValidationErros() {
        return nrOfValidationErros;
    }

    /**
     * @param nrOfValidationErros the nrOfValidationErros to set
     */
    public void setNrOfValidationErros(String nrOfValidationErros) {
        this.nrOfValidationErros = nrOfValidationErros;
    }

    /**
     * @return the nrOfValidationWarnings
     */
    @XmlElement(name = "NrOfValidationWarnings")
    public String getNrOfValidationWarnings() {
        return nrOfValidationWarnings;
    }

    /**
     * @param nrOfValidationWarnings the nrOfValidationWarnings to set
     */
    public void setNrOfValidationWarnings(String nrOfValidationWarnings) {
        this.nrOfValidationWarnings = nrOfValidationWarnings;
    }

    /**
     * @return the nrOfValidationNotes
     */
    @XmlElement(name = "NrOfValidationNotes")
    public String getNrOfValidationNotes() {
        return nrOfValidationNotes;
    }

    /**
     * @param nrOfValidationNotes the nrOfValidationNotes to set
     */
    public void setNrOfValidationNotes(String nrOfValidationNotes) {
        this.nrOfValidationNotes = nrOfValidationNotes;
    }

    /**
     * @return the nrOfValidationReports
     */
    @XmlElement(name = "NrOfValidationReports")
    public String getNrOfValidationReports() {
        return nrOfValidationReports;
    }

    /**
     * @param nrOfValidationReports the nrOfValidationReports to set
     */
    public void setNrOfValidationReports(String nrOfValidationReports) {
        this.nrOfValidationReports = nrOfValidationReports;
    }

    /**
     * @return the nrOfValidationUnknown
     */
    @XmlElement(name = "NrOfValidationUnknown")
    public String getNrOfValidationUnknown() {
        return nrOfValidationUnknown;
    }

    /**
     * @param nrOfValidationUnknown the nrOfValidationUnknown to set
     */
    public void setNrOfValidationUnknown(String nrOfValidationUnknown) {
        this.nrOfValidationUnknown = nrOfValidationUnknown;
    }
}
