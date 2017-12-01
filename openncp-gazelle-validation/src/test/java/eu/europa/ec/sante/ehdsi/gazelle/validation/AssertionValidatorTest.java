package eu.europa.ec.sante.ehdsi.gazelle.validation;

import org.junit.Assert;
import org.junit.Test;

public class AssertionValidatorTest {

    @Test
    public void testValidateDocument() {
        AssertionValidator assertionValidator = GazelleValidatorFactory.getAssertionValidator();
        Assert.assertTrue(assertionValidator.validateDocument("test", "test"));
    }
}
