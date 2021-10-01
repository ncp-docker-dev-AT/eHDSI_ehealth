package epsos.ccd.posam.tm.testcases;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the utility method for retrieving the LTR available languages
 *
 * @author Marcelo Fonseca<code> - marcelo.fonseca@iuz.pt</code>
 */
@Ignore("Test to revise - Exclude unit test from test execution")
public class LtrAvailableLanguagesTest extends TBase {

    private final Logger logger = LoggerFactory.getLogger(LtrAvailableLanguagesTest.class);

    public void testLtrAvailableLanguages() {

        List<String> ltrLanguages = new ArrayList<>();
//        List<String> ltrLanguages = tmService.getLtrLanguages();

        for (String s : ltrLanguages) {
            logger.info("Available languages: '{}'", s);
        }

        assertEquals(4, ltrLanguages.size());
        assertTrue(ltrLanguages.contains("en"));
    }
}
