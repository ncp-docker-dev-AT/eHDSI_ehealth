package eu.europa.ec.sante.ehdsi.gazelle.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
@PropertySource("classpath:test.properties")
public class TestConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public HttpClient httpClient() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor());

        if (Boolean.valueOf(environment.getProperty("http.proxy.used"))) {
            String hostname = environment.getProperty("http.proxy.host");
            int port = Integer.valueOf(environment.getProperty("http.proxy.port"));

            httpClientBuilder.setProxy(new HttpHost(hostname, port));

            if (Boolean.valueOf(environment.getProperty("http.proxy.authenticated"))) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(hostname, port),
                        new UsernamePasswordCredentials(environment.getProperty("http.proxy.username"),
                                environment.getProperty("http.proxy.password")));

                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        return httpClientBuilder.build();
    }
}
