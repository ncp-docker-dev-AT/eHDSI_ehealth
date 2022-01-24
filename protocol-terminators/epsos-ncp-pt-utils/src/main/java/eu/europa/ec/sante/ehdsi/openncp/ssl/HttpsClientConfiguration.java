package eu.europa.ec.sante.ehdsi.openncp.ssl;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.util.ResourceUtils;
import tr.com.srdc.epsos.util.Constants;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class HttpsClientConfiguration {

    private HttpsClientConfiguration() {
    }

    public static SSLContext buildSSLContext() throws NoSuchAlgorithmException, KeyManagementException, IOException,
            CertificateException, KeyStoreException, UnrecoverableKeyException {

        SSLContextBuilder builder = SSLContextBuilder.create();
        builder.setKeyStoreType("JKS");
        builder.setKeyManagerFactoryAlgorithm("SunX509");
        builder.loadKeyMaterial(ResourceUtils.getFile(Constants.SC_KEYSTORE_PATH),
                Constants.SC_KEYSTORE_PASSWORD.toCharArray(),
                Constants.SC_PRIVATEKEY_PASSWORD.toCharArray());
        builder.loadTrustMaterial(ResourceUtils.getFile(Constants.TRUSTSTORE_PATH),
                Constants.TRUSTSTORE_PASSWORD.toCharArray(), TrustAllStrategy.INSTANCE);

        return builder.build();
    }

    public static HttpClient getSSLClient() throws UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {

        SSLContext sslContext = buildSSLContext();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslContext, new String[]{"TLSv1.2", "TLSv1.3"}, null, NoopHostnameVerifier.INSTANCE);
        HttpClientBuilder builder = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory);
        builder.setSSLContext(sslContext);
        builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

        return builder.build();
    }
}
