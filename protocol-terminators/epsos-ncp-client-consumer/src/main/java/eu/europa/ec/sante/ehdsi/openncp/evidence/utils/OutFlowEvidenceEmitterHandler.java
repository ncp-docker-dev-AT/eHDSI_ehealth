package eu.europa.ec.sante.ehdsi.openncp.evidence.utils;

import epsos.ccd.gnomon.auditmanager.EventOutcomeIndicator;
import eu.epsos.util.EvidenceUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * EvidenceEmitterHandler
 * Generates all NROs for the Portal
 * Currently supporting the generation of evidences in the following cases:
 * Portal sends request to NCP-B *
 *
 * @author jgoncalves
 */
public class OutFlowEvidenceEmitterHandler extends AbstractHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutFlowEvidenceEmitterHandler.class);

    @Override
    public Handler.InvocationResponse invoke(MessageContext msgcontext) throws AxisFault {

        LOGGER.debug("OutFlow Evidence Emitter handler is executing");
        EvidenceEmitterHandlerUtils evidenceEmitterHandlerUtils = new EvidenceEmitterHandlerUtils();

        /* I'll leave this here as it might be useful in the future */

//        SOAPHeader soapHeader = msgcontext.getEnvelope().getHeader();
//        if (soapHeader != null) {
//            Iterator<?> blocks = soapHeader.examineAllHeaderBlocks();
//            LOGGER.debug("Iterating over soap headers");
//            while (blocks.hasNext()) {
//                LOGGER.debug("Processing header");
//                SOAPHeaderBlock block = (SOAPHeaderBlock)blocks.next();
//                LOGGER.debug(block.toString());
//                block.setProcessed();
//            }
//        }

//        LOGGER.debug("LOGGING TEST VALUES");
//        LOGGER.debug("MessageContext properties: " + msgcontext.getProperties());
//        LOGGER.debug("MessageContext messageID: " + msgcontext.getMessageID());
//        
//        SessionContext sessionCtx = msgcontext.getSessionContext();
//        if (sessionCtx != null) {
//            LOGGER.debug("SessionContext CookieID: " + sessionCtx.getCookieID());
//        } else {
//            LOGGER.debug("SessionContext is null!");
//        }

//        OperationContext operationCtx = msgcontext.getOperationContext();
//        if (operationCtx != null) {
//            LOGGER.debug("OperationContext operationName: " + operationCtx.getOperationName());
//            LOGGER.debug("OperationContext serviceGroupName: " + operationCtx.getServiceGroupName());
//            LOGGER.debug("OperationContext serviceName; " + operationCtx.getServiceName());
//            LOGGER.debug("OperationContext isComplete: " + operationCtx.isComplete());
//        } else {
//            LOGGER.debug("OperationContext is null!");
//        }

//        ServiceGroupContext serviceGroupCtx = msgcontext.getServiceGroupContext();
//        if (serviceGroupCtx != null) {
//            LOGGER.debug("ServiceGroupContext ID: " + serviceGroupCtx.getId());
//            AxisServiceGroup axisServiceGroup = serviceGroupCtx.getDescription();
//            Iterator<AxisService> itAxisService = axisServiceGroup.getServices();
//            while (itAxisService.hasNext()) {
//                AxisService axisService = itAxisService.next();
//                LOGGER.debug("AxisService BindingName: " + axisService.getBindingName());
//                LOGGER.debug("AxisService CustomSchemaNamePrefix: " + axisService.getCustomSchemaNamePrefix());
//                LOGGER.debug("AxisService CustomSchemaNameSuffix: " + axisService.getCustomSchemaNameSuffix());
//                LOGGER.debug("AxisService endpointName: " + axisService.getEndpointName());
//                Map<String,AxisEndpoint> axisEndpoints = axisService.getEndpoints();
//                for (String key : axisEndpoints.keySet()) {
//                    AxisEndpoint axisEndpoint = axisEndpoints.get(key);
//                    LOGGER.debug("AxisEndpoint calculatedEndpointURL: " + axisEndpoint.calculateEndpointURL());
//                    LOGGER.debug("AxisEndpoint alias: " + axisEndpoint.getAlias());
//                    LOGGER.debug("AxisEndpoint endpointURL: " + axisEndpoint.getEndpointURL());
//                    LOGGER.debug("AxisEndpoint active: " + axisEndpoint.isActive());
//                }
//                LOGGER.debug("AxisService EPRs: " + Arrays.toString((String[]) axisService.getEPRs()));
//                LOGGER.debug("AxisService name: " + axisService.getName());
//                LOGGER.debug("AxisService isClientSide: " + axisService.isClientSide());
//            } 
//        } else {
//            LOGGER.debug("ServiceGroupContext is null!");
//        }

//        ConfigurationContext configCtx = msgcontext.getRootContext();
//        if (configCtx != null) {
//            LOGGER.debug("ConfigurationContext contextRoot: " + configCtx.getContextRoot());
//            LOGGER.debug("ConfigurationContext serviceGroupContextIDs: " + Arrays.toString((String[])configCtx.getServiceGroupContextIDs()));
//            LOGGER.debug("ConfigurationContext servicePath: " + configCtx.getServicePath());
//        } else {
//            LOGGER.debug("ConfigurationContext is null!");
//        }

        try {
            /* Canonicalizing the full SOAP message */
            Document envCanonicalized = evidenceEmitterHandlerUtils.canonicalizeAxiomSoapEnvelope(msgcontext.getEnvelope());

            SOAPHeader soapHeader = msgcontext.getEnvelope().getHeader();
            SOAPBody soapBody = msgcontext.getEnvelope().getBody();
            String eventType = evidenceEmitterHandlerUtils.getEventTypeFromMessage(soapBody);
            String title = evidenceEmitterHandlerUtils.getTransactionNameFromMessage(soapBody);
            String msgUUID = evidenceEmitterHandlerUtils.getMsgUUID(soapHeader, soapBody);
            LOGGER.debug("eventType: '{}'", eventType);
            LOGGER.debug("title: '{}'", title);
            LOGGER.debug("msgUUID: '{}", msgUUID);

            /* Portal sends request to NCP-B*/
            EvidenceUtils.createEvidenceREMNRO(envCanonicalized,
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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Handler.InvocationResponse.CONTINUE;
    }
}
