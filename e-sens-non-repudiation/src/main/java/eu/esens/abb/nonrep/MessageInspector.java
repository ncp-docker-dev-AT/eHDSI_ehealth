package eu.esens.abb.nonrep;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.mail.internet.MimeMessage;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;


public class MessageInspector {

    private static final String SOAP_LOCAL_NAME = "Envelope";
    private static final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    private static final String IHE_ITI_XCA_RETRIEVE = "urn:ihe:iti:2007:CrossGatewayRetrieve";

    private final Logger logger = LoggerFactory.getLogger(MessageInspector.class);
    private final Logger loggerClinical = LoggerFactory.getLogger("LOGGER_CLINICAL");


    private MessageType messageType;
    private String messageUUID;


    public MessageInspector(final MimeMessage incomingMsg) throws MalformedMIMEMessageException {
        throw new MalformedMIMEMessageException("Not yet implemented");
    }

    public MessageInspector(final SOAPMessage incomingMsg) throws MalformedIHESOAPException, SOAPException {
        this(incomingMsg.getSOAPBody().getOwnerDocument());
    }

    /**
     * @param incomingMsg
     * @throws MalformedIHESOAPException
     */
    public MessageInspector(final Document incomingMsg) throws MalformedIHESOAPException {

        if (incomingMsg == null) {
            throw new NullPointerException("No message has been passed");
        }
        logger.debug("MessageInspector, called with a document. Checking headers");
        checkHeaders(incomingMsg);
    }

    private void checkHeaders(Document incomingMsg) throws MalformedIHESOAPException {

        logger.debug("Checking if it is a SOAP document");
        Element docElement = incomingMsg.getDocumentElement();

        logger.info("[Non Repudiation] '{}' - '{}'", docElement.getLocalName(), docElement.getNamespaceURI());
        logMessage(incomingMsg);
        if (StringUtils.equals(docElement.getLocalName(), SOAP_LOCAL_NAME)
                && (StringUtils.equals(docElement.getNamespaceURI(), SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)
                || StringUtils.equals(docElement.getNamespaceURI(), SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE))) {

            logger.debug("Found a SOAP message");
            // The SOAP Message must be well structured to avoid MITM attacks e.g., it must have one SOAP header
            // and one single addressing. No WSSE4j is used here (which doesn't check for it).
            NodeList nodeList = docElement.getElementsByTagNameNS(docElement.getNamespaceURI(), "Header");
            Utilities.checkForNull(nodeList, "Header", logger);

            // get the body
            NodeList nlBody = docElement.getElementsByTagNameNS(docElement.getNamespaceURI(), "Body");
            Utilities.checkForNull(nlBody, "Body", logger);
            Element body = (Element) nlBody.item(0);

            // it can only be an element here, no classcasts
            Element header = (Element) nodeList.item(0);

            // header must have one addressing action
            nodeList = header.getElementsByTagNameNS(WS_ADDRESSING_NS, "Action");
            Utilities.checkForNull(nodeList, "WS-Addressing action", logger);


            Element action = (Element) nodeList.item(0);
            String actionText = action.getTextContent();
            if (actionText == null) {
                throw new MalformedIHESOAPException("No action text found");
            }

            if (StringUtils.equals(actionText, IHE_ITI_XCA_RETRIEVE)) {
                logger.debug("Found an IHE ITI XCA RETRIEVE");
                IHEXCARetrieve xcaRetrieve = new IHEXCARetrieve(body);
                this.setMessageType(xcaRetrieve);
            } else {
                logger.warn("Action not recognized: '{}'", actionText);
                //TODO: I differentiate here, since one may do some other guesses, to see if it is a valid message
                UnknownMessageType umt = new UnknownMessageType(incomingMsg);
                this.setMessageType(umt);
            }

            nodeList = header.getElementsByTagNameNS(WS_ADDRESSING_NS, "MessageID");
            Utilities.checkForNull(nodeList, "WS-Addressing MessageID", logger);

            Element uuidEl = (Element) nodeList.item(0);
            String uuidText = uuidEl.getTextContent();
            if (uuidText == null) {
                throw new MalformedIHESOAPException("No UUID can be found in the WS-Addressing header");
            } else {
                this.messageUUID = uuidText;
            }
        } else {
            logger.warn("The document passed is not a SOAP.");
            UnknownMessageType umt = new UnknownMessageType(incomingMsg);
            this.setMessageType(umt);
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    private void setMessageType(final MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageUUID() {
        return this.messageUUID;
    }

    /**
     * @param message
     */
    private void logMessage(Document message) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Utilities.serialize(message.getDocumentElement(), outputStream);
            String messageAsString = outputStream.toString();
            loggerClinical.info("Message:\n'{}'", messageAsString);
        } catch (TransformerException e) {
            loggerClinical.error("TransformerException: Cannot display Incoming Message '{}'", e.getMessage());
        }
    }
}
