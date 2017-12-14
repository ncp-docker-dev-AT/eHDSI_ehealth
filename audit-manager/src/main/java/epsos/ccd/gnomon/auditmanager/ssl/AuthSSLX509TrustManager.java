package epsos.ccd.gnomon.auditmanager.ssl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class AuthSSLX509TrustManager implements X509TrustManager {

    /**
     * Log object for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthSSLX509TrustManager.class);
    private List<String> authorizedDns;
    private X509TrustManager trustManager;
    private X509TrustManager defaultTrustManager;

    /**
     * Constructor for AuthSSLX509TrustManager.
     */
    public AuthSSLX509TrustManager(final X509TrustManager trustManager, final X509TrustManager defaultTrustManager,
                                   List<String> authorizedDns) {

        super();
        if (trustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.trustManager = trustManager;
        this.defaultTrustManager = defaultTrustManager;
        this.authorizedDns = authorizedDns;
        if (this.authorizedDns == null) {
            this.authorizedDns = new ArrayList<>();
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {

        if (certificates != null) {
            boolean isAuthDN = false;
            if (authorizedDns.isEmpty()) {
                isAuthDN = true;
            }
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                if (!isAuthDN) {
                    for (String authorizedDn : authorizedDns) {
                        if (StringUtils.equals(authorizedDn, cert.getSubjectDN().getName())) {
                            isAuthDN = true;
                        }
                    }
                }
                LOGGER.debug("Client certificate '{}':", (c + 1));
                LOGGER.debug("   Subject DN: '{}'", cert.getSubjectDN());
                LOGGER.debug("   Serial Number: '{}'", cert.getSerialNumber());
                LOGGER.debug("   Signature Algorithm: '{}'", cert.getSigAlgName());
                LOGGER.debug("   Valid from: '{}'", cert.getNotBefore());
                LOGGER.debug("   Valid until: '{}'", cert.getNotAfter());
                LOGGER.debug("   Issuer: '{}'", cert.getIssuerDN());
            }
            if (!isAuthDN) {
                throw new CertificateException("Subject DN is not authorized to perform the requested action.");
            }
            trustManager.checkClientTrusted(certificates, authType);
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {

        if (certificates != null) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                LOGGER.debug("Server certificate '{}':", (c + 1));
                LOGGER.debug("   Subject DN: '{}'", cert.getSubjectDN());
                LOGGER.debug("   Serial Number: '{}'", cert.getSerialNumber());
                LOGGER.debug("   Signature Algorithm: '{}'", cert.getSigAlgName());
                LOGGER.debug("   Valid from: '{}'", cert.getNotBefore());
                LOGGER.debug("   Valid until: '{}'", cert.getNotAfter());
                LOGGER.debug("   Issuer: '{}'", cert.getIssuerDN());
            }
        }

        try {
            if (defaultTrustManager != null) {
                defaultTrustManager.checkServerTrusted(certificates, authType);
                LOGGER.info("Default Trust Manager validated: '{}'", defaultTrustManager.toString());
            }
        } catch (CertificateException e) {
            LOGGER.warn("Default Trust Manager does not contain ROOT CA: '{}'", e.getMessage());
            trustManager.checkServerTrusted(certificates, authType);
            LOGGER.debug("Trust Manager validated");
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {

        X509Certificate[] certs = this.trustManager.getAcceptedIssuers();

        if (defaultTrustManager != null) {
            X509Certificate[] suncerts = this.defaultTrustManager.getAcceptedIssuers();
            X509Certificate[] all = new X509Certificate[certs.length + suncerts.length];
            System.arraycopy(certs, 0, all, 0, certs.length);
            System.arraycopy(suncerts, 0, all, certs.length, suncerts.length);
            certs = all;
        }
        if (certs == null) {
            certs = new X509Certificate[0];
        }

        return certs;
    }
}
