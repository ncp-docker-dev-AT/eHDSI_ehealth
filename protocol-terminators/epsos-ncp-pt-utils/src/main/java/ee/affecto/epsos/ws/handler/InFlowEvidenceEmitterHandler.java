package ee.affecto.epsos.ws.handler;

import epsos.ccd.gnomon.auditmanager.EventOutcomeIndicator;
import eu.epsos.util.EvidenceUtils;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.XMLUtil;

/**
 * InFlowEvidenceEmitterHandler - Generates all NRRs
 * Currently supporting the generation of evidences in the following cases:
 * NCP-B receives from Portal
 * NCP-A receives from NCP-B
 * NCP-B receives from NCP-A (left commented as the Evidence Emitter CP does not mandate generation of evidences on the response)
 *
 * @author jgoncalves
 */
public class InFlowEvidenceEmitterHandler extends AbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(InFlowEvidenceEmitterHandler.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    @Override
    public Handler.InvocationResponse invoke(MessageContext msgContext) {

        logger.info("[NRR] InFlow Evidence Emitter handler is executing");
        EvidenceEmitterHandlerUtils evidenceEmitterHandlerUtils = new EvidenceEmitterHandlerUtils();
        //HandlerUtil.printClientCertificateDetails(msgContext);
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
        SOAPBody soapBody = msgContext.getEnvelope().getBody();

        /* I'll leave this here as it might be useful in the future */
//
//        if (soapHeader != null) {
//            Iterator<?> blocks = soapHeader.examineAllHeaderBlocks();
//            logger.debug("Iterating over soap headers");
//            while (blocks.hasNext()) {
//                logger.debug("Processing header");
//                SOAPHeaderBlock block = (SOAPHeaderBlock) blocks.next();
//                if (logger.isDebugEnabled()) {
//                    logger.debug(block.toString());
//                }
//                block.setProcessed();
//            }
//        }
//
//        logger.debug("LOGGING TEST VALUES");
//        logger.debug("MessageContext properties: '{}'", msgContext.getProperties());
//        logger.debug("MessageContext messageID: '{}'", msgContext.getMessageID());
//
//        SessionContext sessionCtx = msgContext.getSessionContext();
//        if (sessionCtx != null) {
//            logger.debug("SessionContext CookieID: '{}'", sessionCtx.getCookieID());
//        } else {
//            logger.debug("SessionContext is null!");
//        }
//
//        OperationContext operationCtx = msgContext.getOperationContext();
//        if (operationCtx != null) {
//            logger.debug("OperationContext operationName: '{}'", operationCtx.getOperationName());
//            logger.debug("OperationContext serviceGroupName: '{}'", operationCtx.getServiceGroupName());
//            logger.debug("OperationContext serviceName: '{}'", operationCtx.getServiceName());
//            logger.debug("OperationContext isComplete: '{}'", operationCtx.isComplete());
//        } else {
//            logger.debug("OperationContext is null!");
//        }
//
//        ServiceGroupContext serviceGroupCtx = msgContext.getServiceGroupContext();
//        if (serviceGroupCtx != null) {
//            logger.debug("ServiceGroupContext ID: '{}'", serviceGroupCtx.getId());
//            AxisServiceGroup axisServiceGroup = serviceGroupCtx.getDescription();
//            Iterator<AxisService> itAxisService = axisServiceGroup.getServices();
//            while (itAxisService.hasNext()) {
//                AxisService axisService = itAxisService.next();
//                logger.debug("AxisService BindingName: '{}'", axisService.getBindingName());
//                logger.debug("AxisService CustomSchemaNamePrefix: '{}'", axisService.getCustomSchemaNamePrefix());
//                logger.debug("AxisService CustomSchemaNameSuffix: '{}'", axisService.getCustomSchemaNameSuffix());
//                logger.debug("AxisService endpointName: '{}'", axisService.getEndpointName());
//                Map<String, AxisEndpoint> axisEndpoints = axisService.getEndpoints();
//                for (String key : axisEndpoints.keySet()) {
//                    AxisEndpoint axisEndpoint = axisEndpoints.get(key);
//                    logger.debug("AxisEndpoint calculatedEndpointURL: '{}'", axisEndpoint.calculateEndpointURL());
//                    logger.debug("AxisEndpoint alias: '{}'", axisEndpoint.getAlias());
//                    logger.debug("AxisEndpoint endpointURL: '{}'", axisEndpoint.getEndpointURL());
//                    logger.debug("AxisEndpoint active: '{}'", axisEndpoint.isActive());
//                }
//                logger.debug("AxisService EPRs: '{}'", Arrays.toString((String[]) axisService.getEPRs()));
//                logger.debug("AxisService name: '{}'", axisService.getName());
//                logger.debug("AxisService isClientSide: '{}'", axisService.isClientSide());
//            }
//        } else {
//            logger.debug("ServiceGroupContext is null!");
//        }
//
//        ConfigurationContext configCtx = msgContext.getRootContext();
//        if (configCtx != null) {
//            logger.debug("ConfigurationContext contextRoot: '{}'", configCtx.getContextRoot());
//            logger.debug("ConfigurationContext serviceGroupContextIDs: '{}'", Arrays.toString((String[]) configCtx.getServiceGroupContextIDs()));
//            logger.debug("ConfigurationContext servicePath: '{}'", configCtx.getServicePath());
//        } else {
//            logger.debug("ConfigurationContext is null!");
//        }
        // End of Axis Debug

        try {
            /* Canonicalization of the full SOAP message */
            Document canonicalDocument = evidenceEmitterHandlerUtils.canonicalizeAxiomSoapEnvelope(msgContext.getEnvelope());
            if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                loggerClinical.debug("Pretty printing canonicalized:\n'{}'", XMLUtil.prettyPrint(canonicalDocument));
            }
            String eventType;
            String title;
            String msgUUID;
            AxisService axisService = msgContext.getServiceContext().getAxisService();
            boolean isClientSide = axisService.isClientSide();
            logger.debug("AxisService name: '{}' - isClientSide: '{}'", axisService.getName(), isClientSide);
            if (isClientSide) {
                //  This will stay commented as the EE CP doesn't mandate the generation of evidences in the response.
                logger.info("[NRR] Evidence Emitter - Response");
                /* NCP-B receives from NCP-A, e.g.:
                    NRR
                    title = "NCPB_XCPD_RES"
                    eventType = ihe event
                

                */
//                eventType = this.evidenceEmitterHandlerUtils.getEventTypeFromMessage(soapBody);
//                title = "NCPB_" + this.evidenceEmitterHandlerUtils.getTransactionNameFromMessage(soapBody);
//                //msgUUID = null; It stays as null because it's fetched from soap msg
//                logger.debug("eventType: '{}'", eventType);
//                logger.debug("title: '{}'", title);
//                
//                EvidenceUtils.createEvidenceREMNRR(envCanonicalized,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                            tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.SP_PRIVATEKEY_ALIAS,
//                            tr.com.srdc.epsos.util.Constants.SC_KEYSTORE_PATH,
//                            tr.com.srdc.epsos.util.Constants.SC_KEYSTORE_PASSWORD,
//                            tr.com.srdc.epsos.util.Constants.SC_PRIVATEKEY_ALIAS,
//                            eventType,
//                            new DateTime(),
//                            EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
//                            title);
            } else {
                /* NCP-B receives from Portal, e.g.:
                    NRR
                    title = "PORTAL_PD_REQ_RECEIVED"
                    eventType = "PORTAL_PD_REQ"
                    msguuid = IdA ID + datetime
                NCP-A receives from NCP-B, e.g.:
                    NRR
                    title = "NCPA_XCPD_REQ"
                    eventType = ihe event
                */
                eventType = evidenceEmitterHandlerUtils.getEventTypeFromMessage(soapBody);
                title = evidenceEmitterHandlerUtils.getServerSideTitle(soapBody);
                msgUUID = evidenceEmitterHandlerUtils.getMsgUUID(soapHeader, soapBody);
                logger.debug("eventType: '{}' - title: '{}'", eventType, title);

                if (msgUUID != null) {
                    logger.info("[NRR] Evidence Emitter - Portal NCP-B");
                    // this is a Portal-NCPB interaction: msgUUID comes from IdA or TRCA or is random
                    EvidenceUtils.createEvidenceREMNRR(canonicalDocument,
                            Constants.NCP_SIG_KEYSTORE_PATH,
                            Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                            Constants.NCP_SIG_KEYSTORE_PATH,
                            Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                            Constants.SC_KEYSTORE_PATH,
                            Constants.SC_KEYSTORE_PASSWORD,
                            Constants.SC_PRIVATEKEY_ALIAS,
                            eventType,
                            new DateTime(),
                            EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                            title,
                            msgUUID);
                } else {
                    logger.info("[NRR] Evidence Emitter - NCP A/B");
                    // this isn't a Portal-NCPB interaction (it's NCPB-NCPA), so msgUUID is retrieved from the SOAP header
                    EvidenceUtils.createEvidenceREMNRR(canonicalDocument,
                            Constants.NCP_SIG_KEYSTORE_PATH,
                            Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                            Constants.SC_KEYSTORE_PATH,
                            Constants.SC_KEYSTORE_PASSWORD,
                            Constants.SC_PRIVATEKEY_ALIAS,
                            Constants.SP_KEYSTORE_PATH,
                            Constants.SP_KEYSTORE_PASSWORD,
                            Constants.SP_PRIVATEKEY_ALIAS,
                            eventType,
                            new DateTime(),
                            EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                            title);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Handler.InvocationResponse.CONTINUE;
    }
}
