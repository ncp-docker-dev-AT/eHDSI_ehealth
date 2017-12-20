package eu.esens.abb.nonrep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IHEXCARetrieve implements IHEMessageType {

    private static final Logger LOGGER = LoggerFactory.getLogger(IHEXCARetrieve.class);
    private static final String XDS_NS = "urn:ihe:iti:xds-b:2007";
    private final String hcid;
    private final String repuid;
    private final String docuid;

    /**
     * @param body
     * @throws MalformedIHESOAPException
     */
    public IHEXCARetrieve(final Element body) throws MalformedIHESOAPException {

        if (body == null) {
            throw new MalformedIHESOAPException("No body passed in the XCA retrieve");
        }

        NodeList nl = body.getElementsByTagNameNS(XDS_NS, "RetrieveDocumentSetRequest");
        Utilities.checkForNull(nl, "RetrieveDocumentSetRequest", LOGGER);

        Element retrieveDoc = (Element) nl.item(0);

        nl = retrieveDoc.getElementsByTagNameNS(XDS_NS, "DocumentRequest");
        Utilities.checkForNull(nl, "DocumentRequest", LOGGER);

        Element docRequest = (Element) nl.item(0);

        // Now the values out of the message
        nl = docRequest.getElementsByTagNameNS(XDS_NS, "HomeCommunityId");
        Utilities.checkForNull(nl, "HomeCommunityId", LOGGER);
        hcid = (nl.item(0)).getTextContent();

        nl = docRequest.getElementsByTagNameNS(XDS_NS, "RepositoryUniqueId");
        Utilities.checkForNull(nl, "RepositoryUniqueId", LOGGER);
        repuid = (nl.item(0)).getTextContent();

        nl = docRequest.getElementsByTagNameNS(XDS_NS, "DocumentUniqueId");
        Utilities.checkForNull(nl, "DocumentUniqueId", LOGGER);
        docuid = (nl.item(0)).getTextContent();
    }

    public final String getHomeCommunityID() {
        return hcid;
    }

    public final String getRepositoryUniqueId() {
        return repuid;
    }

    public final String getDocumentUniqueId() {
        return docuid;
    }
}
