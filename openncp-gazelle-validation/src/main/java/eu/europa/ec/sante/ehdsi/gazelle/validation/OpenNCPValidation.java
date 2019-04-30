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
    private static final String MSG_VALIDATION_EXECUTION = "Remote validation executed in: '{}ms'";

    private OpenNCPValidation() {
    }

    /**
     * @param document
     * @param eventType
     * @param ncpSide
     */
    public static void validateAuditMessage(String document, String eventType, NcpSide ncpSide) {

        LOGGER.info("Audit Message Validation: '{}'-'{}'", eventType, ncpSide.getName());
        String validator = ValidatorUtil.obtainAuditModel(eventType, ncpSide);

        if (isRemoteValidationEnable()) {

            new Thread(() -> {
                StopWatch watch = new StopWatch();
                watch.start();
                AuditMessageValidator auditMessageValidator = GazelleValidatorFactory.getAuditMessageValidator();
                String xmlResult = auditMessageValidator.validateDocument(document, validator);
                ReportBuilder.build(ReportBuilder.formatDate(), validator, ObjectType.AUDIT.toString(), document,
                        DetailedResultUnMarshaller.unmarshal(xmlResult), xmlResult, ncpSide);
                watch.stop();
                LOGGER.info(MSG_VALIDATION_EXECUTION, watch.getTime());
            }).start();
        } else {
            ReportBuilder.build(ReportBuilder.formatDate(), validator, ObjectType.AUDIT.toString(), document, ncpSide);
        }
    }

    /**
     * @param assertion
     * @param ncpSide
     */
    public static void validateHCPAssertion(Assertion assertion, NcpSide ncpSide) {

        LOGGER.info("validate HCP Assertion...");
        validateAssertion(assertion, ValidatorUtil.EHDSI_ASSERTION_HCP_IDENTITY, ncpSide);
    }

    /**
     * @param assertion
     * @param ncpSide
     */
    public static void validateTRCAssertion(Assertion assertion, NcpSide ncpSide) {

        LOGGER.info("validate TRC Assertion...");
        validateAssertion(assertion, ValidatorUtil.EHDSI_ASSERTION_TRC, ncpSide);
    }

    /**
     * @param assertion
     * @param schematron
     * @param ncpSide
     */
    private static void validateAssertion(Assertion assertion, String schematron, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: Assertion Validator]");
        try {
            String base64 = DatatypeConverter.printBase64Binary(XMLUtil.prettyPrint(assertion.getDOM()).getBytes(StandardCharsets.UTF_8));

            if (isRemoteValidationEnable()) {

                new Thread(() -> {
                    StopWatch watch = new StopWatch();
                    watch.start();
                    String xmlResult;
                    SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
                    xmlResult = schematronValidator.validateObject(base64, schematron, schematron);
                    DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
                    ReportBuilder.build(ReportBuilder.formatDate(), schematron, ObjectType.ASSERTION.toString(), base64, detailedResult, xmlResult, ncpSide);
                    watch.stop();
                    LOGGER.info(MSG_VALIDATION_EXECUTION, watch.getTime());
                }).start();

            } else {
                ReportBuilder.build(ReportBuilder.formatDate(), schematron, ObjectType.ASSERTION.toString(), base64, ncpSide);
            }
        } catch (TransformerException e) {
            LOGGER.error("TransformerException: '{}'", e.getMessage(), e);
        }
    }

    /**
     * @param request
     * @param ncpSide
     */
    public static void validatePatientDemographicRequest(String request, NcpSide ncpSide) {

        validatePatientDemographic(request, ValidatorUtil.EHDSI_ID_SERVICE_REQUEST, ObjectType.XCPD_QUERY_REQUEST, ncpSide);
    }

    /**
     * @param request
     * @param ncpSide
     */
    public static void validatePatientDemographicResponse(String request, NcpSide ncpSide) {

        validatePatientDemographic(request, ValidatorUtil.EHDSI_ID_SERVICE_RESPONSE, ObjectType.XCPD_QUERY_RESPONSE, ncpSide);
    }

    /**
     * @param request
     * @param validator
     * @param objectType
     * @param ncpSide
     */
    private static void validatePatientDemographic(String request, String validator, ObjectType objectType, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: XCPD Validator]");
        String base64 = DatatypeConverter.printBase64Binary(request.getBytes(StandardCharsets.UTF_8));

        if (isRemoteValidationEnable()) {

            new Thread(() -> {
                StopWatch watch = new StopWatch();
                watch.start();
                SchematronValidator schematronValidator = GazelleValidatorFactory.getSchematronValidator();
                String xmlResult = schematronValidator.validateObject(base64, validator, validator);
                DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
                ReportBuilder.build(ReportBuilder.formatDate(), validator, objectType.toString(), base64, detailedResult, xmlResult, ncpSide);
                watch.stop();
                LOGGER.info(MSG_VALIDATION_EXECUTION, watch.getTime());
            }).start();
        } else {

            ReportBuilder.build(ReportBuilder.formatDate(), validator, objectType.toString(), base64, ncpSide);
        }
    }

    /**
     * @param message
     * @param ncpSide
     */
    public static void validateCrossCommunityAccess(String message, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: XCA Validator]");
        XdsModel xdsModel = ValidatorUtil.obtainModelXca(message);
        validateXDSMessage(message, xdsModel, ncpSide);
    }

    /**
     * @param request
     * @param ncpSide
     */
    public static void validateXDRMessage(String request, NcpSide ncpSide) {

        LOGGER.info("[Validation Service: XDR Validator]");
        XdsModel xdsModel = ValidatorUtil.obtainModelXdr(request);
        validateXDSMessage(request, xdsModel, ncpSide);
    }

    private static void validateXDSMessage(String message, XdsModel xdsModel, NcpSide ncpSide) {

        if (isRemoteValidationEnable()) {

            new Thread(() -> {
                StopWatch watch = new StopWatch();
                watch.start();
                XdsValidator xdsValidator = GazelleValidatorFactory.getXdsValidator();
                String xmlResult = xdsValidator.validateDocument(message, xdsModel.getValidatorName());
                DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
                ReportBuilder.build(ReportBuilder.formatDate(), xdsModel.getValidatorName(), xdsModel.getObjectType(), message, detailedResult, xmlResult, ncpSide);
                watch.stop();
                LOGGER.info(MSG_VALIDATION_EXECUTION, watch.getTime());
            }).start();
        } else {
            ReportBuilder.build(ReportBuilder.formatDate(), xdsModel.getValidatorName(), xdsModel.getObjectType(), message, ncpSide);
        }
    }

    /**
     * @param cda
     * @param ncpSide
     * @param classCode
     * @param isPivot
     */
    public static void validateCdaDocument(String cda, NcpSide ncpSide, String classCode, boolean isPivot) {

        LOGGER.info("[Validation Service: CDA Validator]");
        boolean isScannedDocument = cda.contains("nonXMLBody");
        String cdaModel = ValidatorUtil.obtainCdaModel(classCode, isPivot, isScannedDocument);

        if (isRemoteValidationEnable()) {

            new Thread(() -> {
                StopWatch watch = new StopWatch();
                watch.start();
                String xmlResult = GazelleValidatorFactory.getCdaValidator().validateDocument(cda, cdaModel, ncpSide);
                DetailedResult detailedResult = DetailedResultUnMarshaller.unmarshal(xmlResult);
                ReportBuilder.build(ReportBuilder.formatDate(), cdaModel, ObjectType.CDA.toString(), cda, detailedResult, xmlResult, ncpSide);
                watch.stop();
                LOGGER.info(MSG_VALIDATION_EXECUTION, watch.getTime());
            }).start();
        } else {
            ReportBuilder.build(ReportBuilder.formatDate(), cdaModel, ObjectType.CDA.toString(), cda, ncpSide);
        }
    }

    /**
     * @return
     */
    public static boolean isValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation") &&
                !StringUtils.equals(System.getProperty(OpenNCPConstants.SERVER_EHEALTH_MODE), ServerMode.PRODUCTION.name());
    }

    /**
     * @return
     */
    public static boolean isRemoteValidationEnable() {

        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.remote");
    }
}
