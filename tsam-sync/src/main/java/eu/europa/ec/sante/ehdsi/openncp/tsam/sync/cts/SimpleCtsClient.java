package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.cts;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.config.CtsProperties;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class SimpleCtsClient implements CtsClient {

    private static final String AUTHENTICATE_URL = "/login";

    private static final String FETCH_CATALOGUE_URL = "/api/sync/valuesetcatalog";

    private static final String FETCH_CONCEPTS_URL = "/api/sync/valueset/{valueSetId}/valuesetdefinition/{valueSetDefinition}/entries?page={page}&maxtoreturn={size}";

    private final CtsProperties ctsProperties;

    private final RestTemplate restTemplate;

    public SimpleCtsClient(CtsProperties ctsProperties, RestTemplate restTemplate) {
        this.ctsProperties = ctsProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void authenticate() {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("username", ctsProperties.getUsername());
        request.add("password", ctsProperties.getPassword());

        URI location = restTemplate.postForLocation(ctsProperties.getUrl() + AUTHENTICATE_URL, request);
        if (Objects.equals(location.getQuery(), "error")) {
            throw new CtsClientException("Authentication failed for '" + ctsProperties.getUrl() + "'");
        }
    }

    @Override
    public Optional<ValueSetCatalogModel> fetchCatalogue() {
        ValueSetCatalogModel catalogue = restTemplate.getForObject(ctsProperties.getUrl() + FETCH_CATALOGUE_URL, ValueSetCatalogModel.class);
        return Optional.ofNullable(catalogue);
    }

    @Override
    public List<CodeSystemConceptModel> fetchConcepts(String valueSetId, String valueSetVersionId, int page, int size) {
        CodeSystemConceptModel[] concepts =
                restTemplate.getForObject(ctsProperties.getUrl() + FETCH_CONCEPTS_URL, CodeSystemConceptModel[].class, valueSetId, valueSetVersionId, page, size);
        return Arrays.asList(concepts);
    }
}
