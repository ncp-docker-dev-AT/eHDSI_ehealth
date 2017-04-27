package eu.europa.ec.sante.ehdsi.tsam.sync.client;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TermServerClient {

    void authenticate() throws AuthenticationException;

    Optional<ValueSetCatalogModel> retrieveValueSetCatalog(LocalDateTime currentAgreementDate);

    List<CodeSystemConceptModel> retrieveConcepts(String valueSetId, String valueSetVersionId, int page, int maxToReturn);
}
