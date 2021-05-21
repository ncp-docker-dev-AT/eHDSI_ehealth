package eu.europa.ec.ehdsi.openncp.assertion.ws;

import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.ws.addressing.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.util.List;

public class AbstractServiceEndpoint {

    private static final QName SECURITY_ACTION = new QName(Names.WSA_NAMESPACE_NAME, Names.WSA_ACTION_NAME);
    private static final QName SECURITY_MESSAGE_ID = new QName(Names.WSA_NAMESPACE_NAME, Names.WSA_MESSAGEID_NAME);
    private static final QName SECURITY_RELATES_TO = new QName(Names.WSA_NAMESPACE_NAME, Names.WSA_RELATESTO_NAME);
    private static final QName SECURITY_REPLY_TO = new QName(Names.WSA_NAMESPACE_NAME, Names.WSA_REPLYTO_NAME);
    private static final QName SECURITY_TO = new QName(Names.WSA_NAMESPACE_NAME, Names.WSA_TO_NAME);
    private final Logger logger = LoggerFactory.getLogger(AbstractServiceEndpoint.class);

    @Resource
    private WebServiceContext webServiceContext;

    public void getWsAddressing() {

        MessageContext messageContext = webServiceContext.getMessageContext();
        List<Header> headers = CastUtils.cast((List<Header>) messageContext.get(Header.HEADER_LIST));

        for (Header header : headers) {
            if (SECURITY_MESSAGE_ID.equals(header.getName())) {
                Element element = (Element) header.getObject();
                logger.info("Message ID: '{}'", element.getTextContent());
            } else if (SECURITY_ACTION.equals(header.getName())) {
                Element element = (Element) header.getObject();
                logger.info("Action: '{}'", element.getTextContent());
            } else if (SECURITY_TO.equals(header.getName())) {
                Element element = (Element) header.getObject();
                logger.info("To: '{}'", element.getTextContent());
            } else if (SECURITY_RELATES_TO.equals(header.getName())) {
                Element element = (Element) header.getObject();
                logger.info("RelatesTo: '{}'", element.getTextContent());
            } else if (SECURITY_REPLY_TO.equals(header.getName())) {
                Element element = (Element) header.getObject();
                logger.info("ReplyTo: '{}'", element.getTextContent());
            }
        }
    }
}
