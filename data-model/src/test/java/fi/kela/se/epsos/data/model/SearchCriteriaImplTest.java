package fi.kela.se.epsos.data.model;

import fi.kela.se.epsos.data.model.SearchCriteria.Criteria;
import org.junit.Test;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author jgoncalves
 */
public class SearchCriteriaImplTest {

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
     * Test of testAddPatientId() method, of class SearchCriteriaImpl.
     */
    @Test
    public void testAddPatientId() {


        String patientId = "0QROL2G9M";
        SearchCriteria criteria = new SearchCriteriaImpl();
        criteria.addPatientId(patientId);
        assertEquals("0QROL2G9M", criteria.getPatientId().getExtension());
        assertNull(criteria.getPatientId().getRoot());

        patientId = "1501987681058^^^&2.16.17.710.822.1000.990.1&ISO";
        criteria.addPatientId(patientId);
        assertEquals("2.16.17.710.822.1000.990.1", criteria.getPatientId().getRoot());
        assertEquals("1501987681058", criteria.getPatientId().getExtension());

        patientId = "1501987681058^^^&amp;2.16.17.710.822.1000.990.1&amp;ISO";
        criteria.addPatientId(patientId);
        assertEquals("2.16.17.710.822.1000.990.1", criteria.getPatientId().getRoot());
        assertEquals("1501987681058", criteria.getPatientId().getExtension());
    }


    /**
     * Test of testAsXml() method, of class SearchCriteriaImpl.
     *
     * @throws TransformerException - If XML fragment cannot be parsed properly
     */
    @Test
    public void testAsXml() throws TransformerException {

        String patientId = "23q2e";
        String docId = "29846534324.123453";
        SearchCriteria searchCriteria = new SearchCriteriaImpl();
        searchCriteria.add(Criteria.PATIENT_ID, patientId);
        searchCriteria.add(Criteria.DOCUMENT_ID, docId);
        Document doc = searchCriteria.asXml();
        Element element = doc.getDocumentElement();
        String str = convertElementToString(element);
        assertEquals("<SearchCriteria><PatientId>23q2e</PatientId><DocumentId>29846534324.123453</DocumentId></SearchCriteria>", str);
    }
}
