package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;


public class SMPHttp {

    //Upload
    private File smpFile;
    private String smpFileName;
    private String serviceGroupUrl;

    //Delete
    private List<ReferenceCollection> referenceCollection;
    private List<String> referenceSelected;
    private String reference;
    private String documentType;
    private String smptype;
    private Countries country;

    private String signedServiceMetadataUrl;
    private String smpURI;
    private int statusCode;
    private String businessCode;
    private String errorDescription;

    private File certificateFile;
    private MultipartFile extensionFile;
    private String documentIdentifier;
    private String documentIdentifierScheme;
    private String issuanceType;
    private String participantIdentifier;
    private String participantIdentifierScheme;
    private Boolean requireBusinessLevelSignature;
    private String minimumAuthenticationLevel;
    private String transportProfile;
    private String processIdentifier;
    private String processIdentifierScheme;
    private String certificateContent;
    private Date serviceExpirationDate;
    private Date serviceActivationDate;
    private String serviceDescription;
    private String technicalContactUrl;
    private String technicalInformationUrl;
    private String certificateUID;
    private String href;

    public String getServiceGroupUrl() {
        return serviceGroupUrl;
    }

    public void setServiceGroupUrl(String serviceGroupUrl) {
        this.serviceGroupUrl = serviceGroupUrl;
    }

    public String getSignedServiceMetadataUrl() {
        return signedServiceMetadataUrl;
    }

    public void setSignedServiceMetadataUrl(String signedServiceMetadataUrl) {
        this.signedServiceMetadataUrl = signedServiceMetadataUrl;
    }

    public File getSmpFile() {
        return smpFile;
    }

    public void setSmpFile(File smpFile) {
        this.smpFile = smpFile;
    }


    public String getSmpFileName() {
        return smpFileName;
    }

    public void setSmpFileName(String smpFileName) {
        this.smpFileName = smpFileName;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Countries getCountry() {
        return country;
    }

    public void setCountry(Countries country) {
        this.country = country;
    }

    public String getSmptype() {
        return smptype;
    }

    public void setSmptype(String smptype) {
        this.smptype = smptype;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getReferenceSelected() {
        return referenceSelected;
    }

    public void setReferenceSelected(List<String> referenceSelected) {
        this.referenceSelected = referenceSelected;
    }

    public List<ReferenceCollection> getReferenceCollection() {
        return referenceCollection;
    }

    public void setReferenceCollection(List<ReferenceCollection> referenceCollection) {
        this.referenceCollection = referenceCollection;
    }

    public String getSmpURI() {
        return smpURI;
    }

    public void setSmpURI(String smpURI) {
        this.smpURI = smpURI;
    }

    public void setCertificateFile(File certificateFile) {
        this.certificateFile = certificateFile;
    }

    public void setExtensionFile(MultipartFile extensionFile) {
        this.extensionFile = extensionFile;
    }

    public String getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(String documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public String getDocumentIdentifierScheme() {
        return documentIdentifierScheme;
    }

    public void setDocumentIdentifierScheme(String documentIdentifierScheme) {
        this.documentIdentifierScheme = documentIdentifierScheme;
    }

    public String getIssuanceType() {
        return issuanceType;
    }

    public void setIssuanceType(String issuanceType) {
        this.issuanceType = issuanceType;
    }

    public void setProcessIdentifier(String processIdentifier) {
        this.processIdentifier = processIdentifier;
    }

    public void setProcessIdentifierScheme(String processIdentifierScheme) {
        this.processIdentifierScheme = processIdentifierScheme;
    }
    public void setParticipantIdentifier(String participantID) {
        this.participantIdentifier = participantID;
    }

    public void setParticipantIdentifierScheme(String scheme) {
        this.participantIdentifierScheme = scheme;
    }

    public void setRequiredBusinessLevelSig(Boolean requireBusinessLevelSignature) {
        this.requireBusinessLevelSignature = requireBusinessLevelSignature;
    }

    public Boolean getRequiredBusinessLevelSig() {
        return this.requireBusinessLevelSignature;
    }

    public void setMinimumAutenticationLevel(String minimumAuthenticationLevel) {
        this.minimumAuthenticationLevel = minimumAuthenticationLevel;
    }

    public String getMinimumAutenticationLevel() {
        return this.minimumAuthenticationLevel;
    }

    public void setTransportProfile(String transportProfile) {
        this.transportProfile = transportProfile;
    }

    public void setCertificate(Object o) {
    }

    public void setCertificateContent(String certificateContent) {
        this.certificateContent = certificateContent;
    }

    public String getCertificateContent() {
        return this.certificateContent;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceDescription() {
        return this.serviceDescription;
    }

    public void setTechnicalContactUrl(String technicalContactUrl) {
        this.technicalContactUrl = technicalContactUrl;
    }

    public String getTechnicalContactUrl() {
        return this.technicalContactUrl;
    }

    public void setTechnicalInformationUrl(String technicalInformationUrl) {
        this.technicalInformationUrl = technicalInformationUrl;
    }

    public String getTechnicalInformationUrl() {
        return this.technicalInformationUrl;
    }

    public void setServiceExpirationDate(Date dateed) {
        this.serviceExpirationDate = dateed;
    }

    public Date getServiceExpirationDate() {
        return this.serviceExpirationDate;
    }

    public void setServiceActivationDate(Date datead) {
        this.serviceActivationDate = datead;
    }

    public Date getServiceActivationDate() {
        return this.serviceActivationDate;
    }

    public void setCertificateUID(String certificateUID) {
        this.certificateUID = certificateUID;
    }

    public String getCertificateUID() {
        return this.certificateUID;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getHref() {
        return this.href;
    }
}
