package eu.europa.ec.sante.ehdsi.gazelle.validation;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ReportBuilder;
import eu.europa.ec.sante.ehdsi.gazelle.validation.util.DetailedResultUnMarshaller;
import eu.europa.ec.sante.ehdsi.gazelle.validation.util.ObjectType;
import eu.europa.ec.sante.ehdsi.gazelle.validation.util.XdsModel;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.XMLUtil;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.TransformerException;
import java.nio.charset.StandardCharsets;

public class OpenNCPValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNCPValidation.class);

    private OpenNCPValidation() {
    }

    /**
     * @param document
     * @param eventType
     * @param ncpSide
     * @return
     */
    public static boolean validateAuditMessage(String document, String eventType, NcpSide ncpSide) {

        String validator = ValidatorUtil.obtainAuditModel(eventType, ncpSide);
        AuditMessageValidator auditMessageValidator = GazelleValidatorFactory.getAuditMessageValidator();
        String xmlResult = auditMessageValidator.validateDocument(document, validator);
        ReportBuilder.build(validator, ObjectType.AUDIT.toString(), document, DetailedResultUnMarshaller.unmarshal(xmlResult),
                xmlResult, ncpSide);
        return true;
    }

    /**
     * @param assertion
     * @param ncpSide
     * @return
     */
    public static boolean validateHCPAssertion(Assertion assertion, NcpSide ncpSide) {

        LOGGER.info("validate HCP Assertion...");
        return validateAssertion(assertion, ValidatorUtil.EHDSI_ASSERTION_HCP_IDENTITY, ncpSide);
    }

    /**
     * @param assertion
     * @param ncpSide
     * @return
     */
    public static boolean validateTRCAssertion(Assertion assertion, NcpSide ncpSide) {

        LOGGER.info("validate TRC Assertion...");
        return validateAssertion(assertion, ValidatorUtil.EHDSI_ASSERTION_TRC, ncpSide);
    }

    /**
     * @param assertion
     * @param schematron
     * @param ncpSide
     * @return
     */
    private static boolean validateAssertion(Assertion assertion, String schematron, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: Assertion Validator]");
        SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
        String base64;

        try {
            base64 = DatatypeConverter.printBase64Binary(XMLUtil.prettyPrint(assertion.getDOM()).getBytes(StandardCharsets.UTF_8));
            String xmlResult = schematronValidator.validateObject(base64, schematron, schematron);
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            ReportBuilder.build(schematron, ObjectType.ASSERTION.toString(), base64, detailedResult, xmlResult, ncpSide);
            return true;

        } catch (TransformerException e) {
            LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
            return false;
        } catch (GazelleValidationException e) {
            LOGGER.error("GazelleValidationException: '{}'", e.getMessage());
            return false;
        }
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validatePatientDemographicRequest(String request, NcpSide ncpSide) {

        return validatePatientDemographic(request, ValidatorUtil.EHDSI_ID_SERVICE_REQUEST, ObjectType.XCPD_QUERY_REQUEST, ncpSide);
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validatePatientDemographicResponse(String request, NcpSide ncpSide) {

        return validatePatientDemographic(request, ValidatorUtil.EHDSI_ID_SERVICE_RESPONSE, ObjectType.XCPD_QUERY_RESPONSE, ncpSide);
    }

    /**
     * @param request
     * @param validator
     * @param objectType
     * @param ncpSide
     * @return
     */
    private static boolean validatePatientDemographic(String request, String validator, ObjectType objectType, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: XCPD Validator]");
        SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
        String base64 = DatatypeConverter.printBase64Binary(request.getBytes(StandardCharsets.UTF_8));

        try {
            String xmlResult = schematronValidator.validateObject(base64, validator, validator);
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            ReportBuilder.build(validator, objectType.toString(), base64, detailedResult, xmlResult, ncpSide);
            return true;

        } catch (GazelleValidationException e) {
            LOGGER.error("GazelleValidationException: '{}'", e.getMessage());
            return false;
        }
    }

    /**
     * @param message
     * @param ncpSide
     * @return
     */
    public static boolean validateCrossCommunityAccess(String message, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: XCA Validator]");
        XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
        XdsModel xdsModel = ValidatorUtil.obtainModelXca(message);
        String xmlResult = xdsValidator.validateDocument(message, xdsModel.getValidatorName());
        DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
        ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), message, detailedResult, xmlResult, ncpSide);
        return true;
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validateXDRMessage(String request, NcpSide ncpSide) {


        LOGGER.info("[Validation Service: XDR Validator]");
        String xmlResult = "";
        DetailedResult detailedResult;
        XdsModel xdsModel = ValidatorUtil.obtainModelXdr(request);
        if (isRemoteValidationEnable()) {

            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
            xmlResult = xdsValidator.validateDocument(request, xdsModel.getValidatorName());
        }

        if (StringUtils.isNotBlank(xmlResult)) {
            detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), request, detailedResult, xmlResult, ncpSide);
        } else {
            ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), request, null, null, ncpSide);
        }
        return true;
    }

    /**
     * @param cda
     * @param ncpSide
     * @param classCode
     * @param isPivot
     * @return
     */
    public static boolean validateCdaDocument(String cda, NcpSide ncpSide, String classCode, boolean isPivot) {

        LOGGER.info("[Validation Service: CDA Validator]");
        String cdaModel = ValidatorUtil.obtainCdaModel(classCode, isPivot);
        GazelleValidatorFactory.getCdaValidator().validateDocument(cda, cdaModel, ncpSide);
        return true;
    }

    /**
     * @return
     */
    public static boolean isValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation");
    }

    public static boolean isRemoteValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.remote");
    }
}
