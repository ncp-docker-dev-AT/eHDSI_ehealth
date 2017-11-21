package epsos.ccd.gnomon.xslt.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

abstract class AbstractValidator {

    protected static Document transformStringToDocument(String documentAsString) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document document = null;
        DocumentBuilder builder;
        if (isHTML(documentAsString)) {
            if (documentAsString.contains("<meta")) {
                String subString = documentAsString.substring(documentAsString.indexOf("<meta"));
                subString = subString.substring(0, subString.indexOf(">") + 1);
                documentAsString = documentAsString.replaceAll(subString, "");
            }
            documentAsString = documentAsString.replaceAll("<br>", "");
            documentAsString = documentAsString.replaceAll("</br>", "");
            do {
                if (documentAsString.contains("<br")) {
                    String subString = documentAsString.substring(documentAsString.indexOf("<br"));
                    subString = subString.substring(0, subString.indexOf(">") + 1);
                    documentAsString = documentAsString.replaceAll(subString, "");
                }
            } while (documentAsString.contains("<br"));
            if (documentAsString.contains("<hr")) {
                String subString = documentAsString.substring(documentAsString.indexOf("<hr"));
                subString = subString.substring(0, subString.indexOf(">") + 1);
                documentAsString = documentAsString.replaceAll(subString, "");
            }
        }
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(documentAsString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    private static boolean isHTML(String documentAsString) {
        return documentAsString.contains("<html");
    }

    public void validate(String cda, String resultHtml) throws XPathExpressionException, ParserConfigurationException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Document cdaDoc = transformStringToDocument(cda);
        Document resultDoc = transformStringToDocument(resultHtml);

        validateTitle(xpath, cdaDoc, resultDoc);
        validatePatientName(xpath, cdaDoc, resultDoc);
        validateActiveIngredients(xpath, cdaDoc, resultDoc);
    }

    protected abstract void validateActiveIngredients(XPath xpath, Document cdaDoc, Document resultDoc) throws XPathExpressionException;

    protected abstract void validateTitle(XPath xpath, Document cdaDoc, Document resultDoc) throws XPathExpressionException;

    protected abstract void validatePatientName(XPath xpath, Document cdaDoc, Document resultDoc) throws XPathExpressionException;
}