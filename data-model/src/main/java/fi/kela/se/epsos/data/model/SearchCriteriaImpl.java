package fi.kela.se.epsos.data.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tr.com.srdc.epsos.data.model.PatientId;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public class SearchCriteriaImpl implements SearchCriteria {

    private final Logger logger = LoggerFactory.getLogger(SearchCriteriaImpl.class);
    private final PatientId patientId = new PatientId();
    private final Map<Criteria, String> criteriaMap = new EnumMap<>(Criteria.class);

    public SearchCriteriaImpl() {
        //  Default SearchCriteria constructor.
    }

    public SearchCriteria addPatientId(String patientId) {
        this.add(Criteria.PATIENT_ID, patientId);
        String[] result = StringUtils.split(patientId, "^^^&|&ISO");
        if(result.length > 1) {
            this.patientId.setRoot(result[1]);
        }
        this.patientId.setExtension(result[0]);
        return this;
    }

    public SearchCriteria add(Criteria c, String value) {
        if (c != null && value != null) {
            criteriaMap.put(c, value);
        }
        return this;
    }

    public String getCriteriaValue(Criteria c) {
        return criteriaMap.get(c);
    }

    public PatientId getPatientId() {
        return this.patientId;
    }

    public Iterator<Criteria> getSearchCriteriaKeys() {
        return criteriaMap.keySet().iterator();
    }

    public Document asXml() {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<SearchCriteria>");
            for (Map.Entry<Criteria, String> entry : criteriaMap.entrySet()) {
                stringBuilder.append("<");
                stringBuilder.append(entry.getKey().value);
                stringBuilder.append(">");
                stringBuilder.append(StringEscapeUtils.escapeHtml4(entry.getValue()));
                stringBuilder.append("</");
                stringBuilder.append(entry.getKey().value);
                stringBuilder.append(">");
            }
            stringBuilder.append("</SearchCriteria>");
            return builder.parse(new InputSource(new StringReader(stringBuilder.toString())));
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            logger.error("XML Transformation Exception: '{}'", ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Criteria, String> entry : criteriaMap.entrySet()) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("SearchCriteria {");
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append(entry.getKey().value).append(" = ");
            stringBuilder.append(entry.getValue());
        }
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
