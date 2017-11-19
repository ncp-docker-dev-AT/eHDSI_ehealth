package _2007.xds_b.iti.ihe;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.epsos.protocolterminators.ws.server.xca.XCAServiceInterface;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import tr.com.srdc.epsos.ws.server.xca.impl.XCAServiceImpl;

/**
 * XCA_ServiceSkeleton java skeleton for the axisService
 */
public class XCA_ServiceSkeleton {

    /**
     * Auto generated method signature
     *
     * @param adhocQueryRequest
     */
    private XCAServiceInterface service = null;

    public XCA_ServiceSkeleton() {
    }

    public AdhocQueryResponse respondingGateway_CrossGatewayQuery(AdhocQueryRequest adhocQueryRequest, SOAPHeader sh,
                                                                  EventLog eventLog) throws Exception {

        if (service == null) {
            service = new XCAServiceImpl();
        }
        return service.queryDocument(adhocQueryRequest, sh, eventLog);
    }

    /**
     * Auto generated method signature
     *
     * @param retrieveDocumentSetRequest
     */
    public void respondingGateway_CrossGatewayRetrieve(RetrieveDocumentSetRequestType retrieveDocumentSetRequest,
                                                       SOAPHeader soapHeader, EventLog eventLog, OMElement omElement) throws Exception {

        if (service == null) {
            service = new XCAServiceImpl();
        }
        service.retrieveDocument(retrieveDocumentSetRequest, soapHeader, eventLog, omElement);
    }
}
