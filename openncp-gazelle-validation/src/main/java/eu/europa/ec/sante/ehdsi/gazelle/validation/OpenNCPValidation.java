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

        LOGGER.info("Audit Message Validation: '{}'-'{}'", eventType, ncpSide.getName());
        String validator = ValidatorUtil.obtainAuditModel(eventType, ncpSide);

        String xmlResult = "";
        if (isRemoteValidationEnable()) {
            AuditMessageValidator auditMessageValidator = GazelleValidatorFactory.getAuditMessageValidator();
            xmlResult = auditMessageValidator.validateDocument(document, validator);
        }
        if (StringUtils.isNotBlank(xmlResult)) {

            return ReportBuilder.build(validator, ObjectType.AUDIT.toString(), document, DetailedResultUnMarshaller.unmarshal(xmlResult),
                    xmlResult, ncpSide);
        } else {
            return ReportBuilder.build(validator, ObjectType.AUDIT.toString(), document, null, null, ncpSide);
        }
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
        String base64 = "";
        String xmlResult = "";

        if (isRemoteValidationEnable()) {
            SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
            try {
                base64 = DatatypeConverter.printBase64Binary(XMLUtil.prettyPrint(assertion.getDOM()).getBytes(StandardCharsets.UTF_8));
                xmlResult = schematronValidator.validateObject(base64, schematron, schematron);

            } catch (TransformerException e) {
                LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
                return false;
            } catch (GazelleValidationException e) {
                LOGGER.error("GazelleValidationException: '{}'", e.getMessage());
                return false;
            }
        }
        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            return ReportBuilder.build(schematron, ObjectType.ASSERTION.toString(), base64, detailedResult, xmlResult, ncpSide);
        } else {
            return ReportBuilder.build(schematron, ObjectType.ASSERTION.toString(), base64, null, null, ncpSide);
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
        String xmlResult = "";
        String base64 = "";

        if (isRemoteValidationEnable()) {
            SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
            base64 = DatatypeConverter.printBase64Binary(request.getBytes(StandardCharsets.UTF_8));
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

        LOGGER.info("[Validation Service: XCA Validator]");
        String xmlResult = "";

        XdsModel xdsModel = ValidatorUtil.obtainModelXca(message);
        if (isRemoteValidationEnable()) {
            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
            xmlResult = xdsValidator.validateDocument(message, xdsModel.getValidatorName());
        }

        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            return ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), message, detailedResult, xmlResult, ncpSide);
        } else {
            return ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), message, null, null, ncpSide);
        }
    }

    /**
     * @param request
     * @param ncpSide
     * @return
     */
    public static boolean validateXDRMessage(String request, NcpSide ncpSide) {


        LOGGER.info("[Validation Service: XDR Validator]");
        String xmlResult = "";
        XdsModel xdsModel = ValidatorUtil.obtainModelXdr(request);
        if (isRemoteValidationEnable()) {

            XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
            xmlResult = xdsValidator.validateDocument(request, xdsModel.getValidatorName());
        }

        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            return ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), request, detailedResult, xmlResult, ncpSide);
        } else {
            return ReportBuilder.build(xdsModel.getValidatorName(), xdsModel.getObjectType(), request, null, null, ncpSide);
        }
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
        String xmlResult = "";
        String cdaModel = ValidatorUtil.obtainCdaModel(classCode, isPivot);
        if (isRemoteValidationEnable()) {
            xmlResult = GazelleValidatorFactory.getCdaValidator().validateDocument(cda, cdaModel, ncpSide);

        }
        if (StringUtils.isNotBlank(xmlResult)) {
            DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
            return ReportBuilder.build(cdaModel, ObjectType.CDA.toString(), cda, detailedResult, xmlResult, ncpSide);
        } else {
            return ReportBuilder.build(cdaModel, ObjectType.CDA.toString(), cda, null, null, ncpSide);
        }
    }

    /**
     * @return
     */
    public static boolean isValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation") &&
                !StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD");
    }

    public static boolean isRemoteValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.remote");
    }
}
