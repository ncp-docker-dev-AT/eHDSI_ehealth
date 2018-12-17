/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.kela.se.epsos.data.model;

import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * @author jgoncalves
 */
public class SearchCriteriaImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchCriteriaImplTest.class);

    public SearchCriteriaImplTest() {
    }

    private static String convertElementToString(Element elem) throws TransformerException {

        TransformerFactory transFactory = TransformerFactory.newInstance();
        transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(elem), new StreamResult(buffer));
        return buffer.toString();
    }

    /**
     * Test of asXml method, of class SearchCriteriaImpl.
     */
    @Test
    public void testAsXml() throws TransformerException {

        String patientId = "23q2e";
        String docId = "29846534324.123453";
        SearchCriteria sc = new SearchCriteriaImpl();
        sc.add(Criteria.PatientId, patientId);
        sc.add(Criteria.DocumentId, docId);
        Document doc = sc.asXml();
        Element e = doc.getDocumentElement();
        String str = convertElementToString(e);
        LOGGER.info("testAsXml(): '{}'", str);
    }
}
