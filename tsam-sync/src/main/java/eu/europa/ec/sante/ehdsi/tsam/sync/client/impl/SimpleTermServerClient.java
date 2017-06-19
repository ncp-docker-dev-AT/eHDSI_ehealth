package eu.europa.ec.sante.ehdsi.tsam.sync.client.impl;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.client.AuthenticationException;
import eu.europa.ec.sante.ehdsi.tsam.sync.client.TermServerClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class SimpleTermServerClient implements TermServerClient {

    private final Logger logger = LoggerFactory.getLogger(SimpleTermServerClient.class);

    private final Environment environment;

    private final String baseUrl;

    private final String username;

    private final String password;

    private RestTemplate restTemplate;

    @Autowired
    public SimpleTermServerClient(Environment environment) {
        Assert.notNull(environment, "environment must not be null");
        this.environment = environment;

        this.baseUrl = environment.getRequiredProperty("ehealth.termserver.base-url");
        this.username = environment.getRequiredProperty("ehealth.termserver.username");
        this.password = environment.getRequiredProperty("ehealth.termserver.password");
    }

    @PostConstruct
    public void init() {
        HttpClientBuilder builder = HttpClients.custom();

        if (environment.getRequiredProperty("openncp.tsam-sync.proxy.use", Boolean.class)) {
            String proxyHost = environment.getRequiredProperty("openncp.tsam-sync.proxy.host");
            Integer proxyPort = environment.getRequiredProperty("openncp.tsam-sync.proxy.port", Integer.class);

            if (environment.getRequiredProperty("openncp.tsam-sync.proxy.use-authentication", Boolean.class)) {
                String proxyUsername = environment.getRequiredProperty("openncp.tsam-sync.proxy.username");
                String proxyPassword = environment.getRequiredProperty("openncp.tsam-sync.proxy.password");

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxyHost, proxyPort),
                        new UsernamePasswordCredentials(proxyUsername, proxyPassword));

                builder.setDefaultCredentialsProvider(credentialsProvider);
            }

            builder.setProxy(new HttpHost(proxyHost, proxyPort));
        }

        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());

        restTemplate = new RestTemplate(
                new CustomHttpComponentsClientHttpRequestFactory(builder.build(), httpContext));
    }

    @Override
    public void authenticate() {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("username", username);
        request.add("password", password);

        URI location = restTemplate.postForLocation(baseUrl + "/login", request);

        if (StringUtils.containsIgnoreCase(location.toString(), "?error")) {
            throw new AuthenticationException(MessageFormat.format("Authentication failed for user ''{0}''", username));
        }

        logger.info("User '{}' authenticated successfully", username);
    }

    @Override
    public Optional<ValueSetCatalogModel> retrieveValueSetCatalog(LocalDateTime currentAgreementDate) {
        ResponseEntity<ValueSetCatalogModel> response =
                restTemplate.getForEntity(baseUrl + "/api/sync/valuesetcatalog?agreementDate={agreementDate}", ValueSetCatalogModel.class, currentAgreementDate);
        return Optional.of(response.getBody());
    }

    @Override
    public List<CodeSystemConceptModel> retrieveConcepts(String valueSetId, String valueSetVersionId, int page, int maxToReturn) {
        CodeSystemConceptModel[] codeSystemConcepts =
                restTemplate.getForObject(baseUrl + "/api/sync/valueset/{valuesetid}/valuesetdefinition/{valuesetdefinition}/entries?page={page}&maxtoreturn={maxToReturn}",
                        CodeSystemConceptModel[].class, valueSetId, valueSetVersionId, page, maxToReturn);
        return Arrays.asList(codeSystemConcepts);
    }
}
