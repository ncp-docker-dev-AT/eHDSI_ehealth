package fi.kela.se.epsos.data.model;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SearchCriteriaImpl implements SearchCriteria {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(SearchCriteriaImpl.class);

    private Map<Criteria, String> criteriaMap = new HashMap<>();

    public SearchCriteriaImpl() {
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

    public Iterator<Criteria> getSearchCriteriaKeys() {
        return criteriaMap.keySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Criteria c : criteriaMap.keySet()) {
            if (sb.length() == 0) {
                sb.append("SearchCriteria {");
            } else {
                sb.append(", ");
            }

            sb.append(c.name() + " = ");
            sb.append(criteriaMap.get(c));
        }

        sb.append("}");

        return sb.toString();
    }

    public Document asXml() {

        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringBuilder sb = new StringBuilder();
            sb.append("<SearchCriteria>");
            for (Criteria c : criteriaMap.keySet()) {
                sb.append("<");
                sb.append(c.name());
                sb.append(">");
                sb.append(criteriaMap.get(c));
                sb.append("</");
                sb.append(c.name());
                sb.append(">");
            }
            sb.append("</SearchCriteria>");
            doc = builder.parse(new InputSource(new StringReader(sb.toString())));
            return doc;
        } catch (SAXException ex) {
            logger.error("SAXException: '{}'", ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error("IOException: '{}'", ex.getMessage(), ex);
        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException: '{}'", ex.getMessage(), ex);
        }
        return doc;
    }
}
