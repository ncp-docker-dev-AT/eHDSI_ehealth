package com.spirit.epsos.cc.adc;

import org.w3c.dom.Document;

public interface EadcEntry {

    /**
     * XML Document is including transaction information and the CDA L3 document.
     */
    Document getData();

    // DataSource name.
    String getDsName();

    EadcEntry newInstance(String dsName, Document data, Document soapRqData, Document soapRspData);

    /**
     * This enum represents the different transaction types data source names.
     */
    enum DsTypes {

        EADC("jdbc/EADC"),
        @Deprecated XCPD("jdbc/EADC_XCPD"),
        @Deprecated XCA("jdbc/EADC_XCA"),
        @Deprecated XDR("jdbc/EADC_XDR");

        private final String value;

        DsTypes(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
