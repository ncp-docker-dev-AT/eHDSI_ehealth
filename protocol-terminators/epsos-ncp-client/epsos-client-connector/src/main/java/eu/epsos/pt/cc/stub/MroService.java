package eu.epsos.pt.cc.stub;

import eu.epsos.exceptions.XCAException;
import eu.epsos.pt.ws.client.xca.XcaInitGateway;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.opensaml.saml.saml2.core.Assertion;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;
import tr.com.srdc.epsos.util.Constants;

import java.util.Arrays;

/**
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
public class MroService {

    private MroService() {
    }

    public static QueryResponse list(final PatientId pid, final String countryCode, final GenericDocumentCode documentCode,
                                     final Assertion idAssertion, final Assertion trcAssertion) throws XCAException {
        return XcaInitGateway.crossGatewayQuery(pid, countryCode, Arrays.asList(documentCode), idAssertion, trcAssertion, Constants.MroService);
    }

    public static RetrieveDocumentSetResponseType.DocumentResponse retrieve(
            final XDSDocument document,
            final String homeCommunityId,
            final String countryCode,
            final String targetLanguage,
            final Assertion hcpAssertion,
            final Assertion trcAssertion) throws XCAException {
        return XcaInitGateway.crossGatewayRetrieve(document, homeCommunityId, countryCode, targetLanguage, hcpAssertion, trcAssertion, Constants.MroService);
    }
}
