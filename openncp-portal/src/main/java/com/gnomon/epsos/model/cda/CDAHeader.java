package com.gnomon.epsos.model.cda;

import java.util.List;

public class CDAHeader {

    private String effectiveTime;
    private String expireDate;
    private String languageCode;
    private String patientId;
    private String patientAmka;
    private String patientAma;
    private String patientTameio;
    private String patientDikaiouxosEkas;
    private String patientAmesaAsfalismenos;
    private String patientAsfalistikiIkanotita;
    private String patientEidosEmesaAsfalismenou;
    private String patientAsfalistikiIkanotitaEndDate;
    private String patientAddress;
    private String patientCity;
    private String patientPostalCode;
    private String patientCountry;
    private String patientFamilyName;
    private String patientPrefix;
    private String patientGivenName;
    private String patientSex;
    private String patientBirthDate;
    private String patientTelephone;
    private String patientEmail;
    private String patientFax;
    private String patientLanguageCommunication;

    private String guardianAmka;
    private String guardianAddress;
    private String guardianCity;
    private String guardianPostalCode;
    private String guardianCountry;
    private String guardianFamilyName;
    private String guardianPrefix;
    private String guardianGivenName;
    private String guardianSex;
    private String guardianBirthDate;
    private String guardianTelephone;
    private String guardianEmail;
    private String guardianFax;

    private boolean hasParticipant;
    private boolean hasGuardian;
    private String participantType;
    private String participantAddress;
    private String participantCity;
    private String participantPostalCode;
    private String participantCountry;
    private String participantFamilyName;
    private String participantPrefix;
    private String participantGivenName;
    private String participantSex;
    private String participantBirthDate;
    private String participantTelephone;
    private String participantEmail;
    private String participantFax;

    private String pharmacistAMKA;
    private String pharmacistAddress;
    private String pharmacistCity;
    private String pharmacistPostalCode;
    private String pharmacistCountry;
    private String pharmacistFamilyName;
    private String pharmacistPrefix;
    private String pharmacistGivenName;
    private String pharmacistSex;
    private String pharmacistBirthDate;
    private String pharmacistTelephone;
    private String pharmacistEmail;
    private String pharmacistFax;
    private String pharmacistOrgId;
    private String pharmacistOrgName;
    private String pharmacistOrgAddress;
    private String pharmacistOrgCity;
    private String pharmacistOrgPostalCode;
    private String pharmacistOrgCountry;
    private String pharmacistOrgTelephone;
    private String pharmacistOrgEmail;
    private String pharmacistOid;

    private String doctorSpeciality;
    private String prescriptionIssueDate;
    private String prescriptionStartDate;
    private String prescriptionEndDate;
    private String doctorAMKA;
    private String doctorETAA;
    private String doctorAddress;
    private String doctorCity;
    private String doctorPostalCode;
    private String doctorCountry;
    private String doctorFamilyName;
    private String doctorPrefix;
    private String doctorGivenName;
    private String doctorSex;
    private String doctorBirthDate;
    private String doctorTelephone;
    private String doctorEmail;
    private String doctorFax;
    private String doctorUnit;
    private String doctorOrgId;
    private String doctorOrgName;
    private String doctorOrgAddress;
    private String doctorOrgCity;
    private String doctorOrgPostalCode;
    private String doctorOrgCountry;
    private String doctorOrgTelephone;
    private String doctorOrgEmail;
    private String doctorOid;

    private String prescriptionIDRecurrent;
    private String prescriptionBarcodeRecurrent;
    private String prescriptionType;
    private String prescriptionRecurrent;
    private String prescriptionBarcode;
    private String prescriptionTitle;
    private String dispensationId;

    private String consentCode;
    private String consentDisplayName;
    private String consentStartDate;
    private String consentEndDate;

    private List EPDetail;
    private List EDDetail;

