package tr.com.srdc.epsos.ws.xca.client.retrieve;

import eu.epsos.exceptions.XCAException;
import eu.europa.ec.sante.ehdsi.constant.error.EhdsiErrorCode;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.DocumentRequest;
import org.apache.commons.lang3.StringUtils;
import tr.com.srdc.epsos.util.Constants;
import tr.com.srdc.epsos.util.OidUtil;

public class RetrieveDocumentSetRequestTypeCreator {

    public RetrieveDocumentSetRequestType createRetrieveDocumentSetRequestType(String documentId, String homeCommunityId, String repositoryUniqId) throws XCAException {

        RetrieveDocumentSetRequestType retrieveDocumentSetRequest = new RetrieveDocumentSetRequestType();
        DocumentRequest documentRequest = new DocumentRequest();
        if (!OidUtil.isValidHomeCommunityId(StringUtils.remove(homeCommunityId, Constants.OID_PREFIX))) {
            throw new XCAException("Invalid message: HomeCommunity format not correct", EhdsiErrorCode.EHDSI_ERROR_GENERIC);
        }
        // Check for OID prefix, and adds it if not present (The OID prefix is required, as present in ITI TF-2b: 3.38.4.1.2.1);
        if (!homeCommunityId.startsWith(Constants.OID_PREFIX)) {
            homeCommunityId = Constants.OID_PREFIX + homeCommunityId;
        }

        // Set DocumentRequest/HomeCommunityId
        documentRequest.setHomeCommunityId(homeCommunityId);

        // Set DocumentRequest/RepositoryUniqueId
        documentRequest.setRepositoryUniqueId(repositoryUniqId);

        // Set DocumentRequest/DocumentUniqueId
        documentRequest.setDocumentUniqueId(documentId);

        retrieveDocumentSetRequest.getDocumentRequest().add(documentRequest);

        return retrieveDocumentSetRequest;
    }
}
