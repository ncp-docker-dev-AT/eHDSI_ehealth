package ee.affecto.epsos.ws.handler;

import epsos.ccd.gnomon.auditmanager.EventOutcomeIndicator;
import eu.epsos.util.EvidenceUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(InFlowEvidenceEmitterHandler.class);

    @Override
    public Handler.InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        LOGGER.debug("InFlow Evidence Emitter handler is executing");
        EvidenceEmitterHandlerUtils evidenceEmitterHandlerUtils = new EvidenceEmitterHandlerUtils();
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
        SOAPBody soapBody = msgContext.getEnvelope().getBody();

        /* I'll leave this here as it might be useful in the future */
//
//        if (soapHeader != null) {
//            Iterator<?> blocks = soapHeader.examineAllHeaderBlocks();
//            LOGGER.debug("Iterating over soap headers");
//            while (blocks.hasNext()) {
//                LOGGER.debug("Processing header");
//                SOAPHeaderBlock block = (SOAPHeaderBlock) blocks.next();
//                if (LOGGER.isDebugEnabled()) {
//                    LOGGER.debug(block.toString());
//                }
//                block.setProcessed();
//            }
//        }
//
//        LOGGER.debug("LOGGING TEST VALUES");
//        LOGGER.debug("MessageContext properties: '{}'", msgContext.getProperties());
//        LOGGER.debug("MessageContext messageID: '{}'", msgContext.getMessageID());
//
//        SessionContext sessionCtx = msgContext.getSessionContext();
//        if (sessionCtx != null) {
//            LOGGER.debug("SessionContext CookieID: '{}'", sessionCtx.getCookieID());
//        } else {
//            LOGGER.debug("SessionContext is null!");
//        }
//
//        OperationContext operationCtx = msgContext.getOperationContext();
//        if (operationCtx != null) {
//            LOGGER.debug("OperationContext operationName: '{}'", operationCtx.getOperationName());
//            LOGGER.debug("OperationContext serviceGroupName: '{}'", operationCtx.getServiceGroupName());
//            LOGGER.debug("OperationContext serviceName: '{}'", operationCtx.getServiceName());
//            LOGGER.debug("OperationContext isComplete: '{}'", operationCtx.isComplete());
//        } else {
//            LOGGER.debug("OperationContext is null!");
//        }
//
//        ServiceGroupContext serviceGroupCtx = msgContext.getServiceGroupContext();
//        if (serviceGroupCtx != null) {
//            LOGGER.debug("ServiceGroupContext ID: '{}'", serviceGroupCtx.getId());
//            AxisServiceGroup axisServiceGroup = serviceGroupCtx.getDescription();
//            Iterator<AxisService> itAxisService = axisServiceGroup.getServices();
//            while (itAxisService.hasNext()) {
//                AxisService axisService = itAxisService.next();
//                LOGGER.debug("AxisService BindingName: '{}'", axisService.getBindingName());
//                LOGGER.debug("AxisService CustomSchemaNamePrefix: '{}'", axisService.getCustomSchemaNamePrefix());
//                LOGGER.debug("AxisService CustomSchemaNameSuffix: '{}'", axisService.getCustomSchemaNameSuffix());
//                LOGGER.debug("AxisService endpointName: '{}'", axisService.getEndpointName());
//                Map<String, AxisEndpoint> axisEndpoints = axisService.getEndpoints();
//                for (String key : axisEndpoints.keySet()) {
//                    AxisEndpoint axisEndpoint = axisEndpoints.get(key);
//                    LOGGER.debug("AxisEndpoint calculatedEndpointURL: '{}'", axisEndpoint.calculateEndpointURL());
//                    LOGGER.debug("AxisEndpoint alias: '{}'", axisEndpoint.getAlias());
//                    LOGGER.debug("AxisEndpoint endpointURL: '{}'", axisEndpoint.getEndpointURL());
//                    LOGGER.debug("AxisEndpoint active: '{}'", axisEndpoint.isActive());
//                }
//                LOGGER.debug("AxisService EPRs: '{}'", Arrays.toString((String[]) axisService.getEPRs()));
//                LOGGER.debug("AxisService name: '{}'", axisService.getName());
//                LOGGER.debug("AxisService isClientSide: '{}'", axisService.isClientSide());
//            }
//        } else {
//            LOGGER.debug("ServiceGroupContext is null!");
//        }
//
//        ConfigurationContext configCtx = msgContext.getRootContext();
//        if (configCtx != null) {
//            LOGGER.debug("ConfigurationContext contextRoot: '{}'", configCtx.getContextRoot());
//            LOGGER.debug("ConfigurationContext serviceGroupContextIDs: '{}'", Arrays.toString((String[]) configCtx.getServiceGroupContextIDs()));
//            LOGGER.debug("ConfigurationContext servicePath: '{}'", configCtx.getServicePath());
//        } else {
//            LOGGER.debug("ConfigurationContext is null!");
//        }
        // End of Axis Debug

        try {
            /* Canonicalization of the full SOAP message */
            Document envCanonicalized = evidenceEmitterHandlerUtils.canonicalizeAxiomSoapEnvelope(msgContext.getEnvelope());

            String eventType;
            String title;
            String msgUUID;
            AxisService axisService = msgContext.getServiceContext().getAxisService();
            boolean isClientSide = axisService.isClientSide();
            LOGGER.debug("AxisService name: '{}'", axisService.getName());
            LOGGER.debug("AxisService isClientSide: '{}'", isClientSide);
            if (isClientSide) {
                /* NCP-B receives from NCP-A, e.g.:
                    NRR
                    title = "NCPB_XCPD_RES"
                    eventType = ihe event
                
                This will stay commented as the EE CP doesn't mandate the generation of evidences in the response
                */
//                eventType = this.evidenceEmitterHandlerUtils.getEventTypeFromMessage(soapBody);
//                title = "NCPB_" + this.evidenceEmitterHandlerUtils.getTransactionNameFromMessage(soapBody);
//                //msgUUID = null; It stays as null because it's fetched from soap msg
//                LOGGER.debug("eventType: '{}'", eventType);
//                LOGGER.debug("title: '{}'", title);
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
                LOGGER.debug("eventType: '{}'", eventType);
                LOGGER.debug("title: '{}'", title);
//                LOGGER.debug("msgUUID: '{}'", msgUUID); //It stays as null because it's fetched from soap msg

                if (msgUUID != null) {
                    // this is a Portal-NCPB interaction: msgUUID comes from IdA or TRCA or is random
                    EvidenceUtils.createEvidenceREMNRR(envCanonicalized,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                            tr.com.srdc.epsos.util.Constants.SC_KEYSTORE_PATH,
                            tr.com.srdc.epsos.util.Constants.SC_KEYSTORE_PASSWORD,
                            tr.com.srdc.epsos.util.Constants.SC_PRIVATEKEY_ALIAS,
                            eventType,
                            new DateTime(),
                            EventOutcomeIndicator.FULL_SUCCESS.getCode().toString(),
                            title,
                            msgUUID);
                } else {
                    // this isn't a Portal-NCPB interaction (it's NCPB-NCPA), so msgUUID is retrieved from the SOAP header
                    EvidenceUtils.createEvidenceREMNRR(envCanonicalized,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PATH,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_KEYSTORE_PASSWORD,
                            tr.com.srdc.epsos.util.Constants.NCP_SIG_PRIVATEKEY_ALIAS,
                            tr.com.srdc.epsos.util.Constants.SC_KEYSTORE_PATH,
                            tr.com.srdc.epsos.util.Constants.SC_KEYSTORE_PASSWORD,
                            tr.com.srdc.epsos.util.Constants.SC_PRIVATEKEY_ALIAS,
                            tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PATH,
                            tr.com.srdc.epsos.util.Constants.SP_KEYSTORE_PASSWORD,
                            tr.com.srdc.epsos.util.Constants.SP_PRIVATEKEY_ALIAS,
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
