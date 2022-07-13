package epsos.ccd.posam.tm.testcases;

import java.util.List;

import eu.europa.ec.sante.ehdsi.constant.error.ITMTSAMEror;
import eu.europa.ec.sante.ehdsi.constant.error.TMError;
import org.junit.Ignore;
import org.w3c.dom.Document;

import epsos.ccd.posam.tm.response.TMResponseStructure;

/**
 * Negative test scenarios for method translate 
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.4, 2010, 20 October
 */
@Ignore("Test to revise - Exclude unit test from test execution")
public class TranslateNegativeTest extends TBase{

	public void testTranslateNull() {
		TMResponseStructure response = null;
//		TMResponseStructure response = tmService.translate(null, null);
		assertNotNull(response);
		assertFalse(response.isStatusSuccess());

		List<ITMTSAMEror> errors = response.getErrors();
		assertNotNull(errors);
		assertTrue(errors.contains(TMError.ERROR_NULL_INPUT_DOCUMENT));
	}

	public void testTranslateNotValidDoc() {
		Document notValidDocument = getInvalidDocument();
		assertNotNull(notValidDocument);

		TMResponseStructure response = null;
//		TMResponseStructure response = tmService.translate(notValidDocument, null);
		assertNotNull(response);
		assertFalse(response.isStatusSuccess());
		
		List<ITMTSAMEror> errors =  response.getErrors();
		assertNotNull(errors);
	}	
}
