package eu.europa.ec.sante.ehdsi.gazelle.validation;

import eu.epsos.validation.datamodel.common.NcpSide;
import eu.europa.ec.sante.ehdsi.gazelle.validation.reporting.ReportBuilder;
import eu.europa.ec.sante.ehdsi.gazelle.validation.util.DetailedResultUnMarshaller;
import eu.europa.ec.sante.ehdsi.gazelle.validation.util.ObjectType;
import eu.europa.ec.sante.ehdsi.gazelle.validation.util.XdsModel;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import net.ihe.gazelle.jaxb.result.sante.DetailedResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.opensaml.saml.saml2.core.Assertion;
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

        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("Audit Message Validation: '{}'-'{}'", eventType, ncpSide.getName());
        String validator = ValidatorUtil.obtainAuditModel(eventType, ncpSide);

        String xmlResult = "";
        if (isRemoteValidationEnable()) {
            AuditMessageValidator auditMessageValidator = GazelleValidatorFactory.getAuditMessageValidator();
            xmlResult = auditMessageValidator.validateDocument(document, validator);
        }
        boolean validated;
        if (StringUtils.isNotBlank(xmlResult)) {

            validated = ReportBuilder.build(validator, ObjectType.AUDIT.toString(), document, DetailedResultUnMarshaller.unmarshal(xmlResult),
                    xmlResult, ncpSide);
        } else {
            validated = ReportBuilder.build(validator, ObjectType.AUDIT.toString(), document, null, null, ncpSide);
        }
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @param assertion
     * @param ncpSide
     * @return
     */
    public static boolean validateHCPAssertion(Assertion assertion, NcpSide ncpSide) {

        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("validate HCP Assertion...");
        boolean validated = validateAssertion(assertion, ValidatorUtil.EHDSI_ASSERTION_HCP_IDENTITY, ncpSide);
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @param assertion
     * @param ncpSide
     * @return
     */
    public static boolean validateTRCAssertion(Assertion assertion, NcpSide ncpSide) {

        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("validate TRC Assertion...");
        boolean validated = validateAssertion(assertion, ValidatorUtil.EHDSI_ASSERTION_TRC, ncpSide);
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @param assertion
     * @param schematron
     * @param ncpSide
     * @return
     */
    private static boolean validateAssertion(Assertion assertion, String schematron, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: Assertion Validator]");
        try {
            String base64 = DatatypeConverter.printBase64Binary(XMLUtil.prettyPrint(assertion.getDOM()).getBytes(StandardCharsets.UTF_8));
            String xmlResult = "";

            if (isRemoteValidationEnable()) {

                SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
                xmlResult = schematronValidator.validateObject(base64, schematron, schematron);

            }
            if (StringUtils.isNotBlank(xmlResult)) {
                DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
                return ReportBuilder.build(schematron, ObjectType.ASSERTION.toString(), base64, detailedResult, xmlResult, ncpSide);
            } else {
                return ReportBuilder.build(schematron, ObjectType.ASSERTION.toString(), base64, null, null, ncpSide);
            }
        } catch (GazelleValidationException e) {
            LOGGER.error("GazelleValidationException: '{}'", e.getMessage());
            return false;
        } catch (TransformerException e) {
            LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
            return false;
        }
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validatePatientDemographicRequest(String request, NcpSide ncpSide) {
        StopWatch watch = new StopWatch();
        watch.start();
        boolean validated = validatePatientDemographic(request, ValidatorUtil.EHDSI_ID_SERVICE_REQUEST, ObjectType.XCPD_QUERY_REQUEST, ncpSide);
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validatePatientDemographicResponse(String request, NcpSide ncpSide) {
        StopWatch watch = new StopWatch();
        watch.start();
        boolean validated = validatePatientDemographic(request, ValidatorUtil.EHDSI_ID_SERVICE_RESPONSE, ObjectType.XCPD_QUERY_RESPONSE, ncpSide);
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
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
        String xmlResult = "";
        String base64 = DatatypeConverter.printBase64Binary(request.getBytes(StandardCharsets.UTF_8));

        if (isRemoteValidationEnable()) {
            SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
            try {
                xmlResult = schematronValidator.validateObject(base64, validator, validator);
            } catch (GazelleValidationException e) {
                LOGGER.error("GazelleValidationException: '{}'", e.getMessage());
                return false;
            }
        }

        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            return ReportBuilder.build(validator, objectType.toString(), base64, detailedResult, xmlResult, ncpSide);
        } else {
            return ReportBuilder.build(validator, objectType.toString(), base64, null, null, ncpSide);
        }
    }

    /**
     * @param message
     * @param ncpSide
     * @return
     */
    public static boolean validateCrossCommunityAccess(String message, NcpSide ncpSide) {
        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("[Validation Service: XCA Validator]");
        String xmlResult = "";

        XdsModel xdsModel = ValidatorUtil.obtainModelXca(message);
        if (isRemoteValidationEnable()) {
            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
            xmlResult = xdsValidator.validateDocument(message, xdsModel.getValidatorName());
        }
        boolean validated;
        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            validated = ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), message, detailedResult, xmlResult, ncpSide);
        } else {
            validated = ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), message, null, null, ncpSide);
        }
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validateXDRMessage(String request, NcpSide ncpSide) {

        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("[Validation Service: XDR Validator]");
        String xmlResult = "";
        XdsModel xdsModel = ValidatorUtil.obtainModelXdr(request);
        if (isRemoteValidationEnable()) {

            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
            xmlResult = xdsValidator.validateDocument(request, xdsModel.getValidatorName());
        }
        boolean validated;
        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            validated = ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), request, detailedResult, xmlResult, ncpSide);
        } else {
            validated = ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), request, null, null, ncpSide);
        }
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @param cda
     * @param ncpSide
     * @param classCode
     * @param isPivot
     * @return
     */
    public static boolean validateCdaDocument(String cda, NcpSide ncpSide, String classCode, boolean isPivot) {

        StopWatch watch = new StopWatch();
        watch.start();
        LOGGER.info("[Validation Service: CDA Validator]");
        String xmlResult = "";
        boolean isScannedDocument = cda.contains("nonXMLBody");
        String cdaModel = ValidatorUtil.obtainCdaModel(classCode, isPivot, isScannedDocument);
        if (isRemoteValidationEnable()) {
            xmlResult = GazelleValidatorFactory.getCdaValidator().validateDocument(cda, cdaModel, ncpSide);

        }
        boolean validated;
        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            validated = ReportBuilder.build(cdaModel, ObjectType.CDA.toString(), cda, detailedResult, xmlResult, ncpSide);
        } else {
            validated = ReportBuilder.build(cdaModel, ObjectType.CDA.toString(), cda, null, null, ncpSide);
        }
        watch.stop();
        LOGGER.debug("Validation executed in: '{}ms'", watch.getTime());
        return validated;
    }

    /**
     * @return
     */
    public static boolean isValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation") &&
                !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name());
    }

    public static boolean isRemoteValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.remote");
    }
}
