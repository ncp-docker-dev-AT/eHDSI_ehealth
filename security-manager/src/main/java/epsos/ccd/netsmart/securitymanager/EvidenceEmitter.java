package epsos.ccd.netsmart.securitymanager;

import org.w3c.dom.Element;

public interface EvidenceEmitter {

    /**
     * @param uuid
     * @param policyId
     * @param issuerCertificate
     * @param authenticationTime
     * @param authenticationMethod
     * @param senderCertificateDetails
     * @param recipientCertificateDetails
     * @param evidenceEvent
     * @param uaMessageIdentifier
     * @param digest
     * @throws Exception
     */
    Element emitNRO(String uuid, String policyId, String issuerCertificate, String authenticationTime,
                    String authenticationMethod, String senderCertificateDetails, String recipientCertificateDetails,
                    String evidenceEvent, String uaMessageIdentifier, String digest) throws Exception;

    /**
     * @param uuid
     * @param policyId
     * @param issuerCertificate
     * @param authenticationTime
     * @param authenticationMethod
     * @param senderCertificateDetails
     * @param recipientCertificateDetails
     * @param evidenceEvent
     * @param uaMessageIdentifier
     * @param digest
     * @return
     * @throws Exception
     */
    Element emitNRR(String uuid, String policyId, String issuerCertificate, String authenticationTime,
                    String authenticationMethod, String senderCertificateDetails, String recipientCertificateDetails,
                    String evidenceEvent, String uaMessageIdentifier, String digest) throws Exception;
}
