package fi.kela.se.epsos.data.model;

import java.util.Date;

import java.util.List;

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


    class Author{

        public String authorPerson;
        public List<String> authorSpeciality;

        public String getAuthorPerson() {
            return authorPerson;
        }
        public void setAuthorPerson(String authorPerson) {
            this.authorPerson = authorPerson;
        }

        public List<String> getAuthorSpeciality() {
            return authorSpeciality;
        }

        public void setAuthorSpeciality(List<String> authorSpeciality) {
            this.authorSpeciality = authorSpeciality;
        }
    }

    DocumentFileType getDocumentFileType();

    long getSize();

    Date getServiceStartTime();

    ReasonOfHospitalisation getReasonOfHospitalisation();

    List<Author> getAuthors();


}
