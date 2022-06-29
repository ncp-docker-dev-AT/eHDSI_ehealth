package epsos.ccd.posam.tm.testcases;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;


/**  
 * TM Junit test suite
 * @author Frantisek Rudik
 * @author Organization: Posam
 * @author mail:frantisek.rudik@posam.sk
 * @version 1.10, 2010, 20 October
 */
@Ignore("Test to revise - Exclude unit test from test execution")
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("TransformationManager");
				
		suite.addTestSuite(ConfigurationTest.class);
		suite.addTestSuite(CDAXPathTest.class);		
		suite.addTestSuite(ToEpsosPivotPositiveTest.class);
		suite.addTestSuite(ToEpsosPivotNegativeTest.class);
		suite.addTestSuite(TranslatePositiveTest.class);
		suite.addTestSuite(TranslateNegativeTest.class);
		suite.addTestSuite(TranscodeTranslateTest.class);
		suite.addTestSuite(SchematronTest.class);
		
		return suite;
	}
}