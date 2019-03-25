package eu.epsos.protocolterminators.ws.server.xca;

import epsos.ccd.gnomon.auditmanager.EventLog;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;

public interface XCAServiceInterface {

    /**
     * @param request
     * @param soapHeader
     * @param eventLog
     * @return
     * @throws Exception
     */
    AdhocQueryResponse queryDocument(AdhocQueryRequest request, SOAPHeader soapHeader, EventLog eventLog) throws Exception;

    /**
     * @param request
     * @param soapHeader
     * @param eventLog
     * @param response
     * @throws Exception
     */
    void retrieveDocument(RetrieveDocumentSetRequestType request, SOAPHeader soapHeader, EventLog eventLog, OMElement response) throws Exception;
}
