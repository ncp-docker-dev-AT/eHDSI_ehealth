package eu.epsos.pt.cc.stub;

import eu.epsos.exceptions.XCAException;
import eu.epsos.pt.ws.client.xca.XcaInitGateway;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.opensaml.saml.saml2.core.Assertion;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;

/**
 * TODO: Insert description for OrderService class.
 *
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 * @author Luís Pinto<code> - luis.pinto@iuz.pt</code>
 */
public class OrderService {

    private OrderService() {
    }

    public static QueryResponse list(final PatientId pid, final String countryCode, final GenericDocumentCode documentCode,
                                     final Assertion idAssertion, final Assertion trcAssertion) throws XCAException {

        return XcaInitGateway.crossGatewayQuery(pid, countryCode, documentCode, idAssertion, trcAssertion,
                RegisteredService.ORDER_SERVICE.getServiceName());
    }

    public static RetrieveDocumentSetResponseType.DocumentResponse retrieve(final XDSDocument document,
                                                                            final String homeCommunityId,
                                                                            final String countryCode,
                                                                            final String targetLanguage,
                                                                            final Assertion hcpAssertion,
                                                                            final Assertion trcAssertion)
            throws XCAException {

        return XcaInitGateway.crossGatewayRetrieve(document, homeCommunityId, countryCode, targetLanguage, hcpAssertion,
                trcAssertion, RegisteredService.ORDER_SERVICE.getServiceName());
    }
}
