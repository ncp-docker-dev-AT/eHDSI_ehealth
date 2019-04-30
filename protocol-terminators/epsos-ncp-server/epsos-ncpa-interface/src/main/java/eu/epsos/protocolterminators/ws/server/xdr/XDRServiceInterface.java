package eu.epsos.protocolterminators.ws.server.xdr;

import epsos.ccd.gnomon.auditmanager.EventLog;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import org.apache.axiom.soap.SOAPHeader;

public interface XDRServiceInterface {

    /**
     * @param request
     * @param soapHeader
     * @param eventLog
     * @return
     * @throws Exception
     */
    RegistryResponseType saveDocument(ProvideAndRegisterDocumentSetRequestType request, SOAPHeader soapHeader, EventLog eventLog) throws Exception;
}
