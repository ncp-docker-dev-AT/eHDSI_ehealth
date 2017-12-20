package com.spirit.epsos.cc.adc.extractor;

import org.w3c.dom.Document;

/**
 * Utility for extracting data from a transaction-xml-structure and inserting
 * the results into an sql-database. A detailed behavior of the
 * extraction-process can be configured within the config.xml. The supported
 * structure for config.xml as well as the supported structure for the
 * transaction-xml are specified by xml-schemas. All files are located within
 * the EADC_resources directory. This directory must be placed within the
 * current working directory. For a jboss-installation this usually is
 * jboss/bin/
 *
 * @author mk
 */
public interface AutomaticDataCollector {

    // constant for the cda-namespace
    String cdaNamespace = "urn:hl7-org:v3";

    /**
     * Processes a transaction, extracts data according to config.xml and stores
     * it into the database
     *
     * @param transaction    The transaction-xml-structure as specified by the
     *                       XML-Schema
     * @param dsName The dataSourceName of the Database
     * @throws Exception
     */
    void processTransaction(String dsName, Document transaction) throws Exception;
}
