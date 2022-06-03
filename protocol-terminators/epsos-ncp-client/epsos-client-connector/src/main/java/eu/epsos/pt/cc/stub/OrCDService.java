package eu.epsos.pt.cc.stub;

import eu.epsos.exceptions.XCAException;
import eu.epsos.pt.ws.client.xca.XcaInitGateway;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.RegisteredService;
import eu.europa.ec.sante.ehdsi.openncp.util.AssertionEnum;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.opensaml.saml.saml2.core.Assertion;
import tr.com.srdc.epsos.data.model.FilterParams;
import tr.com.srdc.epsos.data.model.GenericDocumentCode;
import tr.com.srdc.epsos.data.model.PatientId;
import tr.com.srdc.epsos.data.model.xds.QueryResponse;
import tr.com.srdc.epsos.data.model.xds.XDSDocument;

import java.util.List;
import java.util.Map;

/**
 *  * TODO: Insert description for OrCDService class.
 *
 * @author Mathias Ghys <mathias.ghys@ext.ec.europa.eu>
 */
public class OrCDService {

    private OrCDService() {
    }

    public static QueryResponse list(final PatientId pid,
                                     final String countryCode,
                                     final List<GenericDocumentCode> documentCodes,
                                     final FilterParams filterParams,
                                     final Map<AssertionEnum, Assertion> assertionMap) throws XCAException {

        return XcaInitGateway.crossGatewayQuery(pid, countryCode, documentCodes, filterParams, assertionMap,
                RegisteredService.ORCD_SERVICE.getServiceName());
    }

    public static RetrieveDocumentSetResponseType.DocumentResponse retrieve(final XDSDocument document,
                                                                            final String homeCommunityId,
                                                                            final String countryCode,
                                                                            final String targetLanguage,
                                                                            final Map<AssertionEnum, Assertion> assertionMap)
            throws XCAException {

        return XcaInitGateway.crossGatewayRetrieve(document, homeCommunityId, countryCode, targetLanguage, assertionMap, RegisteredService.ORCD_SERVICE.getServiceName());
    }
}
