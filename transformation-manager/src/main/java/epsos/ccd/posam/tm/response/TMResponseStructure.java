package epsos.ccd.posam.tm.response;

import eu.europa.ec.sante.ehdsi.constant.error.TMError;
import epsos.ccd.posam.tm.exception.TMException;
import epsos.ccd.posam.tm.util.TMConstants;
import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMEror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer class.<br>
 * Contains:
 * <li>processed (transcoded or translated) CDA document</li>
 * <li>status (success or failure)</li>
 * <li>list of Errors</li>
 * <li>list of Warnings</li>
 * <p>
 * <br>
 * Provides set & get method for simple response manipulating.
 *
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.11, 2010, 20 October
 */
public class TMResponseStructure implements TMConstants {

    private final Logger logger = LoggerFactory.getLogger(TMResponseStructure.class);

    private String requestId;

    /**
     * Target / response CDA document.
     * Options are:
     * <li>transcoded CDA pivot document</li>
     * <li>translated CDA document</li>
     * <li>CDA unstructured document with embedded pdf file</li>
     * <li>In case of ERROR original/input CDA document</li>
     */
    private Document responseCDA;

    /**
     * List of TM Errors
     */
    private List<ITMTSAMEror> errors;

    /**
     * List of TM Warnings
     */
    private List<ITMTSAMEror> warnings;

    /**
     * failure or success
     */
    private String status;

    public TMResponseStructure(Document responseCDA, String status, List<ITMTSAMEror> errors, List<ITMTSAMEror> warnings) {
        this.requestId = UUID.randomUUID().toString();
        this.responseCDA = responseCDA;
        this.errors = errors;
        this.warnings = warnings;
        this.status = status;
    }

    /**
     * Creates XML Document presenting Response structure.<br>
     * Suitable for logging, testing.
     *
     * @return Document - XML presentation of entire ResponseStructure
     */
    private Document getResponseStructureAsXmlDoc() throws TMException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element root = document.createElement("responseStructure");
            root.setAttribute("requestId", getRequestId());
            document.appendChild(root);
            root.appendChild(document.createElement("responseElement"));
            root.appendChild(document.createElement("responseStatus"));

            Node responseElement = document.getElementsByTagName(RESPONSE_ELEMENT).item(0);
            Node tempNode = document.importNode(this.responseCDA.getDocumentElement(), true);
            responseElement.appendChild(tempNode);

            // write status/ errors/ warnings into responseStatus
            Node responseStatus = document.getElementsByTagName(RESPONSE_STATUS).item(0);
            Element elementStatus = document.createElement(STATUS);
            elementStatus.setAttribute(RESULT, this.status);
            responseStatus.appendChild(elementStatus);

            // errors
            Element errorsElement = document.createElement(ERRORS);
            if (!getErrors().isEmpty()) {
                for (ITMTSAMEror tmException : uniqueList(getErrors())) {

                    Element errorElement = document.createElement(ERROR);
                    errorElement.setAttribute(CODE, tmException.getCode());
                    errorElement.setAttribute(DESCRIPTION, tmException.getDescription());
                    errorsElement.appendChild(errorElement);
                }
            }
            root.appendChild(errorsElement);

            // warnings
            Element warningsElement = document.createElement(WARNINGS);
            if (!getWarnings().isEmpty()) {
                for (ITMTSAMEror tmException : uniqueList(getWarnings())) {
                    Element warningElement = document.createElement(WARNING);
                    warningElement.setAttribute(CODE, tmException.getCode());
                    warningElement.setAttribute(DESCRIPTION, tmException.getDescription());
                    warningsElement.appendChild(warningElement);
                }
            }
            root.appendChild(warningsElement);

