/**
 * Copyright (C) 2011, 2012 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik
 * Tic. Ltd. Sti. <epsos@srdc.com.tr>
 * <p>
 * This file is part of SRDC epSOS NCP.
 * <p>
 * SRDC epSOS NCP is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SRDC epSOS NCP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SRDC epSOS NCP. If not, see <http://www.gnu.org/licenses/>.
 */
package tr.com.srdc.epsos.util.xpath;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.HashMap;

public class XPathEvaluator {

    XPathExpression xPathExp = null;
    private XPathFactory xpf = null;
    private XPath xPath = null;

    public XPathEvaluator(HashMap namespaces, String xPathExpStr) {
        this(namespaces);

        try {
            xPathExp = xPath.compile(xPathExpStr);
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public XPathEvaluator(HashMap namespaces) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return matchedNodes;
    }

    public NodeList evaluate(Document doc, String xPathExpStr) {
        NodeList matchedNodes = null;
        try {
            XPathExpression localXPathExp = xPath.compile(xPathExpStr);
            matchedNodes = (NodeList) localXPathExp.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return matchedNodes;
    }
}
