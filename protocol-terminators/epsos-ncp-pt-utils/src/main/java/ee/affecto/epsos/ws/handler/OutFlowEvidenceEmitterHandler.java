package ee.affecto.epsos.ws.handler;

import epsos.ccd.gnomon.auditmanager.EventOutcomeIndicator;
import eu.epsos.util.EvidenceUtils;
import eu.europa.ec.sante.ehdsi.openncp.util.OpenNCPConstants;
import eu.europa.ec.sante.ehdsi.openncp.util.ServerMode;
import org.apache.axiom.soap.SOAPBody;
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
 * OutFlowEvidenceEmitter
 * Generates all NROs
 * Currently supporting the generation of evidences in the following cases:
 * NCP-B sends to NCP-A
 * NCP-A replies to NCP-B (left commented as the Evidence Emitter CP does not mandate generation of evidences on the response)
 * NCP-B replies to Portal (left commented as the Evidence Emitter CP does not mandate generation of evidences on the response)
 *
 * @author jgoncalves
 */
public class OutFlowEvidenceEmitterHandler extends AbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(OutFlowEvidenceEmitterHandler.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");

    @Override
    public Handler.InvocationResponse invoke(MessageContext messageContext) {

        logger.info("[NRO] OutFlow Evidence Emitter handler is executing");
        EvidenceEmitterHandlerUtils evidenceEmitterHandlerUtils = new EvidenceEmitterHandlerUtils();

        /* I'll leave this here as it might be useful in the future */

//        SOAPHeader soapHeader = msgcontext.getEnvelope().getHeader();
//        if (soapHeader != null) {
//            Iterator<?> blocks = soapHeader.examineAllHeaderBlocks();
//            logger.debug("Iterating over soap headers");
//            while (blocks.hasNext()) {
//                logger.debug("Processing header");
//                SOAPHeaderBlock block = (SOAPHeaderBlock)blocks.next();
//                logger.debug(block.toString());
//                block.setProcessed();
//            }
//        }

//        logger.debug("LOGGING TEST VALUES");
//        logger.debug("MessageContext properties: " + msgcontext.getProperties());
//        logger.debug("MessageContext messageID: " + msgcontext.getMessageID());
//        
//        SessionContext sessionCtx = msgcontext.getSessionContext();
//        if (sessionCtx != null) {
//            logger.debug("SessionContext CookieID: " + sessionCtx.getCookieID());
//        } else {
//            logger.debug("SessionContext is null!");
//        }

//        OperationContext operationCtx = msgcontext.getOperationContext();
//        if (operationCtx != null) {
//            logger.debug("OperationContext operationName: " + operationCtx.getOperationName());
//            logger.debug("OperationContext serviceGroupName: " + operationCtx.getServiceGroupName());
//            logger.debug("OperationContext serviceName; " + operationCtx.getServiceName());
//            logger.debug("OperationContext isComplete: " + operationCtx.isComplete());
//        } else {
//            logger.debug("OperationContext is null!");
//        }

//        ServiceGroupContext serviceGroupCtx = msgcontext.getServiceGroupContext();
//        if (serviceGroupCtx != null) {
//            logger.debug("ServiceGroupContext ID: " + serviceGroupCtx.getId());
//            AxisServiceGroup axisServiceGroup = serviceGroupCtx.getDescription();
//            Iterator<AxisService> itAxisService = axisServiceGroup.getServices();
//            while (itAxisService.hasNext()) {
//                AxisService axisService = itAxisService.next();
//                logger.debug("AxisService BindingName: " + axisService.getBindingName());
//                logger.debug("AxisService CustomSchemaNamePrefix: " + axisService.getCustomSchemaNamePrefix());
//                logger.debug("AxisService CustomSchemaNameSuffix: " + axisService.getCustomSchemaNameSuffix());
//                logger.debug("AxisService endpointName: " + axisService.getEndpointName());
//                Map<String,AxisEndpoint> axisEndpoints = axisService.getEndpoints();
//                for (String key : axisEndpoints.keySet()) {
//                    AxisEndpoint axisEndpoint = axisEndpoints.get(key);
//                    logger.debug("AxisEndpoint calculatedEndpointURL: " + axisEndpoint.calculateEndpointURL());
//                    logger.debug("AxisEndpoint alias: " + axisEndpoint.getAlias());
//                    logger.debug("AxisEndpoint endpointURL: " + axisEndpoint.getEndpointURL());
//                    logger.debug("AxisEndpoint active: " + axisEndpoint.isActive());
//                }
//                logger.debug("AxisService EPRs: " + Arrays.toString((String[]) axisService.getEPRs()));
//                logger.debug("AxisService name: " + axisService.getName());
//                logger.debug("AxisService isClientSide: " + axisService.isClientSide());
//            } 
//        } else {
//            logger.debug("ServiceGroupContext is null!");
//        }

//        ConfigurationContext configCtx = msgcontext.getRootContext();
//        if (configCtx != null) {
//            logger.debug("ConfigurationContext contextRoot: " + configCtx.getContextRoot());
//            logger.debug("ConfigurationContext serviceGroupContextIDs: " + Arrays.toString((String[])configCtx.getServiceGroupContextIDs()));
//            logger.debug("ConfigurationContext servicePath: " + configCtx.getServicePath());
//        } else {
//            logger.debug("ConfigurationContext is null!");
//        }

        try {
            // Canonicalization of the full SOAP message
            Document canonicalDocument = evidenceEmitterHandlerUtils.canonicalizeAxiomSoapEnvelope(messageContext.getEnvelope());
            if (OpenNCPConstants.NCP_SERVER_MODE != ServerMode.PRODUCTION && loggerClinical.isDebugEnabled()) {
                loggerClinical.debug("Pretty printing Canonicalize:\n'{}'", XMLUtil.prettyPrint(canonicalDocument));
            }
            SOAPBody soapBody = messageContext.getEnvelope().getBody();
            String eventType;
            String title;
            AxisService axisService = messageContext.getServiceContext().getAxisService();
            boolean isClientSide = axisService.isClientSide();
            logger.debug("AxisService name: '{}' - isClientSide: '{}'", axisService.getName(), isClientSide);

            if (isClientSide) {
                logger.info("[NRO] Evidence Emitter - NCP-B");
                /* NCP-B sends to NCP-A, e.g.: 
                    NRO
                    title = "NCPB_XCPD_REQ"
                    eventType = ihe event 
                */
                eventType = evidenceEmitterHandlerUtils.getEventTypeFromMessage(soapBody);
                title = "NCPB_" + evidenceEmitterHandlerUtils.getTransactionNameFromMessage(soapBody);
                //msgUUID = null; It stays as null because it's fetched from soap msg
                logger.debug("Title: '{}' - eventType: '{}'", title, eventType);

                EvidenceUtils.createEvidenceREMNRO(canonicalDocument, Constants.NCP_SIG_KEYSTORE_PATH, Constants.NCP_SIG_KEYSTORE_PASSWORD,
                        Constants.NCP_SIG_PRIVATEKEY_ALIAS, Constants.SC_KEYSTORE_PATH, Constants.SC_KEYSTORE_PASSWORD,
                        Constants.SC_PRIVATEKEY_ALIAS, Constants.SP_KEYSTORE_PATH, Constants.SP_KEYSTORE_PASSWORD,
                        Constants.SP_PRIVATEKEY_ALIAS, eventType, new DateTime(), EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                        title);
            } else {
                logger.info("[NRO] Evidence Emitter - NCP-A");
                /* NCP-A replies to NCP-B, e.g.: 
                    NRO
                    title = "NCPA_XCPD_RES"
                    eventType = ihe event
                NCP-B replies to Portal, e.g.: 
                    NRO
                    title = "NCPB_PD_RES_SENT"
                    eventType = "NCPB_PD_RES"
                    msguuid = random
                */
                /* Joao: as per the CP, evidence generation on the way back is optional,
                so I leave it commented. If in the future it's decided that is mandatory,
                just uncomment.
                */
//                eventType = this.evidenceEmitterHandlerUtils.getEventTypeFromMessage(soapBody);
//                title = this.evidenceEmitterHandlerUtils.getServerSideTitle(soapBody);
//                msgUUID = this.evidenceEmitterHandlerUtils.getMsgUUID(soapHeader, soapBody);
//                logger.debug("eventType: " + eventType);
//                logger.debug("title: " + title);
//                logger.debug("msgUUID: " + msgUUID);
//                
//                if (msgUUID != null) {
//                    // this is a Portal-NCPB interaction: msgUUID comes from IdA or is random
//                    EvidenceUtils.createEvidenceREMNRO(envCanonicalized,
//                                Constants.NCP_SIG_KEYSTORE_PATH,
//                                Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                                Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                                Constants.SC_KEYSTORE_PATH,
//                                Constants.SC_KEYSTORE_PASSWORD,
//                                Constants.SC_PRIVATEKEY_ALIAS,
//                                Constants.NCP_SIG_KEYSTORE_PATH,
//                                Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                                Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                                eventType,
//                                new DateTime(),
//                                EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
//                                title,
//                                msgUUID);
//                } else {
//                    // this isn't a Portal-NCPB interaction (it's NCPB-NCPA), so msgUUID is retrieved from the soap header
//                    EvidenceUtils.createEvidenceREMNRO(envCanonicalized,
//                                Constants.NCP_SIG_KEYSTORE_PATH,
//                                Constants.NCP_SIG_KEYSTORE_PASSWORD,
//                                Constants.NCP_SIG_PRIVATEKEY_ALIAS,
//                                Constants.SP_KEYSTORE_PATH,
//                                Constants.SP_KEYSTORE_PASSWORD,
//                                Constants.SP_PRIVATEKEY_ALIAS,
//                                Constants.SC_KEYSTORE_PATH,
//                                Constants.SC_KEYSTORE_PASSWORD,
//                                Constants.SC_PRIVATEKEY_ALIAS,
//                                eventType,
//                                new DateTime(),
//                                EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
//                                title);        
//                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Handler.InvocationResponse.CONTINUE;
    }
}
