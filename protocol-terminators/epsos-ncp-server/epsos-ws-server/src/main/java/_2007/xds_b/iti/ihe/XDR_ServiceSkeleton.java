package _2007.xds_b.iti.ihe;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.epsos.protocolterminators.ws.server.xdr.XDRServiceInterface;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.apache.axiom.soap.SOAPHeader;
import tr.com.srdc.epsos.ws.server.xdr.XDRServiceImpl;

/**
 * XDR_ServiceSkeleton java skeleton for the axisService
 */
public class XDR_ServiceSkeleton {

    private XDRServiceInterface service = null;

    public XDR_ServiceSkeleton() {
    }

    /**
     * Auto generated method signature
     *
     * @param provideAndRegisterDocumentSetRequest
     */
    public RegistryResponseType documentRecipient_ProvideAndRegisterDocumentSetB(ProvideAndRegisterDocumentSetRequestType provideAndRegisterDocumentSetRequest,
                                                                                 SOAPHeader soapHeader, EventLog eventLog) throws Exception {

        if (service == null) {
            service = new XDRServiceImpl();
        }

        return service.saveDocument(provideAndRegisterDocumentSetRequest, soapHeader, eventLog);
    }
}
