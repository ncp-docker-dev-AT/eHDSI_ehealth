package _2009.xcpd.iti.ihe;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.epsos.protocolterminators.ws.server.xcpd.XCPDServiceInterface;
import org.apache.axiom.soap.SOAPHeader;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import tr.com.srdc.epsos.ws.server.xcpd.impl.XCPDServiceImpl;

/**
 * XCPD_ServiceSkeleton java skeleton for the axisService
 */
public class XCPD_ServiceSkeleton {

    private XCPDServiceInterface service = null;

    public XCPD_ServiceSkeleton() {
    }

    /**
     * Auto generated method signature
     *
     * @param request
     * @param header
     * @param event
     */

    public PRPAIN201306UV02 respondingGateway_PRPA_IN201305UV02(PRPAIN201305UV02 request, SOAPHeader header, EventLog event)
            throws Exception {

        if (service == null) {
            service = new XCPDServiceImpl();
        }

        return service.queryPatient(request, header, event);
    }
}
