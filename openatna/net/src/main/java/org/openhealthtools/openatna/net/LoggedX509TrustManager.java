package org.openhealthtools.openatna.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * X509TrustManager with log info.
 */
public class LoggedX509TrustManager implements X509TrustManager {

    private final Logger logger = LoggerFactory.getLogger(LoggedX509TrustManager.class);
    private final X509TrustManager defaultTrustManager;
    SecureConnectionDescription scd;

    /**
     * Constructor for AuthSSLX509TrustManager.
     */
    public LoggedX509TrustManager(final X509TrustManager defaultTrustManager, SecureConnectionDescription scd) {
        super();
        if (defaultTrustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.defaultTrustManager = defaultTrustManager;
        this.scd = scd;
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType)
            throws CertificateException {
        if (logger.isInfoEnabled() && certificates != null) {
            StringBuilder stringBuilder = new StringBuilder("\n========== checking client certificate chain ==========");
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                stringBuilder.append("\n Client certificate ").append(c + 1).append(":");
                stringBuilder.append("\n  Subject DN: ").append(cert.getSubjectDN());
                stringBuilder.append("\n  Signature Algorithm: ").append(cert.getSigAlgName());
                stringBuilder.append("\n  Valid from: ").append(cert.getNotBefore());
                stringBuilder.append("\n  Valid until: ").append(cert.getNotAfter());
                stringBuilder.append("\n  Issuer: ").append(cert.getIssuerDN());
            }
            stringBuilder.append("\n=======================================================");
            logger.info(stringBuilder.toString());
        }
        // This will throw a CertificateException if it is not trusted.
        try {
            this.defaultTrustManager.checkClientTrusted(certificates, authType);
        } catch (CertificateException e) {
            logger.error("Something wrong with the client certificate (auth type: '{}')", authType, e);
            throw e;
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {

        if (logger.isInfoEnabled() && certificates != null) {
            StringBuilder certificateChain = new StringBuilder("Server Certificate Chain: \n");
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                certificateChain
                        .append("\n Server certificate ")
                        .append(c + 1)
                        .append(":")
                        .append("\n  Subject DN: ")
                        .append(cert.getSubjectDN())
                        .append("\n  Signature Algorithm: ")
                        .append(cert.getSigAlgName())
                        .append("\n  Valid from: ")
                        .append(cert.getNotBefore())
                        .append("\n  Valid until: ")
                        .append(cert.getNotAfter())
                        .append("\n  Issuer: ")
                        .append(cert.getIssuerDN());
            }
            logger.info(certificateChain.toString());
        }
        // This will throw a CertificateException if it is not trusted.
        try {
            this.defaultTrustManager.checkServerTrusted(certificates, authType);
        } catch (CertificateException e) {
            logger.error("Something wrong with the server certificate: (auth type: '{}')", authType, e);
            throw e;
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.defaultTrustManager.getAcceptedIssuers();
    }
}