    public String getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(String effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getPatientAmka() {
        return patientAmka;
    }

    public void setPatientAmka(String patientAmka) {
        this.patientAmka = patientAmka;
    }

    public String getPatientAma() {
        return patientAma;
    }

    public void setPatientAma(String patientAma) {
        this.patientAma = patientAma;
    }

    public String getPatientTameio() {
        return patientTameio;
    }

    public void setPatientTameio(String patientTameio) {
        this.patientTameio = patientTameio;
    }

    public String getPatientDikaiouxosEkas() {
        return patientDikaiouxosEkas;
    }

    public void setPatientDikaiouxosEkas(String patientDikaiouxosEkas) {
        this.patientDikaiouxosEkas = patientDikaiouxosEkas;
    }

    public String getPatientAmesaAsfalismenos() {
        return patientAmesaAsfalismenos;
    }

    public void setPatientAmesaAsfalismenos(String patientAmesaAsfalismenos) {
        this.patientAmesaAsfalismenos = patientAmesaAsfalismenos;
    }

    public String getPatientAsfalistikiIkanotita() {
        return patientAsfalistikiIkanotita;
    }

    public void setPatientAsfalistikiIkanotita(String patientAsfalistikiIkanotita) {
        this.patientAsfalistikiIkanotita = patientAsfalistikiIkanotita;
    }

    public String getPatientAsfalistikiIkanotitaEndDate() {
        return patientAsfalistikiIkanotitaEndDate;
    }

    public void setPatientAsfalistikiIkanotitaEndDate(
            String patientAsfalistikiIkanotitaEndDate) {
        this.patientAsfalistikiIkanotitaEndDate = patientAsfalistikiIkanotitaEndDate;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public String getPatientCity() {
        return patientCity;
    }

    public void setPatientCity(String patientCity) {
        this.patientCity = patientCity;
    }

    public String getPatientPostalCode() {
        return patientPostalCode;
    }

    public void setPatientPostalCode(String patientPostalCode) {
        this.patientPostalCode = patientPostalCode;
    }

    public String getPatientCountry() {
        return patientCountry;
    }

    public void setPatientCountry(String patientCountry) {
        this.patientCountry = patientCountry;
    }

    public String getPatientFamilyName() {
        return patientFamilyName;
    }

    public void setPatientFamilyName(String patientFamilyName) {
        this.patientFamilyName = patientFamilyName;
    }

    public String getPatientPrefix() {
        return patientPrefix;
    }

    public void setPatientPrefix(String patientPrefix) {
        this.patientPrefix = patientPrefix;
    }

    public String getPatientGivenName() {
        return patientGivenName;
    }

    public void setPatientGivenName(String patientGivenName) {
        this.patientGivenName = patientGivenName;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientTelephone() {
        return patientTelephone;
    }

    public void setPatientTelephone(String patientTelephone) {
        this.patientTelephone = patientTelephone;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientFax() {
        return patientFax;
    }

    public void setPatientFax(String patientFax) {
        this.patientFax = patientFax;
    }

    public String getGuardianAmka() {
        return guardianAmka;
    }

    public void setGuardianAmka(String guardianAmka) {
        this.guardianAmka = guardianAmka;
    }

    public String getGuardianAddress() {
        return guardianAddress;
    }

    public void setGuardianAddress(String guardianAddress) {
        this.guardianAddress = guardianAddress;
    }

    public String getGuardianCity() {
        return guardianCity;
    }

    public void setGuardianCity(String guardianCity) {
        this.guardianCity = guardianCity;
    }

    public String getGuardianPostalCode() {
        return guardianPostalCode;
    }

    public void setGuardianPostalCode(String guardianPostalCode) {
        this.guardianPostalCode = guardianPostalCode;
    }

    public String getGuardianCountry() {
        return guardianCountry;
    }

    public void setGuardianCountry(String guardianCountry) {
        this.guardianCountry = guardianCountry;
    }

    public String getGuardianFamilyName() {
        return guardianFamilyName;
    }

    public void setGuardianFamilyName(String guardianFamilyName) {
        this.guardianFamilyName = guardianFamilyName;
    }

    public String getGuardianPrefix() {
        return guardianPrefix;
    }

    public void setGuardianPrefix(String guardianPrefix) {
        this.guardianPrefix = guardianPrefix;
    }

    public String getGuardianGivenName() {
        return guardianGivenName;
    }

    public void setGuardianGivenName(String guardianGivenName) {
        this.guardianGivenName = guardianGivenName;
    }

    public String getGuardianSex() {
        return guardianSex;
    }

    public void setGuardianSex(String guardianSex) {
        this.guardianSex = guardianSex;
    }

    public String getGuardianBirthDate() {
        return guardianBirthDate;
    }

    public void setGuardianBirthDate(String guardianBirthDate) {
        this.guardianBirthDate = guardianBirthDate;
    }

    public String getGuardianTelephone() {
        return guardianTelephone;
    }

    public void setGuardianTelephone(String guardianTelephone) {
        this.guardianTelephone = guardianTelephone;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

    public String getGuardianFax() {
        return guardianFax;
    }

    public void setGuardianFax(String guardianFax) {
        this.guardianFax = guardianFax;
    }

    public String getDoctorSpeciality() {
        return doctorSpeciality;
    }

    public void setDoctorSpeciality(String doctorSpeciality) {
        this.doctorSpeciality = doctorSpeciality;
    }

    public String getPrescriptionIssueDate() {
        return prescriptionIssueDate;
    }

    public void setPrescriptionIssueDate(String prescriptionIssueDate) {
        this.prescriptionIssueDate = prescriptionIssueDate;
    }

    public String getDoctorAMKA() {
        return doctorAMKA;
    }

    public void setDoctorAMKA(String doctorAMKA) {
        this.doctorAMKA = doctorAMKA;
    }

    public String getDoctorETAA() {
        return doctorETAA;
    }

    public void setDoctorETAA(String doctorETAA) {
        this.doctorETAA = doctorETAA;
    }

    public String getDoctorFamilyName() {
        return doctorFamilyName;
    }

    public void setDoctorFamilyName(String doctorFamilyName) {
        this.doctorFamilyName = doctorFamilyName;
    }

    public String getDoctorPrefix() {
        return doctorPrefix;
    }

    public void setDoctorPrefix(String doctorPrefix) {
        this.doctorPrefix = doctorPrefix;
    }

    public String getDoctorGivenName() {
        return doctorGivenName;
    }

    public void setDoctorGivenName(String doctorGivenName) {
        this.doctorGivenName = doctorGivenName;
    }

    public String getDoctorSex() {
        return doctorSex;
    }

    public void setDoctorSex(String doctorSex) {
        this.doctorSex = doctorSex;
    }

    public String getDoctorBirthDate() {
        return doctorBirthDate;
    }

    public void setDoctorBirthDate(String doctorBirthDate) {
        this.doctorBirthDate = doctorBirthDate;
    }

    public String getDoctorTelephone() {
        return doctorTelephone;
    }

    public void setDoctorTelephone(String doctorTelephone) {
        this.doctorTelephone = doctorTelephone;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getDoctorFax() {
        return doctorFax;
    }

    public void setDoctorFax(String doctorFax) {
        this.doctorFax = doctorFax;
    }

    public String getPatientEidosEmesaAsfalismenou() {
        return patientEidosEmesaAsfalismenou;
    }

    public void setPatientEidosEmesaAsfalismenou(
            String patientEidosEmesaAsfalismenou) {
        this.patientEidosEmesaAsfalismenou = patientEidosEmesaAsfalismenou;
    }

    public String getPatientLanguageCommunication() {
        return patientLanguageCommunication;
    }

    public void setPatientLanguageCommunication(String patientLanguageCommunication) {
        this.patientLanguageCommunication = patientLanguageCommunication;
    }

    public String getDoctorAddress() {
        return doctorAddress;
    }

    public void setDoctorAddress(String doctorAddress) {
        this.doctorAddress = doctorAddress;
    }

    public String getDoctorCity() {
        return doctorCity;
    }

    public void setDoctorCity(String doctorCity) {
        this.doctorCity = doctorCity;
    }

    public String getDoctorPostalCode() {
        return doctorPostalCode;
    }

    public void setDoctorPostalCode(String doctorPostalCode) {
        this.doctorPostalCode = doctorPostalCode;
    }

    public String getDoctorCountry() {
        return doctorCountry;
    }

    public void setDoctorCountry(String doctorCountry) {
        this.doctorCountry = doctorCountry;
    }

    public String getParticipantType() {
        return participantType;
    }

    public void setParticipantType(String participantType) {
        this.participantType = participantType;
    }

    public String getParticipantAddress() {
        return participantAddress;
    }

    public void setParticipantAddress(String participantAddress) {
        this.participantAddress = participantAddress;
    }

    public String getParticipantCity() {
        return participantCity;
    }

    public void setParticipantCity(String participantCity) {
        this.participantCity = participantCity;
    }

    public String getParticipantPostalCode() {
        return participantPostalCode;
    }

    public void setParticipantPostalCode(String participantPostalCode) {
        this.participantPostalCode = participantPostalCode;
    }

    public String getParticipantCountry() {
        return participantCountry;
    }

    public void setParticipantCountry(String participantCountry) {
        this.participantCountry = participantCountry;
    }

    public String getParticipantFamilyName() {
        return participantFamilyName;
    }

    public void setParticipantFamilyName(String participantFamilyName) {
        this.participantFamilyName = participantFamilyName;
    }

    public String getParticipantPrefix() {
        return participantPrefix;
    }

    public void setParticipantPrefix(String participantPrefix) {
        this.participantPrefix = participantPrefix;
    }

    public String getParticipantGivenName() {
        return participantGivenName;
    }

    public void setParticipantGivenName(String participantGivenName) {
        this.participantGivenName = participantGivenName;
    }

    public String getParticipantSex() {
        return participantSex;
    }

    public void setParticipantSex(String participantSex) {
        this.participantSex = participantSex;
    }

    public String getParticipantBirthDate() {
        return participantBirthDate;
    }

    public void setParticipantBirthDate(String participantBirthDate) {
        this.participantBirthDate = participantBirthDate;
    }

    public String getParticipantTelephone() {
        return participantTelephone;
    }

    public void setParticipantTelephone(String participantTelephone) {
        this.participantTelephone = participantTelephone;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getParticipantFax() {
        return participantFax;
    }

    public void setParticipantFax(String participantFax) {
        this.participantFax = participantFax;
    }

    public boolean isHasParticipant() {
        return hasParticipant;
    }

    public void setHasParticipant(boolean hasParticipant) {
        this.hasParticipant = hasParticipant;
    }

    public String getPrescriptionIDRecurrent() {
        return prescriptionIDRecurrent;
    }

    public void setPrescriptionIDRecurrent(String prescriptionIDRecurrent) {
        this.prescriptionIDRecurrent = prescriptionIDRecurrent;
    }

    public String getPrescriptionBarcodeRecurrent() {
        return prescriptionBarcodeRecurrent;
    }

    public void setPrescriptionBarcodeRecurrent(String prescriptionBarcodeRecurrent) {
        this.prescriptionBarcodeRecurrent = prescriptionBarcodeRecurrent;
    }

    public String getPrescriptionType() {
        return prescriptionType;
    }

    public void setPrescriptionType(String prescriptionType) {
        this.prescriptionType = prescriptionType;
    }

    public String getPrescriptionBarcode() {
        return prescriptionBarcode;
    }

    public void setPrescriptionBarcode(String prescriptionBarcode) {
        this.prescriptionBarcode = prescriptionBarcode;
    }

    public String getPrescriptionTitle() {
        return prescriptionTitle;
    }

    public void setPrescriptionTitle(String prescriptionTitle) {
        this.prescriptionTitle = prescriptionTitle;
    }

    public boolean isHasGuardian() {
        return hasGuardian;
    }

    public void setHasGuardian(boolean hasGuardian) {
        this.hasGuardian = hasGuardian;
    }

    public String getPrescriptionRecurrent() {
        return prescriptionRecurrent;
    }

    public void setPrescriptionRecurrent(String prescriptionRecurrent) {
        this.prescriptionRecurrent = prescriptionRecurrent;
    }

    public String getPharmacistAMKA() {
        return pharmacistAMKA;
    }

    public void setPharmacistAMKA(String pharmacistAMKA) {
        this.pharmacistAMKA = pharmacistAMKA;
    }

    public String getPharmacistAddress() {
        return pharmacistAddress;
    }

    public void setPharmacistAddress(String pharmacistAddress) {
        this.pharmacistAddress = pharmacistAddress;
    }

    public String getPharmacistCity() {
        return pharmacistCity;
    }

    public void setPharmacistCity(String pharmacistCity) {
        this.pharmacistCity = pharmacistCity;
    }

    public String getPharmacistPostalCode() {
        return pharmacistPostalCode;
    }

    public void setPharmacistPostalCode(String pharmacistPostalCode) {
        this.pharmacistPostalCode = pharmacistPostalCode;
    }

    public String getPharmacistCountry() {
        return pharmacistCountry;
    }

    public void setPharmacistCountry(String pharmacistCountry) {
        this.pharmacistCountry = pharmacistCountry;
    }

    public String getPharmacistFamilyName() {
        return pharmacistFamilyName;
    }

    public void setPharmacistFamilyName(String pharmacistFamilyName) {
        this.pharmacistFamilyName = pharmacistFamilyName;
    }

    public String getPharmacistPrefix() {
        return pharmacistPrefix;
    }

    public void setPharmacistPrefix(String pharmacistPrefix) {
        this.pharmacistPrefix = pharmacistPrefix;
    }

    public String getPharmacistGivenName() {
        return pharmacistGivenName;
    }

    public void setPharmacistGivenName(String pharmacistGivenName) {
        this.pharmacistGivenName = pharmacistGivenName;
    }

    public String getPharmacistSex() {
        return pharmacistSex;
    }

    public void setPharmacistSex(String pharmacistSex) {
        this.pharmacistSex = pharmacistSex;
    }

    public String getPharmacistBirthDate() {
        return pharmacistBirthDate;
    }

    public void setPharmacistBirthDate(String pharmacistBirthDate) {
        this.pharmacistBirthDate = pharmacistBirthDate;
    }

    public String getPharmacistTelephone() {
        return pharmacistTelephone;
    }

    public void setPharmacistTelephone(String pharmacistTelephone) {
        this.pharmacistTelephone = pharmacistTelephone;
    }

    public String getPharmacistEmail() {
        return pharmacistEmail;
    }

    public void setPharmacistEmail(String pharmacistEmail) {
        this.pharmacistEmail = pharmacistEmail;
    }

    public String getPharmacistFax() {
        return pharmacistFax;
    }

    public void setPharmacistFax(String pharmacistFax) {
        this.pharmacistFax = pharmacistFax;
    }

    public String getDispensationId() {
        return dispensationId;
    }

    public void setDispensationId(String dispensationId) {
        this.dispensationId = dispensationId;
    }

    public String getPharmacistOrgName() {
        return pharmacistOrgName;
    }

    public void setPharmacistOrgName(String pharmacistOrgName) {
        this.pharmacistOrgName = pharmacistOrgName;
    }

    public List getEPDetail() {
        return EPDetail;
    }

    public void setEPDetail(List ePDetail) {
        EPDetail = ePDetail;
    }

    public List getEDDetail() {
        return EDDetail;
    }

    public void setEDDetail(List eDDetail) {
        EDDetail = eDDetail;
    }

    public String getPharmacistOrgAddress() {
        return pharmacistOrgAddress;
    }

    public void setPharmacistOrgAddress(String pharmacistOrgAddress) {
        this.pharmacistOrgAddress = pharmacistOrgAddress;
    }

    public String getPharmacistOrgCity() {
        return pharmacistOrgCity;
    }

    public void setPharmacistOrgCity(String pharmacistOrgCity) {
        this.pharmacistOrgCity = pharmacistOrgCity;
    }

    public String getPharmacistOrgPostalCode() {
        return pharmacistOrgPostalCode;
    }

    public void setPharmacistOrgPostalCode(String pharmacistOrgPostalCode) {
        this.pharmacistOrgPostalCode = pharmacistOrgPostalCode;
    }

    public String getPharmacistOrgCountry() {
        return pharmacistOrgCountry;
    }

    public void setPharmacistOrgCountry(String pharmacistOrgCountry) {
        this.pharmacistOrgCountry = pharmacistOrgCountry;
    }

    public String getPharmacistOrgTelephone() {
        return pharmacistOrgTelephone;
    }

    public void setPharmacistOrgTelephone(String pharmacistOrgTelephone) {
        this.pharmacistOrgTelephone = pharmacistOrgTelephone;
    }

    public String getPharmacistOrgEmail() {
        return pharmacistOrgEmail;
    }

    public void setPharmacistOrgEmail(String pharmacistOrgEmail) {
        this.pharmacistOrgEmail = pharmacistOrgEmail;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getDoctorUnit() {
        return doctorUnit;
    }

    public void setDoctorUnit(String doctorUnit) {
        this.doctorUnit = doctorUnit;
    }

    public String getPrescriptionEndDate() {
        return prescriptionEndDate;
    }

    public void setPrescriptionEndDate(String prescriptionEndDate) {
        this.prescriptionEndDate = prescriptionEndDate;
    }

    public String getPrescriptionStartDate() {
        return prescriptionStartDate;
    }

    public void setPrescriptionStartDate(String prescriptionStartDate) {
        this.prescriptionStartDate = prescriptionStartDate;
    }

    public String getPharmacistOrgId() {
        return pharmacistOrgId;
    }

    public void setPharmacistOrgId(String pharmacistOrgId) {
        this.pharmacistOrgId = pharmacistOrgId;
    }

    public String getDoctorOrgId() {
        return doctorOrgId;
    }

    public void setDoctorOrgId(String doctorOrgId) {
        this.doctorOrgId = doctorOrgId;
    }

    public String getDoctorOrgName() {
        return doctorOrgName;
    }

    public void setDoctorOrgName(String doctorOrgName) {
        this.doctorOrgName = doctorOrgName;
    }

    public String getDoctorOrgAddress() {
        return doctorOrgAddress;
    }

    public void setDoctorOrgAddress(String doctorOrgAddress) {
        this.doctorOrgAddress = doctorOrgAddress;
    }

    public String getDoctorOrgCity() {
        return doctorOrgCity;
    }

    public void setDoctorOrgCity(String doctorOrgCity) {
        this.doctorOrgCity = doctorOrgCity;
    }

    public String getDoctorOrgPostalCode() {
        return doctorOrgPostalCode;
    }

    public void setDoctorOrgPostalCode(String doctorOrgPostalCode) {
        this.doctorOrgPostalCode = doctorOrgPostalCode;
    }

    public String getDoctorOrgCountry() {
        return doctorOrgCountry;
    }

    public void setDoctorOrgCountry(String doctorOrgCountry) {
        this.doctorOrgCountry = doctorOrgCountry;
    }

    public String getDoctorOrgTelephone() {
        return doctorOrgTelephone;
    }

    public void setDoctorOrgTelephone(String doctorOrgTelephone) {
        this.doctorOrgTelephone = doctorOrgTelephone;
    }

    public String getDoctorOrgEmail() {
        return doctorOrgEmail;
    }

    public void setDoctorOrgEmail(String doctorOrgEmail) {
        this.doctorOrgEmail = doctorOrgEmail;
    }

    public String getConsentCode() {
        return consentCode;
    }

    public void setConsentCode(String consentCode) {
        this.consentCode = consentCode;
    }

    public String getConsentDisplayName() {
        return consentDisplayName;
    }

    public void setConsentDisplayName(String consentDisplayName) {
        this.consentDisplayName = consentDisplayName;
    }

    public String getConsentStartDate() {
        return consentStartDate;
    }

    public void setConsentStartDate(String consentStartDate) {
        this.consentStartDate = consentStartDate;
    }

    public String getConsentEndDate() {
        return consentEndDate;
    }

    public void setConsentEndDate(String consentEndDate) {
        this.consentEndDate = consentEndDate;
    }

    public String getPharmacistOid() {
        return pharmacistOid;
    }

    public void setPharmacistOid(String pharmacistOid) {
        this.pharmacistOid = pharmacistOid;
    }

    public String getDoctorOid() {
        return doctorOid;
    }

    public void setDoctorOid(String doctorOid) {
        this.doctorOid = doctorOid;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
