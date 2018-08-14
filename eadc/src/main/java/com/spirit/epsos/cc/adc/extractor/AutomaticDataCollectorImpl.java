package com.spirit.epsos.cc.adc.extractor;

import com.ibatis.common.jdbc.ScriptRunner;
import com.spirit.epsos.cc.adc.db.EadcDbConnect;
import eu.epsos.pt.eadc.util.EadcFactory;
import eu.epsos.pt.eadc.util.EadcUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.StringReader;
import java.util.TreeMap;

/**
 * Utility for extracting data from a transaction-xml-structure and inserting the results into an sql-database.
 * A detailed behavior of the extraction-process can be configured within the config.xml. The supported structure for
 * config.xml as well as the supported structure for the transaction-xml are specified by xml-schemas.
 * All files are located within the EADC_resources directory. This directory must be placed within the
 * current working directory.
 * For a jboss-installation this usually is jboss/bin/
 *
 * @author mk
 */
public class AutomaticDataCollectorImpl implements AutomaticDataCollector {

    // Logger object for logging to log4j
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticDataCollector.class);
    private static final Logger LOGGER_CLINICAL = LoggerFactory.getLogger("LOGGER_CLINICAL");

    // Path to the factory.xslt
    private static final String PATH_XSLT_FACTORY = new File(EadcUtil.getDefaultDsPath()).getAbsolutePath() + File.separator
            + "EADC_resources" + File.separator + "xslt" + File.separator + "factory.xslt";
    // Path to the config.xml
    private static final String PATH_XML_CONFIG = new File(EadcUtil.getDefaultDsPath()).getAbsolutePath() + File.separator
            + "EADC_resources" + File.separator + "config" + File.separator + "config.xml";

    // map with one intermediateTransformer per CDA-classcode
    private final TreeMap<String, EasyXsltTransformer> intermediateTransformerList;
    // DOM structure for caching the factory.xslt
    private Document factoryXslt;
    // DOM structure for caching the config.xml
    private Document configXml;

    /**
     * Initialize a new AutomaticDataCollector
     *
     * @throws IllegalArgumentException Will be thrown, when a required resource can not be initialized
     */
    public AutomaticDataCollectorImpl() {

        try {
            intermediateTransformerList = new TreeMap<>();
            this.factoryXslt = XmlFileReader.getInstance().readXmlDocumentFromFile(AutomaticDataCollectorImpl.PATH_XSLT_FACTORY);
            this.configXml = XmlFileReader.getInstance().readXmlDocumentFromFile(AutomaticDataCollectorImpl.PATH_XML_CONFIG);
            LOGGER.debug("Instance of AutomaticDataCollector created");
        } catch (Exception exception) {
            LOGGER.error("Error, when creating an Instance of AutomaticDataCollector");
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Processes a transaction, extracts data according to config.xml and stores
     * it into the database
     *
     * @param transaction    The transaction-xml-structure as specified by the
     *                       XML-Schema
     * @param dataSourceName The dataSourceName of the Database
     * @throws Exception
     */
    public void processTransaction(String dataSourceName, Document transaction) throws Exception {

        LOGGER.info("Processing a transaction object");
        String sqlInsertStatementList = this.extractDataAndCreateAccordingSqlInserts(transaction);
        if (!StringUtils.equals(System.getProperty("server.ehealth.mode"), "PROD")) {
            LOGGER_CLINICAL.info("Insert the following sql-queries:\n'{}'", sqlInsertStatementList);
        }
        this.runSqlScript(dataSourceName, sqlInsertStatementList);
    }

    /**
     * Extracts data from a transaction and creates the according
     * sql-insert-statements
     *
     * @param transaction The transaction xml-structure as specified by the
     *                    XML-Schema
     * @return A list of sql-insert-statements
     * @throws Exception
     */
    private String extractDataAndCreateAccordingSqlInserts(Document transaction) throws Exception {

        LOGGER.info("--> method extractDataAndCreateAccordingSqlInserts({})", transaction);
        String processedDocumentCode;
        String processedDocumentCodeSystem;
        String processedDocumentCodeAndCodeSystemCombination;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("XML Document: {}", EadcUtil.convertXMLDocumentToString(transaction));
        }

        NodeList clinicalDocumentNodeList = transaction.getElementsByTagNameNS(cdaNamespace, "ClinicalDocument");
        int numberOfCdaDocuments = clinicalDocumentNodeList.getLength();
        // Test, if the currently processed comes without a CDA-document
        if (numberOfCdaDocuments < 1) {
            LOGGER.info("No CDA Documents found.");
            processedDocumentCode = "N/A";
            processedDocumentCodeSystem = "N/A";
        } else {
            // Check the validity of the supplied CDA-content
            // And extract the classCode for using the correct classCode-customized xslt
            if (numberOfCdaDocuments > 1) {
                LOGGER.error("Multiple CDA Documents were found within the Transaction");
                throw new Exception("Multiple CDA Documents were found within the Transaction");
            }
            LOGGER.info("One CDA-document found");
            Node clinicalDocumentNode = clinicalDocumentNodeList.item(0);
            if (clinicalDocumentNode.getNodeType() != Element.ELEMENT_NODE) {
                LOGGER.error("The ClinicalDocument Node being found was not of type org.w3c.dom.Element");
                throw new Exception("The ClinicalDocument Node being found was not of type org.w3c.dom.Element");
            }
            Element clinicalDocumentElement = (Element) clinicalDocumentNode;
            NodeList codeNodeList = clinicalDocumentElement.getElementsByTagNameNS(cdaNamespace, "code");
            Node codeNode = codeNodeList.item(0);
            if (codeNode.getParentNode() != clinicalDocumentElement) {
                LOGGER.error("The first codeNode found was not a child of ClinicalDocument");
                throw new Exception("The first codeNode found was not a child of ClinicalDocument");
            }
            if (codeNode.getNodeType() != Element.ELEMENT_NODE) {
                LOGGER.error("The code node being found was not of type org.w3c.dom.Element");
                throw new Exception("The code node being found was not of type org.w3c.dom.Element");
            }
            LOGGER.info("Code Element found");
            Element codeElement = (Element) codeNode;
            // Extracting the document's code
            processedDocumentCode = codeElement.getAttribute("code");
            if (processedDocumentCode == null) {
                LOGGER.error("Unable to read the code Attribute");
                throw new Exception("Unable to read the code Attribute");
            }
            if (processedDocumentCode.length() == 0) {
                LOGGER.error("The code Attribute was either not specified or it was the empty string");
                throw new Exception("The code Attribute was either not specified or it was the empty string");
            }
            LOGGER.info("code: '{}", processedDocumentCode);
            // Extracting the document's codeSystem
            processedDocumentCodeSystem = codeElement.getAttribute("codeSystem");
            if (processedDocumentCodeSystem == null) {
                LOGGER.error("Unable to read the coceSystem Attribute");
                throw new Exception("Unable to read the coceSystem Attribute");
            }
            if (processedDocumentCodeSystem.length() == 0) {
                LOGGER.error("The codeSystemAttribute was either not specified or it was the empty string");
                throw new Exception("The codeSystemAttribute was either not specified or it was the empty string");
            }
            LOGGER.info("codeSystem: '{}'", processedDocumentCode);
        }
        processedDocumentCodeAndCodeSystemCombination = processedDocumentCode + "\"" + processedDocumentCodeSystem;
        // Guarantee, that the cachedIntermediaTransformerList is being initialized only once.
        // And ensure, that a cachedIntermediaTransformer is only created/added once to the cachedIntermediaTransformerList
        EasyXsltTransformer currentTransformer = this.intermediateTransformerList.get(processedDocumentCodeAndCodeSystemCombination);
        Document result;
        if (currentTransformer == null) {
            try {
                synchronized (this.intermediateTransformerList) {
                    currentTransformer = this.intermediateTransformerList.get(processedDocumentCodeAndCodeSystemCombination);
                    if (currentTransformer == null) {
                        LOGGER.info("Creating the XSLT-Transformer for code: '{}' and codeSystem: '{}'",
                                processedDocumentCode, processedDocumentCodeSystem);
                        ((Element) this.factoryXslt.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform",
                                "variable")
                                .item(0)).setAttribute("select",
                                "'" + processedDocumentCode
                                        + "'");
                        ((Element) this.factoryXslt.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Transform",
                                "variable")
                                .item(1)).setAttribute("select",
                                "'" + processedDocumentCodeSystem
                                        + "'");
                        currentTransformer = new EasyXsltTransformer(new EasyXsltTransformer(this.factoryXslt).transform(this.configXml));
                        this.intermediateTransformerList.put(processedDocumentCodeAndCodeSystemCombination,
                                currentTransformer);
                    }
                }
            } catch (Exception exception) {
                LOGGER.error("Unable to initialize the customized XSLT for processedDocumentCode:" + processedDocumentCode
                                + " and processedDocumentCodeSystem:"
                                + processedDocumentCodeSystem,
                        exception);
                throw new Exception("Unable to initialize the customized XSLT for processedDocumentCode:" + processedDocumentCode
                        + " and processedDocumentCodeSystem:"
                        + processedDocumentCodeSystem,
                        exception);
            }
            LOGGER.info("Current intermediaTransformer retrieved successfully");
        }
        // Perform the data-extraction
        try {
            result = currentTransformer.transform(transaction);
        } catch (Exception exception) {
            LOGGER.error("Error when transforming a document", exception);
            throw new Exception("Error when transforming a document", exception);
        }
        // As the XSLT returns plain text, the content is found within the result's root-node which is a text-node.
        return result.getFirstChild().getTextContent();
    }

    /**
     * Run the provided SQL-script by using the provided dataSourceName
     *
     * @param sqlScript      The sql-script being executed. Must be a list of
     *                       sql-statements being terminated with the ";" character.
     * @param dataSourceName The dataSource Identifier being used to connect to
     *                       the database. This string usually refers to a database-specification in a
     *                       datasource xml-file.
     * @throws Exception
     */
    private void runSqlScript(String dataSourceName, String sqlScript) throws Exception {

        EadcDbConnect sqlConnection = null;
        try (StringReader stringReader = new StringReader(sqlScript)) {
            sqlConnection = EadcFactory.INSTANCE.createEadcDbConnect(dataSourceName);
            ScriptRunner objScriptRunner = new ScriptRunner(sqlConnection.getConnection(), false, true);
            objScriptRunner.setLogWriter(null);
            objScriptRunner.setErrorLogWriter(null);

            objScriptRunner.runScript(stringReader);
        } catch (Exception exception) {
            LOGGER.error("The following error occurred during an SQL operation:", exception);
            throw new Exception("The following error occurred during an SQL operation:", exception);
        } finally {
            if (sqlConnection != null) {
                sqlConnection.closeConnection();
            }
        }
    }
}
