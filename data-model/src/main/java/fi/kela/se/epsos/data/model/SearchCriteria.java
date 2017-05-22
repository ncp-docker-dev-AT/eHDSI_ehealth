package fi.kela.se.epsos.data.model;

import java.util.Iterator;

/**
 * SearchCriteria class will be used for XCAService interface to allow search of documents with
 * different criteria.
 * <p>
 * The class can be used like:
 * DocumentFactory.createSearchCriteria(Criteria.PatientId, "PatientId");
 * or
 * DocumentFactory.createSearchCriteria().add(Criteria.PatientId, "PatientId").add(Criteria.RepositoryId, "repositoryId");
 */
public interface SearchCriteria {

    SearchCriteria add(Criteria c, String value);

    String getCriteriaValue(Criteria c);

    Iterator<Criteria> getSearchCriteriaKeys();

    enum Criteria {
        PatientId,
        RepositoryId,
        DocumentId
    }
}
