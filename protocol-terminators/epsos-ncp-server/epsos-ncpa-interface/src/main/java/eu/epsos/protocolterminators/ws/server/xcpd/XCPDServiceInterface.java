package eu.epsos.protocolterminators.ws.server.xcpd;

import epsos.ccd.gnomon.auditmanager.EventLog;
import org.apache.axiom.soap.SOAPHeader;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;

public interface XCPDServiceInterface {

    /**
     * @param request
     * @param soapHeader
     * @param eventLog
     * @return
     * @throws Exception
     */
    PRPAIN201306UV02 queryPatient(PRPAIN201305UV02 request, SOAPHeader soapHeader, EventLog eventLog) throws Exception;
}
