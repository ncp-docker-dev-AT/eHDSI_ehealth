package com.spirit.epsos.cc.adc.test;

import com.spirit.epsos.cc.adc.db.EadcDbConnect;
import com.spirit.epsos.cc.adc.extractor.AutomaticDataCollector;
import com.spirit.epsos.cc.adc.extractor.AutomaticDataCollectorImpl;
import com.spirit.epsos.cc.adc.extractor.XmlFileReader;
import eu.epsos.pt.eadc.util.EadcFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.UUID;

/**
 * Test-class for testing the DataExtractionMechanism and storing the results into a database
 */
@RunWith(JUnit4.class)
public class TestAutomaticDataCollector extends BaseEadcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomaticDataCollector.class);
    private static AutomaticDataCollector automaticDataCollectorInstance;
    private static Document transactionDemo;
    private EadcDbConnect con;

    @Before
    public void setUp() throws Exception {

        super.setUp();

        /* TEST DATABASE INSTANTIATION */
        con = EadcFactory.INSTANCE.createEadcDbConnect(DS_NAME);

        automaticDataCollectorInstance = EadcFactory.INSTANCE.createAutomaticDataCollector();
        String basedir = "src/test/resources/EADC_resources";
        transactionDemo = XmlFileReader.getInstance()
                .readXmlDocumentFromFile(basedir + File.separator
                        + "demo"
                        + File.separator
                        + "TransactionDemo.xml");
        // Insert the currently tested Transaction-Primary-Key into the TransactionDemo XML-Structure
        ((Element) (transactionDemo.getElementsByTagName("TransactionInfo").item(0))).setAttribute(
                "Transaction_PK", UUID.randomUUID().toString());
    }

    @After
    public void tearDown() throws Exception {

        super.tearDown();
        con.closeConnection();
    }

    /**
     * Test a matching codeSystem and a non-matching code
     *
     * @throws Exception
     */
    @Test
    public void testWithDifferentCodeAndMatchingCodeSystem() throws Exception {
        SetCodeAndCodeSystem(transactionDemo, "2.16.840.1.113883.6.1",
                "60591-4");
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test a non-matching codeSystem and a matching code
     *
     * @throws Exception
     */
    @Test
    public void testWithMathingCodeAndDifferentCodeSystem() throws Exception {
        SetCodeAndCodeSystem(transactionDemo, "2.16.840.1.113883.6.0",
                "60591-5");
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test a matching codeSystem and a matching code
     *
     * @throws Exception
     */
    @Test
    public void testWithMatchingCodeAndMatchingCodeSystem() throws Exception {
        SetCodeAndCodeSystem(transactionDemo, "2.16.840.1.113883.6.1",
                "60591-5");
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test if the Exception is thrown, when the CDA's code-element is missing
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void testWithNoCodeElementInCdaDocument() throws Exception {
        transactionDemo.getElementsByTagNameNS(AutomaticDataCollectorImpl.CDA_NAMESPACE, "ClinicalDocument")
                .item(0).removeChild(transactionDemo.getElementsByTagNameNS(AutomaticDataCollectorImpl.CDA_NAMESPACE,
                "code").item(0));
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test if the Exception is thrown, if the CDA's code element's
     * code-attribute is missing
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void testWithNoCodeSystemAttributeInCodeElementOfCdaDocument() throws Exception {
        ((Element) transactionDemo.getElementsByTagNameNS(AutomaticDataCollectorImpl.CDA_NAMESPACE,
                "code").item(0)).removeAttribute("code");
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test if the Exception is thrown, if the CDA's code element's
     * codeSystem-attribute is missing
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void testWithNoCodeSystemAttributeInCodeSystemElementOfCdaDocument() throws Exception {
        ((Element) transactionDemo.getElementsByTagNameNS(AutomaticDataCollectorImpl.CDA_NAMESPACE,
                "code").item(0)).removeAttribute("codeSystem");
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test, if the extractor handles a CDA-less transaction correctly
     *
     * @throws Exception
     */
    @Test
    public void testWithNoCdaDocument() throws Exception {
        transactionDemo.getFirstChild().removeChild(transactionDemo.getElementsByTagNameNS(
                AutomaticDataCollectorImpl.CDA_NAMESPACE, "ClinicalDocument").item(0));
        runAutomaticDataCollector(DS_NAME, transactionDemo);
    }

    /**
     * Test, if the correct Exception is thronw, if the transaction-xml is null
     *
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void testWithTransactionDemoNull() throws Exception {
        runAutomaticDataCollector(DS_NAME, null);
    }

    /**
     * Test if transaction failure populate correctly
     *
     * @throws Exception
     */
    @Test
    public void testWithTransactionError() throws Exception {
        runAutomaticDataCollectorFailure(DS_NAME, transactionDemo, "unexpected error");
    }
    /**
     * Helper Method for setting the code and codeSystem Attributes at
     * cda:ClinicalDocument/cda:code
     *
     * @param transactionXmlStructure
     * @param currentProcessedDocumentCodeSystem
     * @param currentProcessedDocumentCode
     */
    public void SetCodeAndCodeSystem(Document transactionXmlStructure, String currentProcessedDocumentCodeSystem,
                                     String currentProcessedDocumentCode) {

        LOGGER.info("currentProcessedDocumentCodeSystem:" + currentProcessedDocumentCodeSystem);
        LOGGER.info("currentProcessedDocumentCode:" + currentProcessedDocumentCode);

        // Insert the currently tested code Attribute into the TransactionDemo XML-Structure
        ((Element) (transactionXmlStructure.getElementsByTagNameNS(AutomaticDataCollectorImpl.CDA_NAMESPACE,
                "code").item(0))).setAttribute("codeSystem", currentProcessedDocumentCodeSystem);
        // Insert the currently tested codeSystem Attribute into the TransactionDemo XML-Structure
        ((Element) (transactionXmlStructure.getElementsByTagNameNS(AutomaticDataCollectorImpl.CDA_NAMESPACE, "code")
                .item(0))).setAttribute("code", currentProcessedDocumentCode);
    }

    /**
     * run a Test with the specified documentCode and documentCodeSystem
     *
     * @param transactionXmlStructure
     * @throws Exception
     */
    public void runAutomaticDataCollector(String dsName, Document transactionXmlStructure) throws Exception {
        automaticDataCollectorInstance.processTransaction(dsName, transactionXmlStructure);
    }

    /**
     * run a Test with the specified error description
     *
     * @param transactionXmlStructure
     * @param errorDescription
     * @throws Exception
     */
    public void runAutomaticDataCollectorFailure(String dsName, Document transactionXmlStructure, String errorDescription) throws Exception {
        automaticDataCollectorInstance.processTransactionFailure(dsName, transactionXmlStructure, errorDescription);
    }

}
