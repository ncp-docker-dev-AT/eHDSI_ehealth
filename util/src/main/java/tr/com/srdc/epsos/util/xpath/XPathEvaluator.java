package tr.com.srdc.epsos.util.xpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.Map;

public class XPathEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(XPathEvaluator.class);
    private static final String LOG_FMT_MESSAGE_XPATHEXPRESSIONEXCEPTION = "XPathExpressionException: '{}'";

    private XPathExpression xPathExp = null;
    private XPathFactory xpf = null;
    private XPath xPath = null;

    public XPathEvaluator(Map namespaces, String xPathExpStr) {

        this(namespaces);
        try {
            xPathExp = xPath.compile(xPathExpStr);
        } catch (XPathExpressionException e) {
            LOGGER.error(LOG_FMT_MESSAGE_XPATHEXPRESSIONEXCEPTION, e.getMessage(), e);
        }
    }

    public XPathEvaluator(Map namespaces) {

        init();
        xPath.setNamespaceContext(new NamespaceContextImpl(namespaces));
    }

    private void init() {

        if (xpf == null) {
            xpf = XPathFactory.newInstance();
            xPath = xpf.newXPath();
        }
    }

    public NodeList evaluate(Document doc) {

        NodeList matchedNodes = null;
        try {
            if (xPathExp != null) {
                matchedNodes = (NodeList) xPathExp.evaluate(doc, XPathConstants.NODESET);
            }
        } catch (XPathExpressionException e) {
            LOGGER.error(LOG_FMT_MESSAGE_XPATHEXPRESSIONEXCEPTION, e.getMessage(), e);
        }
        return matchedNodes;
    }

    public NodeList evaluate(Document doc, String xPathExpStr) {

        NodeList matchedNodes = null;
        try {
            XPathExpression localXPathExp = xPath.compile(xPathExpStr);
            matchedNodes = (NodeList) localXPathExp.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOGGER.error(LOG_FMT_MESSAGE_XPATHEXPRESSIONEXCEPTION, e.getMessage(), e);
        }
        return matchedNodes;
    }
}
