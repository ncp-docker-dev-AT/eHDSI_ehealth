package epsos.ccd.gnomon.xslt.util;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashSet;
import java.util.Set;

public class HtmlValidator extends AbstractValidator {

    @Override
    protected void validateActiveIngredients(XPath xpath, Document cdaDoc, Document resultDoc) throws XPathExpressionException {
        final NodeList nl = (NodeList) xpath.evaluate("(/*[local-name()='ClinicalDocument']/*[local-name()='component']/*[local-name()='structuredBody']/*[local-name()='component']/*[local-name()='section']/*[local-name()='text']/*[local-name()='paragraph'])[4]/*[local-name()='content']",
                cdaDoc, XPathConstants.NODESET);
        Set<String> activeIngredientsCdaParts = new HashSet<>();
        for (int index = 0; index < nl.getLength(); index++) {
            Node node = nl.item(index);
            activeIngredientsCdaParts.add(node.getTextContent());
        }

        final String activeIngredientsHtml = (String) xpath.evaluate("(/html/body/div/p)[4]/text()", resultDoc, XPathConstants.STRING);
        Set<String> activeIngredientsHtmlParts = buildActiveIngredientsHtmlParts(activeIngredientsHtml.split("                     "));
        compareSets(activeIngredientsCdaParts, activeIngredientsHtmlParts);
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
        final String patientNameHtml = (String) xpath.evaluate("((/html/body/table[@class='header_table']/tbody/tr)[1]/td)[2]/text()",
                resultDoc, XPathConstants.STRING);
        Assert.assertEquals(patientNameCda, patientNameHtml);
    }

    private void compareSets(Set<String> activeIngredientsCdaParts, Set<String> activeIngredientsHtmlParts) {
        Assert.assertEquals(activeIngredientsCdaParts.size(), activeIngredientsHtmlParts.size());
        for (String activeIngredientsCdaPart : activeIngredientsCdaParts) {
            Assert.assertTrue(activeIngredientsHtmlParts.contains(activeIngredientsCdaPart));
        }
    }

    private Set<String> buildActiveIngredientsHtmlParts(String[] activeIngredients) {
        Set<String> activeIngredientsHtmlParts = new HashSet<>();
        for (String activeIngredient : activeIngredients) {
            if (!StringUtils.EMPTY.equals(activeIngredient.trim())) {
                activeIngredientsHtmlParts.add(activeIngredient.trim());
            }
        }
        return activeIngredientsHtmlParts;
    }
}