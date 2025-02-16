package epsos.ccd.gnomon.xslt.util;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Assert;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public class PdfValidator extends AbstractValidator {

    @Override
    protected void validateActiveIngredients(XPath xpath, Document cdaDoc, Document resultDoc) {

    }

    @Override
    protected void validateTitle(XPath xpath, Document cdaDoc, Document resultDoc) throws XPathExpressionException {
        final String titleCda = (String) xpath.evaluate("/*[local-name()='ClinicalDocument']/*[local-name()='title']/text()",
                cdaDoc, XPathConstants.STRING);
        final String titleHtml = (String) xpath.evaluate("/html/body/h1/text()",
                resultDoc, XPathConstants.STRING);
        Assert.assertEquals(titleCda, titleHtml);
    }

    @Override
    protected void validatePatientName(XPath xpath, Document cdaDoc, Document resultDoc) throws XPathExpressionException {
        final String patientNameCda = xpath.evaluate("/*[local-name()='ClinicalDocument']/*[local-name()='recordTarget']/*[local-name()='patientRole']/*[local-name()='patient']/*[local-name()='name']/*[local-name()='given']/text()",
                cdaDoc, XPathConstants.STRING) +
                " " +
                xpath.evaluate("/*[local-name()='ClinicalDocument']/*[local-name()='recordTarget']/*[local-name()='patientRole']/*[local-name()='patient']/*[local-name()='name']/*[local-name()='family']/text()",
                        cdaDoc, XPathConstants.STRING);
        String patientNameHtml = StringEscapeUtils.unescapeHtml4(((String) xpath.evaluate("((((/html/body/table[@class='header_table']/tbody/tr)[3]/table[@class='header_table']/tbody/tr)[2]/td)[3])/text()",
                resultDoc, XPathConstants.STRING)).trim() +
                " " +
                ((String) xpath.evaluate("((((/html/body/table[@class='header_table']/tbody/tr)[3]/table[@class='header_table']/tbody/tr)[2]/td)[2])/text()",
                        resultDoc, XPathConstants.STRING)).trim());
        Assert.assertEquals(patientNameCda, patientNameHtml);
    }
}