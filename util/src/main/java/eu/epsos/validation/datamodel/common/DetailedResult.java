package eu.epsos.validation.datamodel.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@XmlRootElement
public class DetailedResult {

    /* PARAMETERS */
    private DocumentValidXsd documentValidXsd;
    private DocumentWellFormed documentWellFormed;
    private MdaValidation mdaValidation;
    private ValidationResultsOverview valResultsOverview;
    private TemplateDesc templateDesc;
    private SchematronValidation schematronValidation;

    /* GETTERS AND SETTERS */

    /**
     * @return the documentValidXsd
     */
    @XmlElement(name = "DocumentValidXSD")
    public DocumentValidXsd getDocumentValidXsd() {
        return documentValidXsd;
    }

    /**
     * @param documentValidXsd the documentValidXsd to set
     */
    public void setDocumentValidXsd(DocumentValidXsd documentValidXsd) {
        this.documentValidXsd = documentValidXsd;
    }

    /**
     * @return the documentWellFormed
     */
    @XmlElement(name = "DocumentWellFormed")
    public DocumentWellFormed getDocumentWellFormed() {
        return documentWellFormed;
    }

    /**
     * @param documentWellFormed the documentWellFormed to set
     */
    public void setDocumentWellFormed(DocumentWellFormed documentWellFormed) {
        this.documentWellFormed = documentWellFormed;
    }

    /**
     * @return the mdaValidation
     */
    @XmlElement(name = "MDAValidation")
    public MdaValidation getMdaValidation() {
        return mdaValidation;
    }

    /**
     * @param mdaValidation the mdaValidation to set
     */
    public void setMdaValidation(MdaValidation mdaValidation) {
        this.mdaValidation = mdaValidation;
    }

    /**
     * @return the valResultsOverview
     */
    @XmlElement(name = "ValidationResultsOverview")
    public ValidationResultsOverview getValResultsOverview() {
        return valResultsOverview;
    }

    /**
     * @param valResultsOverview the valResultsOverview to set
     */
    public void setValResultsOverview(ValidationResultsOverview valResultsOverview) {
        this.valResultsOverview = valResultsOverview;
    }

    /**
     * @return the templateDesc
     */
    @XmlElement(name = "TemplateDesc")
    public TemplateDesc getTemplateDesc() {
        return templateDesc;
    }

    /**
     * @param templateDesc the templateDesc to set
     */
    public void setTemplateDesc(TemplateDesc templateDesc) {
        this.templateDesc = templateDesc;
    }

    /**
     * @return the schematronValidation
     */
    @XmlElement(name = "SchematronValidation")
    public SchematronValidation getSchematronValidation() {
        return schematronValidation;
    }

    /**
     * @param schematronValidation the schematronValidation to set
     */
    public void setSchematronValidation(SchematronValidation schematronValidation) {
        this.schematronValidation = schematronValidation;
    }


}
