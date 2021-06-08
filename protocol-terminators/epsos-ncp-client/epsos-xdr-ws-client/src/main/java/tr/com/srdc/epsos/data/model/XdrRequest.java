package tr.com.srdc.epsos.data.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.opensaml.saml.saml2.core.Assertion;

/**
 * @author DG-Sante A4
 */
public class XdrRequest {

    private Assertion idAssertion;
    private Assertion trcAssertion;
    private String countryCode;
    private String countryName;
    private String cdaId;
    private String submissionSetId;
    private String cda;
    private GenericDocumentCode documentCode;
    private PatientDemographics patient;

    public PatientDemographics getPatient() {
        return patient;
    }

    public void setPatient(PatientDemographics patient) {
        this.patient = patient;
    }

    public String getCdaId() {
        return cdaId;
    }

    public void setCdaId(String cdaId) {
        this.cdaId = cdaId;
    }

    public String getSubmissionSetId() {
        return submissionSetId;
    }

    public void setSubmissionSetId(String submissionSetId) {
        this.submissionSetId = submissionSetId;
    }

    public String getCda() {
        return cda;
    }

    public void setCda(String cda) {
        this.cda = cda;
    }

    public Assertion getIdAssertion() {
        return idAssertion;
    }

    public void setIdAssertion(Assertion idAssertion) {
        this.idAssertion = idAssertion;
    }

    public Assertion getTrcAssertion() {
        return trcAssertion;
    }

    public void setTrcAssertion(Assertion trcAssertion) {
        this.trcAssertion = trcAssertion;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String policyCountryCode) {
        this.countryCode = policyCountryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String policyCountryName) {
        this.countryName = policyCountryName;
    }

    /**
     * @return the documentCode
     */
    public GenericDocumentCode getDocumentCode() {
        return documentCode;
    }

    /**
     * @param documentCode the documentCode to set
     */
    public void setDocumentCode(GenericDocumentCode documentCode) {
        this.documentCode = documentCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("idAssertion", idAssertion)
                .append("trcAssertion", trcAssertion)
                .append("countryCode", countryCode)
                .append("countryName", countryName)
                .append("cdaId", cdaId)
                .append("submissionSetId", submissionSetId)
                .append("cda", cda)
                .append("documentCode", documentCode)
                .append("patient", patient)
                .toString();
    }
}
