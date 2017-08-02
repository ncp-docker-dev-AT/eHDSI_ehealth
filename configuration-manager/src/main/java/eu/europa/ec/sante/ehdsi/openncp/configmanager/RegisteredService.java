package eu.europa.ec.sante.ehdsi.openncp.configmanager;

public enum RegisteredService {

    PATIENT_IDENTIFICATION_SERVICE("PatientIdentificationService", "urn:ehealth:PatientIdentificationAndAuthentication::XCPD::CrossGatewayPatientDiscovery##ITI-55"),
    PATIENT_SERVICE("PatientService", "urn:ehealth:RequestOfData::XCA::CrossGatewayQuery##ITI-38"),
    ORDER_SERVICE("OrderService", "urn:ehealth:RequestOfData::XCA::CrossGatewayQuery##ITI-38"),
    DISPENSATION_SERVICE("DispensationService", "urn:ehealth:ProvisioningOfData:Provide::XDR::ProvideandRegisterDocumentSet-b##ITI-41"),
    CONSENT_SERVICE("ConsentService", "urn:ehealth:ProvisioningOfData:BPPC-RegisterUpdate::XDR::ProvideandRegisterDocumentSet-b##ITI-41"),
    ITI_63("ITI-63", "urn:ehealth:RequestOfData::XCF::CrossGatewayFetchRequest##ITI-63"),
    ITI_39("ITI-39", "urn:ehealth:RequestOfData::XCA::CrossGatewayRetrieve##ITI-39"),
    EPSOS_91("epsos-91", "urn:ehealth:CountryBIdentityProvider::identityProvider::HPAuthentication##epsos-91"),
    ITI_40("ITI-40", "urn:ehealth:CountryBIdentityProvider::XUA::ProvideX-UserAssertion##ITI-40"),
    EHEALTH_105("ehealth-105", "urn:ehealth:VPN::VPNGatewayServer##ehealth-105"),
    EHEALTH_106("ehealth-106", "urn:ehealth:VPN::VPNGatewayClient##ehealth-106"),
    EHEALTH_107("ehealth-107", "urn:ehealth:ISM::InternationalSearchMask##ehealth-107");

    private String serviceName;

    private String urn;

    RegisteredService(String serviceName, String urn) {
        this.serviceName = serviceName;
        this.urn = urn;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getUrn() {
        return urn;
    }
}
