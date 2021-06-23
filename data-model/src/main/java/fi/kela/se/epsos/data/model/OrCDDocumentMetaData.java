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
        public List<String> authorInstitution;
        public List<String> authorRole;
        public List<String> authorSpeciality;
        public String authorTelecommunication;


        public String getAuthorPerson() {
            return authorPerson;
        }

        public void setAuthorPerson(String authorPerson) {
            this.authorPerson = authorPerson;
        }

        public List<String> getAuthorInstitution() {
            return authorInstitution;
        }

        public void setAuthorInstitution(List<String> authorInstitution) {
            this.authorInstitution = authorInstitution;
        }

        public List<String> getAuthorRole() {
            return authorRole;
        }

        public void setAuthorRole(List<String> authorRole) {
            this.authorRole = authorRole;
        }

        public List<String> getAuthorSpeciality() {
            return authorSpeciality;
        }

        public void setAuthorSpeciality(List<String> authorSpeciality) {
            this.authorSpeciality = authorSpeciality;
        }

        public String getAuthorTelecommunication() {
            return authorTelecommunication;
        }

        public void setAuthorTelecommunication(String authorTelecommunication) {
            this.authorTelecommunication = authorTelecommunication;
        }
    }

    DocumentFileType getDocumentFileType();

    long getSize();

    Date getServiceStartTime();

    ReasonOfHospitalisation getReasonOfHospitalisation();

    List<Author> getAuthors();


}