            return document;
        } catch (Exception e) {
            logger.error("Exception: '{}'", e.getMessage(), e);
            throw new TMException(TMError.ERROR_PROCESSING_ERROR);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private ArrayList<ITMTSAMEror> uniqueList(List<ITMTSAMEror> list) {

        ArrayList<ITMTSAMEror> result = new ArrayList<>();
        for (ITMTSAMEror itmtsamEror : list) {
            if (!result.contains(itmtsamEror)) {
                result.add(itmtsamEror);
            }
        }
        return result;
    }

    public String getRequestId() {
        return requestId;
    }

    /**
     * @return target / response CDA document. Options are:
     * <li>transcoded CDA pivot document</li>
     * <li>translated CDA document</li>
     * <li>CDA unstructured document with embedded pdf file</li>
     */
    public Document getResponseCDA() {
        return responseCDA;
    }

    public void setResponseCDA(Document responseCDA) {
        this.responseCDA = responseCDA;
    }

    /**
     * @return List of TMErrors
     */
    public List<ITMTSAMEror> getErrors() {
        return (errors == null ? new ArrayList<>() : errors);
    }

    public void setErrors(List<ITMTSAMEror> errors) {
        this.errors = errors;
    }

    /**
     * @return List of TMWarnings
     */
    public List<ITMTSAMEror> getWarnings() {
        return (warnings == null ? new ArrayList<>() : warnings);
    }

    public void setWarnings(List<ITMTSAMEror> warnings) {
        this.warnings = warnings;
    }

    public void addError(ITMTSAMEror newError) {
        if (!errors.contains(newError)) {
            errors.add(newError);
        }
    }

    public void addWarning(ITMTSAMEror newWarning) {
        if (!warnings.contains(newWarning)) {
            warnings.add(newWarning);
        }
    }

    /**
     * @return true id status is SUCCESS otherwise false
     */
    public boolean isStatusSuccess() {
        return (status != null && status.equals(STATUS_SUCCESS));
    }

    /**
     * Method is used for logging, testing.<br>
     * (To obtain CDA document use #getResponseCDA() method)<br>
     * Returns xml presentation of Response Structure in form:
     * <p>
     * <pre>
     * &lt;responseStructureDoc&gt;
     * 	  &lt;responseElement&gt;
     * 	  &lt;!--One of these:--&gt;
     * 	    &lt;!-- epSOS CDA Pivot Document (result of transcoding in country A)--&gt;
     * 	    &lt;!-- Translated epSOS CDA Pivot Document (result of translation in country B)--&gt;
     * 		&lt;!-- epSOS CDA unstructured document with embedded pdf file--&gt;
     * 	  &lt;/responseElement&gt;
     * 	  &lt;responseStatus&gt;
     * 	    &lt;status result="success/failure"/&gt;
     * 	    &lt;!-- optional --&gt;
     * 	    &lt;errors&gt;
     * 	      &lt;error code="..." description=".."/&gt;
     * 	      &lt;error code="..." description=".."/&gt;
     * 	    &lt;/errors&gt;
     * 	    &lt;!-- optional --&gt;
     * 	    &lt;warnings&gt;
     * 	      &lt;warning code="..." description=".."/&gt;
     * 	      &lt;warning code="..." description=".."/&gt;
     * 	    &lt;/warnings&gt;
     * 	  &lt;/responseStatus&gt;
     * 	&lt;/responseStructureDoc&gt;
     * </pre>
     *
     * @return Document - xml presentation of TMResponseStructure
     * @see #getResponseCDA()
     */
    public Document getDocument() throws TMException {
        return getResponseStructureAsXmlDoc();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("TMResponseStructure.toString() BEGIN : ").append(NEWLINE);
        sb.append(STATUS).append(COLON).append(status).append(NEWLINE);
        sb.append(ERRORS).append(COLON).append(NEWLINE);
        for (ITMTSAMEror tmError : errors) {
            sb.append(tmError.toString()).append(NEWLINE);
        }
        sb.append(WARNINGS).append(COLON).append(NEWLINE);
        for (ITMTSAMEror tmError : warnings) {
            sb.append(tmError.toString()).append(NEWLINE);
        }
        sb.append("TMResponseStructure.toString() END");
        return sb.toString();
    }
}
