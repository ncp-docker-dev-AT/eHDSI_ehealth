package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.cts;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;

import java.util.List;
import java.util.Optional;

public interface CtsClient {

    void authenticate();

    Optional<ValueSetCatalogModel> fetchCatalogue();

    List<CodeSystemConceptModel> fetchConcepts(String valueSetId, String valueSetVersionId, int page, int size);
}
