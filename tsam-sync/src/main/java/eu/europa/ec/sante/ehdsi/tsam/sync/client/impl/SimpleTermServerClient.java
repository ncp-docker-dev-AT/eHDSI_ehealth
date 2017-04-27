package eu.europa.ec.sante.ehdsi.tsam.sync.client.impl;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.client.AuthenticationException;
import eu.europa.ec.sante.ehdsi.tsam.sync.client.TermServerClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class SimpleTermServerClient implements TermServerClient {

    private final Logger logger = LoggerFactory.getLogger(SimpleTermServerClient.class);

    private final RestTemplate template;

    @Value("${ehealth.termserver.base-url}")
    private String baseUrl;

    @Value("${ehealth.termserver.username}")
    private String username;

    @Value("${ehealth.termserver.password}")
    private String password;

    public SimpleTermServerClient() {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
        this.template = new RestTemplate(new CustomHttpComponentsClientHttpRequestFactory(httpClient, httpContext));
    }

    @Override
    public void authenticate() {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("username", username);
        request.add("password", password);

        URI location = this.template.postForLocation(baseUrl + "/login", request);

        if (StringUtils.containsIgnoreCase(location.toString(), "?error")) {
            throw new AuthenticationException(MessageFormat.format("Authentication failed for user ''{0}''", username));
        }

        logger.info("User '{}' authenticated successfully", username);
    }

    @Override
    public Optional<ValueSetCatalogModel> retrieveValueSetCatalog(LocalDateTime currentAgreementDate) {
        ResponseEntity<ValueSetCatalogModel> response =
                template.getForEntity(baseUrl + "/api/sync/valuesetcatalog?agreementDate={agreementDate}", ValueSetCatalogModel.class, currentAgreementDate);
        return Optional.of(response.getBody());
    }

    @Override
    public List<CodeSystemConceptModel> retrieveConcepts(String valueSetId, String valueSetVersionId, int page, int maxToReturn) {
        CodeSystemConceptModel[] codeSystemConcepts =
                template.getForObject(baseUrl + "/api/sync/valueset/{valuesetid}/valuesetdefinition/{valuesetdefinition}/entries?page={page}&maxtoreturn={maxToReturn}",
                        CodeSystemConceptModel[].class, valueSetId, valueSetVersionId, page, maxToReturn);
        return Arrays.asList(codeSystemConcepts);
    }
}
