package tr.com.srdc.epsos.ws.xdr.client.mock;

import eu.europa.ec.sante.ehdsi.openncp.pt.common.AdhocQueryResponseStatus;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.opensaml.saml2.core.Assertion;


/*
 *  DocumentRecipient_ServiceStub java implementation
 */
public class DocumentRecipient_ServiceStub extends org.apache.axis2.client.Stub {

    /**
     * Auto generated method signature
     *
     * @param provideAndRegisterDocumentSetRequest
     * @see tr.com.srdc.epsos.ws.xdr.client.DocumentRecipient_Service#documentRecipient_ProvideAndRegisterDocumentSetB
     */
    public RegistryResponseType documentRecipient_ProvideAndRegisterDocumentSetB(ProvideAndRegisterDocumentSetRequestType provideAndRegisterDocumentSetRequest,
                                                                                 Assertion idAssertion, Assertion trcAssertion) {
        RegistryResponseType mock = new RegistryResponseType();
        mock.setStatus(AdhocQueryResponseStatus.SUCCESS);

        return mock;
    }
}
