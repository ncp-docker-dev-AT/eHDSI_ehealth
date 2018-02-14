package eu.europa.ec.sante.ehdsi.openncp.pt.common;

public class AdhocQueryResponseStatus {

    public static final String SUCCESS = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
    public static final String PARTIAL_SUCCESS = "urn:ihe:iti:2007:ResponseStatusType:PartialSuccess";
    public static final String FAILURE = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure";

    private AdhocQueryResponseStatus() {
    }
}
