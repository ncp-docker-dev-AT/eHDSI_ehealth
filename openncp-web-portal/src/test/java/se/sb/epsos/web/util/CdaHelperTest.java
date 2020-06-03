package se.sb.epsos.web.util;

import org.apache.wicket.model.StringResourceModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import se.sb.epsos.shelob.ws.client.jaxws.EpsosDocument;
import se.sb.epsos.web.model.Prescription;
import se.sb.epsos.web.model.PrescriptionRow;
import se.sb.epsos.web.service.MetaDocument;
import se.sb.epsos.web.service.mock.DocumentCatalog;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author andreas
 */
public class CdaHelperTest {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    /**
     * Test of parsePrescriptionFromDocument method, of class CdaHelper.
     */
    @Test
    public void testParsePrescriptionFromDocument() throws ParserConfigurationException, SAXException, IOException {

        byte[] cdaBytes = DocumentCatalog.get("EP.1.1.1.CDA");
        assert (cdaBytes != null && cdaBytes.length > 0);
        Prescription prescription = new Prescription(new MetaDocument("", "", new EpsosDocument()),
                cdaBytes, new EpsosDocument());
        CdaHelper cdaHelper = new CdaHelper();
        cdaHelper.parsePrescriptionFromDocument(prescription);
        assert (prescription.getPerformer().equals("Dr. Läkare, Lars"));
        assert ("Medical doctors".equals(prescription.getProfession()));
        assert (prescription.getAddress().startsWith("V\u00e5rdcentralen TC"));
        assert (prescription.getAddress().startsWith("V\u00e5rdcentralen TC"));
        assert (prescription.getRows() != null);
        assert (prescription.getRows().size() == 1);

        PrescriptionRow row = prescription.getRows().get(0);
        assert (row.getDescription().startsWith("A10AB01 - insulin (human)"));
        assert (row.getIngredient().get(0).getActiveIngredient().equals("A10AB01 - insulin (human)"));
        assert (row.getIngredient().get(0).getStrength().equals("100 IU/ml"));
        assert (row.getPackageSize().getQuantityValue().equals("3"));
        assert (row.getPackageSize().getQuantityUnit().equals("ml"));
        assert (row.getNbrPackages().getQuantityValue().equals("3"));
        assert (row.getFormCode().equals("SOINCA"));
        assert (row.getFormName().equals("Injektionsvätska, lösning, cylinderampull"));
        assert (row.getStartDate().equals("2011-04-07"));
        assert (row.getEndDate().equals("2012-04-07"));
        assert (row.getFrequency().equals("Unknown"));
        assert (row.getDosage().equals("Unknown"));
        assert (row.getProductName().equals("Actrapid® Penfill®"));
        assert (row.getRoute().equals(""));
        assert (row.getProductId().equals("013509"));
        assert (row.getPrescriptionIdExtension().equals("1.133427"));
        assert (row.getSubstitutionPermittedText().equals(new StringResourceModel("prescription.substitute.null",
                null, "").getString()));
        assert (!row.isSubstitutionPermitted());
    }
}
