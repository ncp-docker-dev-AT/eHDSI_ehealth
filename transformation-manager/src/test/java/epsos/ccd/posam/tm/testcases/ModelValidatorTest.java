package epsos.ccd.posam.tm.testcases;

import epsos.ccd.posam.tm.util.ModelBasedValidator;
import epsos.ccd.posam.tm.util.ModelValidatorResult;
import epsos.ccd.posam.tm.util.TMConstants;
import net.ihe.gazelle.epsos.utils.ProjectDependencies;
import net.ihe.gazelle.epsos.validator.GazelleValidatorCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.List;

public class ModelValidatorTest extends TBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelValidatorTest.class);

    public void testValidator() {
        ProjectDependencies.CDA_XSD = "src\\test\\resources\\validator_res\\xsd\\CDA.xsd";
        ProjectDependencies.CDA_EPSOS_XSD = "src\\test\\resources\\validator_res\\xsd\\CDA_extended.xsd";
        ProjectDependencies.CDA_XSL_TRANSFORMER = "src\\test\\resources\\validator_res\\mbvalidatorDetailedResult.xsl";
        ProjectDependencies.VALUE_SET_REPOSITORY = "src\\test\\resources\\validator_res\\valueSets\\";

        try {
            File docFile = new File("src\\test\\resources\\samples\\schPassed\\PatientSummary-pivot.xml");
            char[] buffer = new char[(int) docFile.length()];

            FileReader fr = new FileReader(docFile);
            fr.read(buffer);
            fr.close();

            String xmlStr = new String(buffer);

            ModelBasedValidator mdaValidator = ModelBasedValidator.getInstance();

            ModelValidatorResult result = mdaValidator.validate(xmlStr, TMConstants.PATIENT_SUMMARY3, true);
            assertNotNull(result);
            LOGGER.info("Result: '{}'", result);
        } catch (Exception e) {
            LOGGER.error("{}: '{}'", e.getClass(), e.getMessage(), e);
        }
    }

    public void testSupportedDocs() {
        List<String> listOfValidators = GazelleValidatorCore.getListOfValidators();
        LOGGER.info("Validator List: '{}'", listOfValidators);
    }
}
