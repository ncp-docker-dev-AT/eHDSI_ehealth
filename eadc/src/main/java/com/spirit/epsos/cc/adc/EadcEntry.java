package com.spirit.epsos.cc.adc;

import org.w3c.dom.Document;

public interface EadcEntry {

    /**
     * the data xml is including transaction information and the CDA L3 document
     */
    Document getData();

    // The DataSetName
    String getDsName();

    EadcEntry newInstance(String dsName, Document data, Document soapRqData, Document soapRspData);

    /**
     * This enum represents the three different transaction types data source names.
     */
    enum DsTypes {

        EADC("jdbc/EADC");

        /*, _XCPD("jdbc/EADC_XCPD"), _XCA("jdbc/EADC_XCA"), _XDR("jdbc/EADC_XDR")*/

        private String value;

        DsTypes(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
