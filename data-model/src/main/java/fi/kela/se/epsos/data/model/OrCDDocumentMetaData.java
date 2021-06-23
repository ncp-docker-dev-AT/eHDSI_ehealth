package fi.kela.se.epsos.data.model;

import java.util.Date;

/**
 * EDispensationDocumentMetaData interface
 */
public interface OrCDDocumentMetaData extends EPSOSDocumentMetaData {

    enum DocumentFileType {
        PDF, PNG, JPEG;
    }

    class ReasonOfHospitalisation {
        private String code;
        private String codingScheme;
        private String text;

        public ReasonOfHospitalisation(String code, String codingScheme, String text) {
            this.code = code;
            this.codingScheme = codingScheme;
            this.text = text;
        }

        public String getCode() {
            return code;
        }

        public String getCodingScheme() {
            return codingScheme;
        }

        public String getText() {
            return text;
        }
    }

    DocumentFileType getDocumentFileType();

    long getSize();

    Date getServiceStartTime();

    ReasonOfHospitalisation getReasonOfHospitalisation();
}
