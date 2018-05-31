package eu.europa.ec.sante.ehdsi.gazelle.validation;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.epsos.validation.datamodel.common.ObjectType;
import eu.epsos.validation.datamodel.hl7v3.Hl7v3Schematron;
import eu.epsos.validation.datamodel.saml.AssertionSchematron;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.TransformerException;
import java.nio.charset.StandardCharsets;

public class OpenNCPValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNCPValidation.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    private static final String MSG_REMOTE_VALIDATION_DISABLED = "Remote Validation disabled";

    private OpenNCPValidation() {
    }

    /**
     * @param document
     * @param validator
     * @param ncpSide
     * @return
     */
    public static boolean validateAuditMessage(String document, String validator, NcpSide ncpSide) {

        if (isValidationEnable()) {

            AuditMessageValidator auditMessageValidator = GazelleValidatorFactory.getAuditMessageValidator();
            return auditMessageValidator.validateDocument(document, validator, ncpSide);

        }
        LOGGER.info(MSG_REMOTE_VALIDATION_DISABLED);
        return false;
    }

    /**
     * @param assertion
     * @param ncpSide
     * @return
     */
    public static boolean validateHCPAssertion(Assertion assertion, NcpSide ncpSide) {

        LOGGER.info("validate HCP Assertion...");
        return validateAssertion(assertion, AssertionSchematron.EPSOS_HCP_IDENTITY_ASSERTION.toString(), ncpSide);
    }

    /**
     * @param assertion
     * @param ncpSide
     * @return
     */
    public static boolean validateTRCAssertion(Assertion assertion, NcpSide ncpSide) {

        LOGGER.info("validate TRC Assertion...");
        return validateAssertion(assertion, AssertionSchematron.EPSOS_TRC_ASSERTION.toString(), ncpSide);
    }

    /**
     * @param assertion
     * @param schematron
     * @param ncpSide
     * @return
     */
    private static boolean validateAssertion(Assertion assertion, String schematron, NcpSide ncpSide) {

        if (isValidationEnable()) {

            LOGGER.info("[Validation Service: Assertion Validator]");
            SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
            String base64;
            try {
                base64 = DatatypeConverter.printBase64Binary(XMLUtil.prettyPrint(assertion.getDOM()).getBytes(StandardCharsets.UTF_8));
                LOGGER_CLINICAL.info("Assertion:\n'{}'", base64);

            } catch (TransformerException e) {
                LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
                return false;
            }
            return schematronValidator.validateObject(base64, schematron, schematron, ObjectType.ASSERTION.toString(), ncpSide);
        }
        LOGGER.info(MSG_REMOTE_VALIDATION_DISABLED);
        return false;
    }

    /**
     * @param request
     * @param validator
     * @param ncpSide
     * @return
     */
    public static boolean validatePatientDemographic(String request, String validator, NcpSide ncpSide) {

        if (isValidationEnable()) {

            LOGGER.info("[Validation Service: XCPD Validator]");
            SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
            String base64 = DatatypeConverter.printBase64Binary(request.getBytes(StandardCharsets.UTF_8));
            LOGGER_CLINICAL.info("XCPD:\n'{}'", base64);

            return schematronValidator.validateObject(base64, validator, validator, Hl7v3Schematron.checkSchematron(validator).getObjectType().toString(), ncpSide);
        }
        LOGGER.info(MSG_REMOTE_VALIDATION_DISABLED);
        return false;
    }

    /**
     * @param request
     * @param validator
     * @param ncpSide
     * @return
     */
    public static boolean validateCrossCommunityAccess(String request, String validator, NcpSide ncpSide) {

        if (isValidationEnable()) {

            LOGGER.info("[Validation Service: XCA Validator]");
            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();

            return xdsValidator.validateDocument(request, validator, ncpSide);
        }
        LOGGER.info(MSG_REMOTE_VALIDATION_DISABLED);
        return false;
    }

    /**
     * @param request
     * @param validator
     * @param ncpSide
     * @return
     */
    public static boolean validateXDRMessage(String request, String validator, NcpSide ncpSide) {

        if (isValidationEnable()) {

            LOGGER.info("[Validation Service: XDR Validator]");
            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();

            return xdsValidator.validateDocument(request, validator, ncpSide);
        }
        LOGGER.info(MSG_REMOTE_VALIDATION_DISABLED);
        return false;
    }

    /**
     * @param cda
     * @param cdaModel
     * @param ncpSide
     * @return
     */
    public static boolean validateCdaDocument(String cda, String cdaModel, NcpSide ncpSide) {

        if (isValidationEnable()) {
            LOGGER.info("[Validation Service: CDA Validator]");
            GazelleValidatorFactory.getCdaValidator().validateDocument(cda, cdaModel, ncpSide);
        }
        LOGGER.info(MSG_REMOTE_VALIDATION_DISABLED);
        return false;
    }

    /**
     * @return
     */
    private static boolean isValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.new");
    }
}
