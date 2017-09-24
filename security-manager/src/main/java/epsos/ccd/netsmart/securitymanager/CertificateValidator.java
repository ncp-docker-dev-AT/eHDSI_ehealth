/*
 *  Copyright 2010 Jerry Dimitriou <jerouris at netsmart.gr>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package epsos.ccd.netsmart.securitymanager;

import epsos.ccd.netsmart.securitymanager.exceptions.SMgrException;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.io.IOException;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Date;

/**
 * The Certificate Validator is a component that is responsible for validating
 * certificates against the NCP Trust Store. The certificate validation consists
 * of checking if the certificate is trusted and if it is not revoked: All
 * certificates registered with the local NCP trust store are assumed as
 * trusted. For revocation check both CRL retrieval and OCSP are supported.
 *
 * @author Jerry Dimitriou <jerouris at netsmart.gr>
 */
public final class CertificateValidator extends KeySelector {

    public static final String CRLDP_OID = "2.5.29.31";
    public static final String AIA_OID = "1.3.6.1.5.5.7.1.1";
    public static final String PROP_CHECK_FOR_KEYUSAGE = "secman.cert.validator.checkforkeyusage";
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateValidator.class);
    private final KeyStore trustStore;

    private boolean CHECK_FOR_KEYUSAGE;
    private Certificate cert = null;
    private boolean isRevocationEnabled = false;

    public CertificateValidator(KeyStore _trustStore) throws IOException {

        CHECK_FOR_KEYUSAGE = false;
        trustStore = _trustStore;

        if (StringUtils.equalsIgnoreCase(ConfigurationManagerFactory.getConfigurationManager()
                .getProperty(PROP_CHECK_FOR_KEYUSAGE).trim(), "true")) {
            CHECK_FOR_KEYUSAGE = true;
        }
    }

    private static boolean algEquals(String algURI, String algName) {

        if ((algName.equalsIgnoreCase("DSA") && algURI.contains("#dsa"))
                || (algName.equalsIgnoreCase("RSA") && algURI.contains("#rsa"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method verifies the validity of a certificate by a given keyInfo structure
     *
     * @param keyInfo The keyInfo structure that contains the certificate (X509).
     * @throws SMgrException when the validation of the certificate fails
     */
    private void validateCertificate(KeyInfo keyInfo) throws SMgrException {

        for (Object object : keyInfo.getContent()) {
            if (object instanceof X509Data) {
                X509Data data = (X509Data) object;
                for (Object o : data.getContent()) {
                    // check X509Certificate
                    if (o instanceof X509Certificate) {
                        X509Certificate xcert = (X509Certificate) o;
                        validateCertificate(xcert);
                        cert = xcert;
                        return;
                    } else {
                        // skip all other entries
                        continue;
                    }
                }
                throw new SMgrException("The KeyInfo Structure does not contain X509Data element: No Certificate Present");
            }
        }
    }

    public void validateCertificate(org.opensaml.xml.signature.KeyInfo keyInfo) throws SMgrException {

        for (org.opensaml.xml.signature.X509Data object : keyInfo.getX509Datas()) {
            if (object != null) {
                for (Object o : object.getX509Certificates()) {
                    // check X509Certificate
                    if (o instanceof org.opensaml.xml.signature.X509Certificate) {
                        X509Certificate xcert = (X509Certificate) o;
                        validateCertificate(xcert);
                        cert = xcert;
                        return;
                    } else {
                        // skip all other entries
                        continue;
                    }
                }
                throw new SMgrException("The KeyInfo Structure does not contain X509Data element: No Certificate Present");
            }
        }
    }

    /**
     * This method verifies the validity of the given X509 certificate by checking the truststore.
     *
     * @param cert the certificate that will be validated
     * @throws SMgrException when the validation of the certificate fails
     */
    public void validateCertificate(X509Certificate cert) throws SMgrException {

        try {
            if (CHECK_FOR_KEYUSAGE) {
                LOGGER.info("Key usage available in conf manager");
                boolean[] keyUsage = cert.getKeyUsage();

                if (keyUsage == null || !keyUsage[0]) {
                    throw new SMgrException("Certificate Key Usage Extension for Digital Signature Missing");
                }
            }

            try {
                cert.checkValidity(new Date());
            } catch (CertificateExpiredException ex) {
                LOGGER.error(null, ex);
                throw new SMgrException("Certificate Expired", ex);
            } catch (CertificateNotYetValidException ex) {
                LOGGER.error(null, ex);
                throw new SMgrException("Certificate Not Valid Yet", ex);
            }

            // Check if the cert supports the CRLDP
            if (cert.getExtensionValue(AIA_OID) != null) {
                setRevocationEnabled(true);
                setOCSPEnabled(true);
                LOGGER.info("Found AIA Extension. Using OCSP");
            }

            if (cert.getExtensionValue(CRLDP_OID) != null) {
                setRevocationEnabled(true);
                setCRLDPEnabled(true);
                LOGGER.info("Found CRLDP Extension. Using CRLDP");
            }

            CertStoreParameters intermediates = new CollectionCertStoreParameters(Collections.singletonList(cert));

            X509CertSelector target = new X509CertSelector();
            target.setCertificate(cert);

            PKIXBuilderParameters builderParams = new PKIXBuilderParameters(trustStore, target);
            builderParams.addCertStore(CertStore.getInstance("Collection", intermediates));
            builderParams.setRevocationEnabled(isRevocationEnabled);
            builderParams.setDate(new Date());

            PKIXParameters params = new PKIXParameters(trustStore);
            params.setRevocationEnabled(isRevocationEnabled);

        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error(null, ex);
            throw new SMgrException("Certificate's Public key algorithm is unknown", ex);
        } catch (KeyStoreException ex) {
            LOGGER.error(null, ex);
            throw new SMgrException("Error when tried to use the TrustStore", ex);
        } catch (InvalidAlgorithmParameterException ex) {
            LOGGER.error(null, ex);
            throw new SMgrException("Invalid Algorith parameters for building Certificate Path", ex);
        }
    }

    public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
                                    XMLCryptoContext context) throws KeySelectorException {

        for (Object o1 : keyInfo.getContent()) {
            XMLStructure info = (XMLStructure) o1;
            if (!(info instanceof X509Data)) {
                continue;
            }
            X509Data x509Data = (X509Data) info;
            for (Object o : x509Data.getContent()) {
                if (!(o instanceof X509Certificate)) {
                    continue;
                }
                try {
                    validateCertificate(keyInfo);
                } catch (SMgrException ex) {
                    LOGGER.error(null, ex);
                    throw new KeySelectorException("Validation Failed: " + ex.getMessage());
                }
                final PublicKey key = ((X509Certificate) o).getPublicKey();
                // Make sure the algorithm is compatible with the method.
                if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                    return new KeySelectorResult() {
                        public Key getKey() {
                            return key;
                        }
                    };
                }
                // if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                //return () -> key;
                //}
            }
        }
        throw new KeySelectorException("No key found!");
    }

    /**
     * @return the X509 Certificate that is validated from the #validate function
     */
    public Certificate getValidatedCertificate() {
        return cert;
    }

    private void setOCSPEnabled(boolean flag) {
        java.security.Security.setProperty("ocsp.enable", Boolean.toString(flag));
    }

    private void setCRLDPEnabled(boolean flag) {
        System.setProperty("com.sun.security.enableCRLDP", Boolean.toString(flag));
    }

    private void setRevocationEnabled(boolean flag) {
        this.isRevocationEnabled = flag;
    }
}
